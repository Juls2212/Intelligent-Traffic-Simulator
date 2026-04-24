package model;

public class Car {
    private String id;
    private int lane;
    private int xPosition;
    private int speed;
    private boolean blocked;
    private boolean crashed;
    private boolean visible;
    private boolean changingLane;

    public Car(String id, int lane, int xPosition, int speed, boolean blocked) {
        this(id, lane, xPosition, speed, blocked, false, true, false);
    }

    public Car(String id, int lane, int xPosition, int speed, boolean blocked,
               boolean crashed, boolean visible, boolean changingLane) {
        this.id = id;
        this.lane = lane;
        this.xPosition = xPosition;
        this.speed = speed;
        this.blocked = blocked;
        this.crashed = crashed;
        this.visible = visible;
        this.changingLane = changingLane;
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

    public boolean isCrashed() {
        return crashed;
    }

    public void setCrashed(boolean crashed) {
        this.crashed = crashed;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isChangingLane() {
        return changingLane;
    }

    public void setChangingLane(boolean changingLane) {
        this.changingLane = changingLane;
    }
}
