package JSONClasses;

public class LiftRidePostRequest {
    private int resortID;
    private int seasonID;
    private int dayID;
    private int skierID;
    private LiftRide liftRide;

    public LiftRidePostRequest(int resortID, int seasonID, int dayID, int skierID, LiftRide liftRide) {
        this.resortID = resortID;
        this.seasonID = seasonID;
        this.dayID = dayID;
        this.skierID = skierID;
        this.liftRide = liftRide;
    }

    // Getters
    public int getResortID() { return resortID; }
    public int getSeasonID() { return seasonID; }
    public int getDayID() { return dayID; }
    public int getSkierID() { return skierID; }
    public LiftRide getLiftRide() { return liftRide; }
}