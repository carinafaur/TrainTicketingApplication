package repository;

import data.HibernateUtils;
import domain.Route;
import domain.Station;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class RouteRepository {
    public RouteRepository() {}

    public List<Route> getAllRoutes() {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            Query<Route> query = session.createQuery("FROM Route", Route.class);
            return query.list();
        }
    }

    public Route save(Route route) {
        Transaction transaction = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(route);
            transaction.commit();
            return route;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public Route update(Route route) {
        Transaction transaction = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(route);
            transaction.commit();
            return route;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public Route delete(Route route) {
        Transaction transaction = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(session.contains(route) ? route : session.merge(route));
            transaction.commit();
            return route;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public Route findByStations(Station startStation, Station endStation) {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            return session.createQuery("FROM Route WHERE startStation = :start AND endStation = :end", Route.class)
                    .setParameter("start", startStation)
                    .setParameter("end", endStation)
                    .uniqueResult();
        }
    }

    public Route findByRouteId(Integer routeId) {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            return session.get(Route.class, routeId);
        }
    }

}
