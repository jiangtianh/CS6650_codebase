import JSONClasses.ErrorMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import JSONClasses.LiftRide;
import JSONClasses.LiftRidePostRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import redis.clients.jedis.Jedis;


@WebServlet(value = "/skiers/*")
public class SkierServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            sendErrorResponse(res, HttpServletResponse.SC_BAD_REQUEST, "missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (urlParts.length == 8) {
            // Handle /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
            if (!isValidUrl(urlParts)) {
                sendErrorResponse(res, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL");
                return;
            }
            if (!areUrlParametersValid(urlParts)) {
                sendErrorResponse(res, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL parameters");
                return;
            }

            // Get vertical for the skier
            try (Jedis jedis = RedisConnectionPool.getJedis()) {
                if (jedis == null) {
                    sendErrorResponse(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to get Redis connection");
                }
                String skierSeasonKey = String.format("skier:%s:resort:%s:season:%s:day:%s", urlParts[7], urlParts[1], urlParts[3], urlParts[5]);

                List<String> liftIds = jedis.lrange(skierSeasonKey, 0, -1);

                if (liftIds.isEmpty()) {
                   sendErrorResponse(res, HttpServletResponse.SC_NOT_FOUND, "Data not found");
                   return;
                }

                int sum = 0;
                for (String liftId : liftIds) {
                   try {
                       sum += Integer.parseInt(liftId);
                   } catch (NumberFormatException e) {
                       System.err.println("Invalid number format in liftId: " + liftId);
                   }
                }
                int result = sum * 10;
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().write(String.valueOf(result));

            } catch (Exception e) {
                System.out.println("Error at GET /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}");
                e.printStackTrace();
            }

        } else if (urlParts.length == 3 && urlParts[2].equals("vertical")) {
            // Handle /skiers/{skierID}/vertical
            try {
                int skierID = Integer.parseInt(urlParts[1]);
            } catch (NumberFormatException e) {
                sendErrorResponse(res, HttpServletResponse.SC_BAD_REQUEST, "Invalid skierID");
                return;
            }

            String resort = req.getParameter("resort");
            String season = req.getParameter("season");

            if (resort == null || resort.isEmpty()) {
                sendErrorResponse(res, HttpServletResponse.SC_BAD_REQUEST, "missing resort parameter");
                return;
            }

            try (Jedis jedis = RedisConnectionPool.getJedis()) {
                if (jedis == null) {
                    sendErrorResponse(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to get Redis connection");
                    return;
                }

                String skierResortKey = String.format("skier:%s:resort:%s", urlParts[1], resort);
                JsonObject responseJson = new JsonObject();
                JsonArray resortsArray = new JsonArray();


                if (season != null && !season.isEmpty()) {
                    // Case: Season is provided
                    try {
                        int seasonId = Integer.parseInt(season);
                    } catch (NumberFormatException e) {
                        sendErrorResponse(res, HttpServletResponse.SC_BAD_REQUEST, "Invalid season parameter");
                        return;
                    }

                    String seasonField = "season:" + season;
                    String vertical = jedis.hget(skierResortKey, seasonField);

                    if (vertical == null) {
                        sendErrorResponse(res, HttpServletResponse.SC_NOT_FOUND, "Data not found");
                        return;
                    }

                    JsonObject seasonData = new JsonObject();
                    seasonData.addProperty("seasonID", season);
                    seasonData.addProperty("totalVert", Integer.parseInt(vertical));
                    resortsArray.add(seasonData);

                } else {
                    // Case: season not provided, return all season data
                    Map<String, String> seasonData = jedis.hgetAll(skierResortKey);

                    if (seasonData.isEmpty()) {
                        sendErrorResponse(res, HttpServletResponse.SC_NOT_FOUND, "Data not found");
                        return;
                    }

                    for (Map.Entry<String, String> entry : seasonData.entrySet()) {
                        String field = entry.getKey();
                        if (field.startsWith("season:")) {
                            String seasonId = field.substring(7);
                            int totalVert = Integer.parseInt(entry.getValue());

                            JsonObject seasonObj = new JsonObject();
                            seasonObj.addProperty("seasonID", seasonId);
                            seasonObj.addProperty("totalVert", totalVert);
                            resortsArray.add(seasonObj);
                        }
                    }
                }

                responseJson.add("resorts", resortsArray);
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().write(responseJson.toString());

            } catch (Exception e) {
                System.err.println("Error at GET /skiers/{skierID}/vertical");
                e.printStackTrace();
                sendErrorResponse(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
            }

        } else {
            sendErrorResponse(res, HttpServletResponse.SC_NOT_FOUND, "Invalid URL");
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo();

        if (urlPath == null || urlPath.isEmpty()) {
            sendErrorResponse(res, HttpServletResponse.SC_NOT_FOUND, "missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");

        // Validate URL
        if (!isValidUrl(urlParts)) {
            sendErrorResponse(res, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL");
            return;
        }
        // Validate URL parameters
        if (!areUrlParametersValid(urlParts)) {
            sendErrorResponse(res, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL parameters");
            return;
        }

        BufferedReader reader = req.getReader();
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }

        if (!isValidLiftRideJson(requestBody.toString())) {
            sendErrorResponse(res, HttpServletResponse.SC_BAD_REQUEST, "Invalid LiftRide JSON");
            return;
        }
        int resortID = Integer.parseInt(urlParts[1]);
        int seasonID = Integer.parseInt(urlParts[3]);
        int dayID = Integer.parseInt(urlParts[5]);
        int skierID = Integer.parseInt(urlParts[7]);
        LiftRide liftRide = new Gson().fromJson(JsonParser.parseString(requestBody.toString()).getAsJsonObject(), LiftRide.class);

        Channel channel = null;
        try {
            RabbitMQChannelPool channelPool = RabbitMQChannelPool.getInstance();
            channel = channelPool.borrowChannel();
            if (channel == null) {
                sendErrorResponse(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No available RabbitMQ channels in the pool");
                return;
            }
            channel.basicPublish("", RabbitMQChannelPool.QUEUE_NAME, null, new Gson().toJson(
                    new LiftRidePostRequest(
                            skierID,
                            resortID,
                            seasonID,
                            dayID,
                            liftRide.getLiftID(),
                            liftRide.getTime()
                    )).getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to publish message to RabbitMQ");
            return;
        } finally {
            if (channel != null) {
                try {
                    RabbitMQChannelPool.getInstance().returnChannel(channel);
                } catch (TimeoutException e) {
                    throw new RuntimeException(e);
                }
            }
        }


        res.setStatus(HttpServletResponse.SC_CREATED);
        res.getWriter().write(new Gson().toJson(liftRide));
    }

    private boolean isValidUrl(String[] urlPath) {
        // urlPath  = "/1/seasons/2019/days/1/skiers/123"
        // Check if the URL format is correct
        return urlPath.length == 8
                && urlPath[2].equals("seasons")
                && urlPath[4].equals("days")
                && urlPath[6].equals("skiers");
    }

    private boolean areUrlParametersValid(String[] urlPath) {
        try {
            int resortID = Integer.parseInt(urlPath[1]);
            int seasonID = Integer.parseInt(urlPath[3]);
            int dayID = Integer.parseInt(urlPath[5]);
            int skierID = Integer.parseInt(urlPath[7]);

            return 1 <= dayID && dayID <= 366;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidLiftRideJson(String requestBody) {
        try {
            JsonObject jsonObject = JsonParser.parseString(requestBody).getAsJsonObject();

            if (jsonObject.size() != 2 || !jsonObject.has("time") || !jsonObject.has("liftID")) {
                return false;
            }

            try {
                int time = jsonObject.get("time").getAsInt();
                int liftId = jsonObject.get("liftID").getAsInt();
            } catch (NumberFormatException | ClassCastException e) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void sendErrorResponse(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.getWriter().write(new Gson().toJson(new ErrorMessage(message)));
    }



}
