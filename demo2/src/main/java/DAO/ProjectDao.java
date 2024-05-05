package DAO;

import hibernate.HibernateUtil;

import models.Project;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class ProjectDao {

    public List<Project> getAllProjects() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Project";
            Query<Project> query = session.createQuery(hql, Project.class);
            return query.list();
        } catch (Exception e) {e.printStackTrace();return null;}}

    public Project getProjectById(Long projectId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Project.class, projectId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public void saveProject(Project project) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            System.out.print(project);
            session.save(project);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions
        }
    }

    public void deleteProjectById(Long projectId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // Load the Employee entity by ID
            Project project = session.load(Project.class, projectId);
            if (project != null) {
                session.delete(project);
                tx.commit();
            } else {
                // Handle case when employee with given ID is not found
                throw new IllegalArgumentException("Project with ID " + projectId + " not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateProject(Project updatedProject) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.update(updatedProject);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
