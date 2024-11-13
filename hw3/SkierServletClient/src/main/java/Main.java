
import PostSkiersEndpointTest.PostSkiersEndpointTest;
import ResultHandaling.ResultHandler;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        PostSkiersEndpointTest.startTest();

        ResultHandler resultHandler = new ResultHandler("result.csv");
        resultHandler.showResult();
    }
}

