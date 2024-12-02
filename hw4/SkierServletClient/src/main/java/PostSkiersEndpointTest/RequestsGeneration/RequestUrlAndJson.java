package PostSkiersEndpointTest.RequestsGeneration;

import com.google.gson.Gson;

public class RequestUrlAndJson {
    private final String url;
    private final String json;

    public RequestUrlAndJson(String url, RequestJsonObj json) {
        this.url = url;
        this.json = new Gson().toJson(json);
    }

    public String getUrl() {
        return url;
    }

    public String getJson() {
        return json;
    }
}
