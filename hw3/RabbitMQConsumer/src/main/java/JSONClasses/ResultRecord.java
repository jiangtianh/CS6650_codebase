package JSONClasses;

public class ResultRecord {
    private final long startTimeStamp;
    private final long endTimeStamp;

    public ResultRecord(long startTimeStamp, long endTimeStamp) {
        this.startTimeStamp = startTimeStamp;
        this.endTimeStamp = endTimeStamp;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }
}
