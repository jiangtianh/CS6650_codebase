package JSONClasses;

public class LiftRidePostRequest {
    private final int skierId;
    private final int resortId;
    private final int seasonId;
    private final int dayId;
    private final int liftId;
    private final int time;

    public LiftRidePostRequest(int skierId, int resortId, int seasonId, int dayId, int liftId, int time) {
        this.skierId = skierId;
        this.resortId = resortId;
        this.seasonId = seasonId;
        this.dayId = dayId;
        this.liftId = liftId;
        this.time = time;
    }

    // Getters
    public int getSkierId() { return skierId; }
    public int getResortId() { return resortId; }
    public int getSeasonId() { return seasonId; }
    public int getDayId() { return dayId; }
    public int getLiftId() { return liftId; }
    public int getTime() { return time; }

    @Override
    public String toString() {
        return "LiftRidePostRequest{" +
                "skierId=" + skierId +
                ", resortId=" + resortId +
                ", seasonId=" + seasonId +
                ", dayId=" + dayId +
                ", liftId=" + liftId +
                ", time=" + time +
                '}';
    }
}