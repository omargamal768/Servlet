package DAO;

import hibernate.HibernateUtil;
import models.Employee;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class EmployeeDao {



    public List<Employee> getAllEmployees() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            Query<Employee> query = session.createQuery("SELECT e FROM Employee e", Employee.class);
            return query.list();
        } catch (Exception e) {e.printStackTrace();return null;}}




    public void saveEmployee(Employee employee) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            System.out.print(employee);
            session.save(employee);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions
        }
    }

    public void deleteEmployeeById(Long employeeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // Load the Employee entity by ID
            Employee employee = session.load(Employee.class, employeeId);
            if (employee != null) {
                session.delete(employee);
                tx.commit();
            } else {
                // Handle case when employee with given ID is not found
                throw new IllegalArgumentException("Employee with ID " + employeeId + " not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateEmployee(Employee updatedEmployee) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.update(updatedEmployee);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Employee getEmployeeById(Long employeeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Employee.class, employeeId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<Employee> getAllSalary() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            Query<Employee> query = session.createQuery("SELECT salary FROM Employee e", Employee.class);
            return query.list();
        } catch (Exception e) {e.printStackTrace();return null;}}


}
