package org.example.demo2;

import DAO.DepartmentDao;
import DAO.HrDao;
import DAO.ProjectDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.Department;
import models.Project;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@WebServlet(name = "helloSvlet", value = "/pro")
public class ProjectServlet extends HttpServlet{
    private final ProjectDao projectDao = new ProjectDao();
    private final Gson gson = new Gson();

    private final HrDao hrDao = new HrDao();
    public void init() {

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {


        String idString = request.getParameter("id");
        if (idString != null && !idString.isEmpty()) {
            // If ID parameter is present, handle request to get single employee
            getProjectById(request, response);
        } else {
            // If ID parameter is not present, handle request to get all employees
            getAllProjects(request, response);
        }

    }

    private void getProjectById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{
            if (!isHrAuthenticated(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized access");
                return;
            }
            Long projectId = Long.parseLong(request.getParameter("id"));
            Project project = projectDao.getProjectById(projectId);
            if (project != null) {
                // Convert employee object to JSON and send as response
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(new Gson().toJson(project));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Project not found.");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid project ID format.");
        }
    }

    private void getAllProjects(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isHrAuthenticated(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized access");
            return;
        }
        response.setContentType("application/json");
        List<Project> items = projectDao.getAllProjects();
        String json = gson.toJson(items);
        response.getWriter().write(json);    }



    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isHrAuthenticated(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized access");
            return;
        }
        Project project=new Project();
        System.out.print(project);

        project = getProjectObject(request);

        projectDao.saveProject(project);
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setContentType("text/plain");
        // Write response
        response.getWriter().println("Project created successfully");
    }

    private Project getProjectObject(HttpServletRequest request) {
        BufferedReader reader = null;
        StringBuilder jsonData = null;
        Project project=null;
        try {
            reader = request.getReader();
            jsonData = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonData.append(line);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Parse the JSON data into a Java object
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            project = objectMapper.readValue(jsonData.toString(), Project.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return project;
    }
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isHrAuthenticated(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized access");
            return;
        }
        String idString = request.getParameter("id");
        if (idString != null && !idString.isEmpty()) {
            try {
                Long projectId = Long.parseLong(idString);
                projectDao.deleteProjectById(projectId);
                response.getWriter().write("Project with ID " + projectId + " deleted successfully!");
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid project ID format.");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Project ID parameter is required.");
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isHrAuthenticated(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized access");
            return;
        }
        BufferedReader reader = request.getReader();
        Project updatedProject = gson.fromJson(reader, Project.class);
        projectDao.updateProject(updatedProject);
        response.getWriter().write("Project updated successfully!");
    }
    // Helper method to check user authentication
    private boolean isHrAuthenticated(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Basic ")) {
            String base64Credentials = authorizationHeader.substring("Basic ".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String[] parts = credentials.split(":", 2);
            String username = parts[0];
            String password = parts[1];
            return isValidAdmin(username, password);
        }
        return false; // Authorization header missing or invalid
    }

    // Helper method to validate admin credentials using database check
    private boolean isValidAdmin(String username, String password) {
        return hrDao.isValidUser(username, password);
    }

    public void destroy() {
    }
}
