package DAO;

import hibernate.HibernateUtil;
import models.Department;

import models.Employee;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class DepartmentDao {

    public List<Department> getAllDepartments() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Department ";
            Query<Department> query = session.createQuery(hql, Department.class);
            return query.list();
        } catch (Exception e) {e.printStackTrace();return null;}}

    public Department getDepartmentById(Long departmentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Department.class, departmentId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public void saveDepartment(Department department) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            System.out.print(department);
            session.save(department);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions
        }
    }

    public void deleteDepartmentById(Long departmentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // Load the Employee entity by ID
            Department department = session.load(Department.class, departmentId);
            if (department != null) {
                session.delete(department);
                tx.commit();
            } else {
                // Handle case when employee with given ID is not found
                throw new IllegalArgumentException("Department with ID " + departmentId + " not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateDepartment(Department updatedDepartment) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.update(updatedDepartment);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
