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


@WebServlet(name = "StatServlet", value = "/stats/*")
public class StatServlet extends HttpServlet {
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


        // Query the database for likes and dislikes for the user
        int numLikes = getNumLikesForUser(userId);
        int numDislikes = getNumDislikesForUser(userId);

        // Prepare the response
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Create a JSON object with the required information
        JsonObject result = new JsonObject();
        result.addProperty("userId", userId);
        result.addProperty("numLikes", numLikes);
        result.addProperty("numDislikes", numDislikes);
        out.println(result.toString());
        out.close();

    }

    private int getNumLikesForUser(String userId) {
        // Query database to retrieve number of likes for the user
        // Return as integer
        // You can use the SELECT statement with a COUNT operation to retrieve number of likes
        return swipeDao.getNumLikesForUser(userId);
    }

    private int getNumDislikesForUser(String userId) {
        // Query database to retrieve number of likes for the user
        // Return as integer
        // You can use the SELECT statement with a COUNT operation to retrieve number of likes
        return swipeDao.getNumDislikesForUser(userId);
    }
}