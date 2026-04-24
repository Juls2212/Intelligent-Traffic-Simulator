package model;

public class RoadStatus {
    private String stateName;
    private String spanishStateName;
    private String description;
    private String activeStateClass;
    private String lastActionTrace;
    private int averageSpeed;
    private String congestionLevel;
    private boolean accidentActive;

    public RoadStatus(String stateName, String spanishStateName, String description,
                      String activeStateClass, String lastActionTrace,
                      int averageSpeed, String congestionLevel, boolean accidentActive) {
        this.stateName = stateName;
        this.spanishStateName = spanishStateName;
        this.description = description;
        this.activeStateClass = activeStateClass;
        this.lastActionTrace = lastActionTrace;
        this.averageSpeed = averageSpeed;
        this.congestionLevel = congestionLevel;
        this.accidentActive = accidentActive;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getSpanishStateName() {
        return spanishStateName;
    }

    public void setSpanishStateName(String spanishStateName) {
        this.spanishStateName = spanishStateName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActiveStateClass() {
        return activeStateClass;
    }

    public void setActiveStateClass(String activeStateClass) {
        this.activeStateClass = activeStateClass;
    }

    public String getLastActionTrace() {
        return lastActionTrace;
    }

    public void setLastActionTrace(String lastActionTrace) {
        this.lastActionTrace = lastActionTrace;
    }

    public int getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(int averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public String getCongestionLevel() {
        return congestionLevel;
    }

    public void setCongestionLevel(String congestionLevel) {
        this.congestionLevel = congestionLevel;
    }

    public boolean isAccidentActive() {
        return accidentActive;
    }

    public void setAccidentActive(boolean accidentActive) {
        this.accidentActive = accidentActive;
    }
}
