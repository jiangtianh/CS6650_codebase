import JSONClasses.ErrorMessage;
import JSONClasses.ResortResponse;
import com.google.gson.Gson;
import org.json.HTTPTokener;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@WebServlet(value = "/resorts/*")
public class ResortServlet extends HttpServlet {

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

        if (urlParts.length == 7 && Objects.equals(urlParts[2], "seasons") && Objects.equals(urlParts[4], "day") && Objects.equals(urlParts[6], "skiers")) {
            // GET/resorts/{resortID}/seasons/{seasonID}/day/{dayID}/skiers
            try {
                int resortId = Integer.parseInt(urlParts[1]);
                int seasonId = Integer.parseInt(urlParts[3]);
                int dayId = Integer.parseInt(urlParts[5]);

                try (Jedis jedis = RedisConnectionPool.getJedis()) {
                    if (jedis == null) {
                        sendErrorResponse(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to get Redis connection");
                    }

                    String setKey = "resort:" + resortId + ":season:" + seasonId + ":day:" + dayId;
                    long setSize = jedis.scard(setKey);

                    // If no data found, scard will return 0
                    if (setSize == 0) {
                        sendErrorResponse(res, HttpServletResponse.SC_NOT_FOUND, "No data found");
                        return;
                    }

                    String jsonResponse = new Gson().toJson(new ResortResponse(resortId, setSize));

                    res.setStatus(HttpServletResponse.SC_OK);
                    res.getWriter().write(jsonResponse);

                } catch (Exception e) {
                    System.out.println("Error at GET /resorts/{resortID}/seasons/{seasonID}/day/{dayID}/skiers");
                    e.printStackTrace();
                }
            } catch (NumberFormatException e) {
            sendErrorResponse(res, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL parameters");
            return;
        }
        } else {
            sendErrorResponse(res, HttpServletResponse.SC_NOT_FOUND, "Invalid URL");
        }
    }



    private void sendErrorResponse(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.getWriter().write(new Gson().toJson(new ErrorMessage(message)));
    }
}
