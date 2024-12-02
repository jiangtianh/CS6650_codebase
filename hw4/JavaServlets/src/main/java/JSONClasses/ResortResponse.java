package JSONClasses;

public class ResortResponse {
    private int resortId;
    private long numSkiers;

    public ResortResponse(int resortId, long numSkiers) {
        this.resortId = resortId;
        this.numSkiers = numSkiers;
    }

    public int getResort() {
        return resortId;
    }
    public long getNumSkiers() {
        return numSkiers;
    }

}
