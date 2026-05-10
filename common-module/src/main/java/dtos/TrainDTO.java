package dtos;

import java.io.Serializable;

public class TrainDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String trainNumber;
    private int capacity;

    public TrainDTO() {}

    public TrainDTO(int id, String trainNumber, int capacity) {
        this.id = id;
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
