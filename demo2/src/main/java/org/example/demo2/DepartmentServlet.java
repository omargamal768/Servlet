package org.example.demo2;

import DAO.DepartmentDao;
import DAO.EmployeeDao;
import DAO.HrDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.Department;
import models.Employee;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@WebServlet(name = "helloSevlet", value = "/dep")
public class DepartmentServlet extends HttpServlet{
    private final DepartmentDao departmentDao = new DepartmentDao();
    private final Gson gson = new Gson();
    private final HrDao hrDao = new HrDao();

    public void init() {

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {


        String idString = request.getParameter("id");
        if (idString != null && !idString.isEmpty()) {
            // If ID parameter is present, handle request to get single employee
            getDepartmentById(request, response);
        } else {
            // If ID parameter is not present, handle request to get all employees
            getAllDepartments(request, response);
        }

    }

    private void getDepartmentById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{
            if (!isHrAuthenticated(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized access");
                return;
            }
            Long departmentId = Long.parseLong(request.getParameter("id"));
            Department department = departmentDao.getDepartmentById(departmentId);
            if (department != null) {
                // Convert employee object to JSON and send as response
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(new Gson().toJson(department));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Department not found.");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid employee ID format.");
        }
    }

    private void getAllDepartments(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isHrAuthenticated(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized access");
            return;
        }
        response.setContentType("application/json");
        List<Department> items = departmentDao.getAllDepartments();
        String json = gson.toJson(items);
        response.getWriter().write(json);    }



    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isHrAuthenticated(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized access");
            return;
        }
        Department department=new Department();
        System.out.print(department);

        department = getDepartmentObject(request);

        departmentDao.saveDepartment(department);
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setContentType("text/plain");
        // Write response
        response.getWriter().println("Department created successfully");
    }

    private Department getDepartmentObject(HttpServletRequest request) {
        BufferedReader reader = null;
        StringBuilder jsonData = null;
        Department department=null;
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
            department = objectMapper.readValue(jsonData.toString(), Department.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return department;
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
                Long departmentId = Long.parseLong(idString);
                departmentDao.deleteDepartmentById(departmentId);
                response.getWriter().write("Department with ID " + departmentId + " deleted successfully!");
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid Department ID format.");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Department ID parameter is required.");
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isHrAuthenticated(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized access");
            return;
        }
        BufferedReader reader = request.getReader();
        Department updatedDepartment = gson.fromJson(reader, Department.class);
        departmentDao.updateDepartment(updatedDepartment);
        response.getWriter().write("Department updated successfully!");
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
