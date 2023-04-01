import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;


@WebServlet(name = "MatchServlet", value = "/matches/*")

public class MatchServlet extends HttpServlet {
    private SwipeDaoImpl swipeDao;
    private static final String RABBITMQ_HOST = "localhost";
    private com.rabbitmq.client.Connection connection;
    private BlockingQueue<Channel> channelPool;
    private static final int NumOfChannels = 10;

    public void init() throws ServletException {
        super.init();
        swipeDao = new SwipeDaoImpl();
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBITMQ_HOST);
            connection = factory.newConnection();
            // Create a fixed number of channels and add them to the pool
            channelPool = new ArrayBlockingQueue<>(NumOfChannels);
            for (int i = 0; i < NumOfChannels; i++) {
                Channel channel = connection.createChannel();
                channelPool.offer(channel);
            }
        } catch (IOException | TimeoutException e) {
            throw new ServletException("Failed to initialize RabbitMQ connection", e);
        }

    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Extract userId from the URL path
        String pathInfo = request.getPathInfo();
        String userId = pathInfo.substring(pathInfo.lastIndexOf("/") + 1);


        // Query the database for matches for the user
        List<String> matches = getMatchesForUser(userId);

        // Prepare the response
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Create a JSON object with the required information
        JsonObject result = new JsonObject();
        result.addProperty("userId", userId);
        JsonArray matchesArray = new JsonArray();
        for (String match : matches) {
            matchesArray.add(new JsonPrimitive(match));
        }
        result.add("matches", matchesArray);
        out.println(result.toString());
        out.close();

    }

    public List<String> getMatchesForUser(String userId){
        return swipeDao.getMatchesForUser(userId);
    }


}
