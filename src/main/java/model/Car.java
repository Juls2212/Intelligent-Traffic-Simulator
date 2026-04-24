package model;

public class Car {
    private String id;
    private int lane;
    private int xPosition;
    private int speed;
    private boolean blocked;

    public Car(String id, int lane, int xPosition, int speed, boolean blocked) {
        this.id = id;
        this.lane = lane;
        this.xPosition = xPosition;
        this.speed = speed;
        this.blocked = blocked;
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

    public int getXPosition() {
        return xPosition;
    }

    public void setXPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
