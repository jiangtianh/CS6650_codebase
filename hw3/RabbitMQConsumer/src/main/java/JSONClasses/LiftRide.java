package JSONClasses;

public class LiftRide {
    private final int time;
    private final int liftID;

    public LiftRide(int time, int liftID) {
        this.time = time;
        this.liftID = liftID;
    }

    public int getTime() {
        return time;
    }

    public int getLiftID() {
        return liftID;
    }
}
