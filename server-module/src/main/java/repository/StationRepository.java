package repository;

import data.HibernateUtils;
import domain.Station;
import java.util.List;

public class StationRepository {
    public StationRepository() {
    }

    public Station findStationById(int id) {
        return HibernateUtils.getSessionFactory().openSession().get(Station.class, id);
    }

    public List<Station> getAllStations() {
        return HibernateUtils.getSessionFactory().openSession().createQuery("from Station",Station.class).list();
    }
}
