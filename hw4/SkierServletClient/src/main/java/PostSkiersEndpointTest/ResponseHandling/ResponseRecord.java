package PostSkiersEndpointTest.ResponseHandling;

public class ResponseRecord {
    private final long startTimeStamp;
    private final long endTimeStamp;
    private final String requestType;
    private final int responseCode;

    public ResponseRecord(long startTimeStamp, long endTimeStamp, String requestType, int responseCode) {
        this.startTimeStamp = startTimeStamp;
        this.endTimeStamp = endTimeStamp;
        this.requestType = requestType;
        this.responseCode = responseCode;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }

    public String getRequestType() {
        return requestType;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
