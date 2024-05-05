package org.example.demo2;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import DAO.HrDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import DAO.EmployeeDao;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import models.Employee;

@WebServlet(name = "helloServlet", value = "/api")
public class EmployeeServlet  extends HttpServlet{
    private final EmployeeDao employeeDao = new EmployeeDao();
    private final Gson gson = new Gson();
    private final HrDao hrDao = new HrDao();
    private String message;

    public void init() {

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String department = request.getParameter("department");

        String idString = request.getParameter("id");
        if (idString != null && !idString.isEmpty()) {
            // If ID parameter is present, handle request to get single employee
            getEmployeeById(request, response);
        }
        else{
            // If ID parameter is not present, handle request to get all employees
            getAllEmployees(request, response);
        }

    }

    private void getEmployeeById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Long employeeId = Long.parseLong(request.getParameter("id"));
            Employee employee = employeeDao.getEmployeeById(employeeId);
            if (employee != null) {
                // Convert employee object to JSON and send as response
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(new Gson().toJson(employee));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Employee not found.");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid employee ID format.");
        }
    }

    private void getAllEmployees(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isHrAuthenticated(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized access");
            return;
        }
        response.setContentType("application/json");
        List<Employee> items = employeeDao.getAllEmployees();
        String json = gson.toJson(items);
        response.getWriter().write(json);    }



    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isHrAuthenticated(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized access");
            return;
        }
        Employee employee=new Employee();
        System.out.print(employee);

        employee = getEmployeeObject(request);

        employeeDao.saveEmployee(employee);
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setContentType("text/plain");
        // Write response
        response.getWriter().println("Employee created successfully");
    }

    private Employee getEmployeeObject(HttpServletRequest request) {
        BufferedReader reader = null;
        StringBuilder jsonData = null;
        Employee employee=null;
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
            employee = objectMapper.readValue(jsonData.toString(), Employee.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return employee;
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
                Long employeeId = Long.parseLong(idString);
                employeeDao.deleteEmployeeById(employeeId);
                response.getWriter().write("Employee with ID " + employeeId + " deleted successfully!");
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid employee ID format.");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Employee ID parameter is required.");
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isHrAuthenticated(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized access");
            return;
        }
        BufferedReader reader = request.getReader();
        Employee updatedEmployee = gson.fromJson(reader, Employee.class);
        employeeDao.updateEmployee(updatedEmployee);
        response.getWriter().write("Employee updated successfully!");
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
