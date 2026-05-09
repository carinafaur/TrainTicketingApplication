package repository;

import data.HibernateUtils;
import domain.Train;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class TrainRepository {
    public TrainRepository() {}

    public List<Train> getAllTrains() {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            Query<Train> query = session.createQuery("FROM Train", Train.class);
            return query.list();
        }
    }

    public Train findById(int id) {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            return session.get(Train.class, id);
        }
    }

    public Train findByTrainNumber(String trainNumber) {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            return session.createQuery("FROM Train WHERE trainNumber = :n", Train.class)
                    .setParameter("n", trainNumber)
                    .uniqueResult();
        }
    }

    public Train save(Train train) {
        Transaction transaction = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(train);
            transaction.commit();
            return train;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public Train update(Train train) {
        Transaction transaction = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Train merged = session.merge(train);
            transaction.commit();
            return merged;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public Train delete(Train train) {
        Transaction transaction = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(session.contains(train) ? train : session.merge(train));
            transaction.commit();
            return train;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }
}
