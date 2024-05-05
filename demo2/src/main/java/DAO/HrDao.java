package DAO;

import hibernate.HibernateUtil;
import models.Hr;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class HrDao {
    public boolean isValidUser(String username, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Hibernate query to check if a user with the given username and password exists.
            String hql = "FROM Hr WHERE username = :username AND password = :password";
            Query<Hr> query = session.createQuery(hql, Hr.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            // Return true if a user is found, false otherwise.
            return query.uniqueResult() != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
