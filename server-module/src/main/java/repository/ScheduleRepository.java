package repository;

import data.HibernateUtils;
import domain.Route;
import domain.Schedule;
import domain.ScheduleStop;
import domain.Station;
import domain.Train;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ScheduleRepository {
    public ScheduleRepository() {}

    public List<Schedule> getAllSchedules() {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT s FROM Schedule s " +
                                    "LEFT JOIN FETCH s.train " +
                                    "LEFT JOIN FETCH s.route r " +
                                    "LEFT JOIN FETCH r.startStation " +
                                    "LEFT JOIN FETCH r.endStation " +
                                    "LEFT JOIN FETCH s.stops st " +
                                    "LEFT JOIN FETCH st.station " +
                                    "ORDER BY s.departureTime",
                            Schedule.class)
                    .list();
        }
    }

    public Schedule findById(int id) {
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            return session.get(Schedule.class, id);
        }
    }

    public Schedule save(Schedule schedule) {
        Transaction tx = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(schedule);
            tx.commit();
            return schedule;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public Schedule update(Schedule incoming) {
        Transaction tx = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Schedule managed = session.get(Schedule.class, incoming.getId());
            if (managed == null) {
                throw new IllegalStateException(
                        "Schedule with id " + incoming.getId() + " no longer exists.");
            }

            if (incoming.getTrain() != null) {
                managed.setTrain(session.get(Train.class, incoming.getTrain().getId()));
            }
            if (incoming.getRoute() != null) {
                managed.setRoute(session.get(Route.class, incoming.getRoute().getId()));
            }

            managed.setDepartureTime(incoming.getDepartureTime());
            managed.setArrivalTime(incoming.getArrivalTime());
            managed.setDelayMinutes(incoming.getDelayMinutes());
            managed.setStatus(incoming.getStatus());

            managed.getStops().clear();
            session.flush();

            for (ScheduleStop newStop : incoming.getStops()) {
                Station st = session.get(Station.class, newStop.getStation().getId());
                ScheduleStop attached = new ScheduleStop(
                        st,
                        newStop.getStopOrder(),
                        newStop.getArrivalTime(),
                        newStop.getDepartureTime()
                );
                attached.setSchedule(managed);
                managed.getStops().add(attached);
            }

            tx.commit();
            return managed;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public Schedule delete(Schedule schedule) {
        Transaction tx = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Schedule managed = session.contains(schedule) ? schedule : session.merge(schedule);
            session.remove(managed);
            tx.commit();
            return schedule;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}
