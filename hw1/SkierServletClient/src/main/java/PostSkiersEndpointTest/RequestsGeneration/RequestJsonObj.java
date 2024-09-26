package PostSkiersEndpointTest.RequestsGeneration;

public class RequestJsonObj {
    private final int time;
    private final int liftID;

    public RequestJsonObj(int time, int liftID) {
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
