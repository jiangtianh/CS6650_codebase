package ResultHandaling;

public class ResultRecord {
    private final long startTimeStamp;
    private final int Latency;
    private final String requestType;
    private final int responseCode;

    public ResultRecord(long startTimeStamp, int Latency, String requestType, int responseCode) {
        this.startTimeStamp = startTimeStamp;
        this.Latency = Latency;
        this.requestType = requestType;
        this.responseCode = responseCode;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public int getLatency() {
        return Latency;
    }

    public String getRequestType() {
        return requestType;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
