package JSONClasses;

public class LiftRidePostRequest {
    private final int skierID;
    private final int resortID;
    private final int seasonID;
    private final int dayID;
    private final LiftRide liftRide;

    public LiftRidePostRequest(int skierId, int resortId, int seasonId, int dayId, LiftRide liftRide) {
        this.skierID = skierId;
        this.resortID = resortId;
        this.seasonID = seasonId;
        this.dayID = dayId;
        this.liftRide = liftRide;
    }

    // Getters
    public int getSkierId() { return skierID; }
    public int getResortId() { return resortID; }
    public int getSeasonId() { return seasonID; }
    public int getDayId() { return dayID; }
    public LiftRide getLiftRide() { return liftRide; }
}