-- Train Ticketing Application — database schema
-- Run with: psql train_tickets < scripts/schema.sql
-- Requires PostgreSQL 14+.

CREATE TABLE users (
    user_id     SERIAL PRIMARY KEY,
    username    VARCHAR(30)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    user_email  VARCHAR(100),
    user_role   VARCHAR(20)  NOT NULL CHECK (user_role IN ('ADMIN', 'CUSTOMER'))
);

CREATE TABLE stations (
    station_id   SERIAL PRIMARY KEY,
    station_city VARCHAR(50) NOT NULL,
    station_name VARCHAR(50) NOT NULL,
    UNIQUE (station_city, station_name)
);

CREATE TABLE routes (
    route_id      SERIAL PRIMARY KEY,
    start_station INTEGER NOT NULL REFERENCES stations(station_id),
    end_station   INTEGER NOT NULL REFERENCES stations(station_id),
    CHECK (start_station <> end_station)
);

CREATE TABLE trains (
    train_id       SERIAL PRIMARY KEY,
    train_number   VARCHAR(20) NOT NULL UNIQUE,
    train_capacity INTEGER     NOT NULL CHECK (train_capacity > 0)
);

CREATE TABLE schedules (
    schedule_id    SERIAL PRIMARY KEY,
    train_id       INTEGER     NOT NULL REFERENCES trains(train_id),
    route_id       INTEGER     NOT NULL REFERENCES routes(route_id),
    departure_time TIMESTAMP   NOT NULL,
    arrival_time   TIMESTAMP   NOT NULL,
    delay_minutes  INTEGER     NOT NULL DEFAULT 0,
    status         VARCHAR(20) NOT NULL
                    CHECK (status IN ('ON_TIME', 'DELAYED', 'CANCELLED'))
);

CREATE TABLE schedule_stops (
    stop_id        SERIAL PRIMARY KEY,
    schedule_id    INTEGER NOT NULL REFERENCES schedules(schedule_id) ON DELETE CASCADE,
    station_id     INTEGER NOT NULL REFERENCES stations(station_id),
    stop_order     INTEGER NOT NULL,
    arrival_time   TIMESTAMP,
    departure_time TIMESTAMP,
    UNIQUE (schedule_id, stop_order),
    UNIQUE (schedule_id, station_id),
    CHECK (arrival_time IS NULL OR departure_time IS NULL OR arrival_time <= departure_time)
);

CREATE INDEX idx_schedule_stops_schedule ON schedule_stops(schedule_id);

CREATE TABLE bookings (
    booking_id       SERIAL PRIMARY KEY,
    user_id          INTEGER   NOT NULL REFERENCES users(user_id),
    schedule_id      INTEGER   NOT NULL REFERENCES schedules(schedule_id),
    start_station_id INTEGER   NOT NULL REFERENCES stations(station_id),
    end_station_id   INTEGER   NOT NULL REFERENCES stations(station_id),
    seats_reserved   INTEGER   NOT NULL CHECK (seats_reserved > 0),
    booking_date     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_bookings_schedule ON bookings(schedule_id);
CREATE INDEX idx_bookings_user     ON bookings(user_id);
