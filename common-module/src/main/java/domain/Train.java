package domain;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "trains")
public class Train implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "train_id")
    private int id;

    @Column(name = "train_number", unique = true, length = 20)
    private String trainNumber;

    @Column(name = "train_capacity")
    private int capacity;

    public Train() {}

    public Train(String trainNumber, int capacity) {
        this.trainNumber = trainNumber;
        this.capacity = capacity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    @Override
    public String toString() {
        return trainNumber + " (cap=" + capacity + ")";
    }
}
