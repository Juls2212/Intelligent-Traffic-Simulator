package model;

public class TrafficLight {
    private String id;
    private int lane;
    private String color;
    private int remainingSeconds;

    public TrafficLight(String id, int lane, String color, int remainingSeconds) {
        this.id = id;
        this.lane = lane;
        this.color = color;
        this.remainingSeconds = remainingSeconds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLane() {
        return lane;
    }

    public void setLane(int lane) {
        this.lane = lane;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(int remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
}
