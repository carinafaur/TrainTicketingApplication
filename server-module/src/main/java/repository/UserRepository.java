package repository;

import data.HibernateUtils;
import domain.User;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class UserRepository {
    public UserRepository() {}

    public User findUserByUsername(String username) {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE username = :username", User.class);
            query.setParameter("username", username);
            return query.uniqueResult();
        }
    }
}
