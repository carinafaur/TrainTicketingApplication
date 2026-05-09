package repository;

import data.HibernateUtils;
import domain.Booking;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class BookingRepository {
    public BookingRepository() {}

    public Booking save(Booking booking) {
        Transaction tx = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(booking);
            tx.commit();
            return booking;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public Booking findById(int id) {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT b FROM Booking b " +
                                    "LEFT JOIN FETCH b.user " +
                                    "LEFT JOIN FETCH b.schedule s " +
                                    "LEFT JOIN FETCH s.train " +
                                    "LEFT JOIN FETCH b.startStation " +
                                    "LEFT JOIN FETCH b.endStation " +
                                    "WHERE b.id = :id",
                            Booking.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }

    public List<Booking> findAll() {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT b FROM Booking b " +
                                    "LEFT JOIN FETCH b.user " +
                                    "LEFT JOIN FETCH b.schedule s " +
                                    "LEFT JOIN FETCH s.train " +
                                    "LEFT JOIN FETCH b.startStation " +
                                    "LEFT JOIN FETCH b.endStation " +
                                    "ORDER BY b.bookingDate DESC",
                            Booking.class)
                    .list();
        }
    }

    public List<Booking> findBySchedule(int scheduleId) {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT b FROM Booking b " +
                                    "LEFT JOIN FETCH b.startStation " +
                                    "LEFT JOIN FETCH b.endStation " +
                                    "WHERE b.schedule.id = :id",
                            Booking.class)
                    .setParameter("id", scheduleId)
                    .list();
        }
    }

    public List<Booking> findByUsername(String username) {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT b FROM Booking b " +
                                    "LEFT JOIN FETCH b.user u " +
                                    "LEFT JOIN FETCH b.schedule s " +
                                    "LEFT JOIN FETCH s.train " +
                                    "LEFT JOIN FETCH b.startStation " +
                                    "LEFT JOIN FETCH b.endStation " +
                                    "WHERE u.username = :u " +
                                    "ORDER BY b.bookingDate DESC",
                            Booking.class)
                    .setParameter("u", username)
                    .list();
        }
    }
}
