package repository;

import data.HibernateUtils;
import domain.Station;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class StationRepository {
    public StationRepository() {}

    public Station findStationById(int id) {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            return session.get(Station.class, id);
        }
    }

    public List<Station> getAllStations() {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            return session.createQuery("from Station", Station.class).list();
        }
    }

    public Station findByNameAndCity(String name, String city) {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Station WHERE stationName = :n AND stationCity = :c",
                            Station.class)
                    .setParameter("n", name)
                    .setParameter("c", city)
                    .uniqueResult();
        }
    }

    public Station save(Station station) {
        Transaction tx = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(station);
            tx.commit();
            return station;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public Station update(Station station) {
        Transaction tx = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Station merged = session.merge(station);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public Station delete(Station station) {
        Transaction tx = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Station managed = session.contains(station) ? station : session.merge(station);
            session.remove(managed);
            tx.commit();
            return station;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}
