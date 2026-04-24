package model;

public class LaneStatus {
    private int laneNumber;
    private boolean blocked;
    private boolean priority;
    private int vehicleCount;
    private int averageSpeed;

    public LaneStatus(int laneNumber, boolean blocked, boolean priority, int vehicleCount, int averageSpeed) {
        this.laneNumber = laneNumber;
        this.blocked = blocked;
        this.priority = priority;
        this.vehicleCount = vehicleCount;
        this.averageSpeed = averageSpeed;
    }

    public int getLaneNumber() {
        return laneNumber;
    }

    public void setLaneNumber(int laneNumber) {
        this.laneNumber = laneNumber;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public int getVehicleCount() {
        return vehicleCount;
    }

    public void setVehicleCount(int vehicleCount) {
        this.vehicleCount = vehicleCount;
    }

    public int getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(int averageSpeed) {
        this.averageSpeed = averageSpeed;
    }
}
