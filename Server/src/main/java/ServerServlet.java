import com.google.gson.*;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
//import java.sql.Connection;
import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import static java.lang.System.out;

@WebServlet(name = "ServerServlet", value = "/swipe/*")
public class ServerServlet extends HttpServlet {


    private static final String QUEUE_NAME = "my_queue";
    private static final String RMQ_USERNAME = "SwipeQueue";
    private static final String RMQ_PASSWORD = "123456";
//    localhost
    private static final String RABBITMQ_HOST = "localhost";
    private static final int RABBITMQ_PORT = 5672;
    private static final int NumOfChannels = 10;
    private com.rabbitmq.client.Connection connection;
    private BlockingQueue<Channel> channelPool;
//    private SwipeDao swipeDao;
//
    private SwipeDaoImpl swipeDao;
    private static final String JDBC_URL = "jdbc:mysql://database-2.cmluuqszwcgn.us-west-2.rds.amazonaws.com:3306/SwipeData?user=admin&password=admin123";
    private static final String JDBC_USERNAME = "admin";
    private static final String JDBC_PASSWORD = "admin123";


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
        } catch (IOException  | TimeoutException e) {
            throw new ServletException("Failed to initialize RabbitMQ connection", e);
        }

    }

//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//
//        // Extract userId from the URL path
//        String pathInfo = request.getPathInfo();
////        String userId = pathInfo.substring(1);
//        String userId = pathInfo.substring(pathInfo.lastIndexOf("/") + 1);
//        // Query the database for matches for the user
////        List<String> matches = getMatchesForUser(userId);
//
//
//        // Query the database for likes and dislikes for the user
//        int numLikes = getNumLikesForUser(userId);
//        int numDislikes = getNumDislikesForUser(userId);
//
//        // Prepare the response
//        response.setContentType("application/json");
//        PrintWriter out = response.getWriter();
//
//        // Create a JSON object with the required information
//        JsonObject result = new JsonObject();
//        result.addProperty("userId", userId);
//        result.addProperty("numLikes", numLikes);
//        result.addProperty("numDislikes", numDislikes);
//        JsonArray matchesArray = new JsonArray();
////        for (String match : matches) {
////            matchesArray.add(new JsonPrimitive(match));
////        }
////        result.add("matches", matchesArray);
//
//        // Write the JSON object to the response
//        out.println(result.toString());
//        out.close();
//
//    }


    private int getNumLikesForUser(String userId) {
        // Query database to retrieve number of likes for the user
        // Return as integer
        // You can use the SELECT statement with a COUNT operation to retrieve number of likes
//        int numlikes = 0;
//        try {
//            java.sql.Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
//            Statement statement = connection.createStatement();
//            String query = "SELECT COUNT(*) AS num_likes FROM SwipeData WHERE swiper='" + userId + "' AND swipe='right'";
//
//            ResultSet resultSet = statement.executeQuery(query);
//            if (resultSet.next()) {
//                numlikes = resultSet.getInt("num_likes");
//            }
//            statement.close();
//            connection.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return numlikes;
        return swipeDao.getNumLikesForUser(userId);
    }

    private int getNumDislikesForUser(String userId) {
        // Query database to retrieve number of dislikes for the user
        // Return as integer
        // You can use the SELECT statement with a COUNT operation to retrieve number of dislikes
//        int numDislikes = 0;
//        try {
//            java.sql.Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
//            Statement statement = connection.createStatement();
//            String query = "SELECT COUNT(*) AS num_dislikes FROM SwipeData WHERE swiper='" + userId + "' AND swipe='left'";
//
//            ResultSet resultSet = statement.executeQuery(query);
//            if (resultSet.next()) {
//                numDislikes = resultSet.getInt("num_dislikes");
//            }
//            statement.close();
//            connection.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return numDislikes;
        return swipeDao.getNumDislikesForUser(userId);
    }


    private List<String> getMatchesForUser(String userId) {
        // Query database to retrieve up to 100 matches for the user
        // Return as list of strings
        // You can use the SELECT statement with a JOIN operation to retrieve matches
        List<String> matches = new ArrayList<String>();
        try {
            java.sql.Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
            Statement statement = connection.createStatement();
            String query = "SELECT swipee FROM SwipeData WHERE swiper='" + userId + "' AND swipe = 'right' LIMIT 100";
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                matches.add(resultSet.getString("swipee"));
            }
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return matches;
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        Gson gson = new Gson();

        StringBuilder sb = new StringBuilder();
        String s;

        String url = request.getPathInfo();
        while ((s = request.getReader().readLine()) != null) {
            sb.append(s);
        }

        String swipe = "";
        if("/left/".equals(url)) swipe = "left";
        else if("/right/".equals(url)) swipe = "right";

        String[] urlDetails = url.split("/");

        JsonObject swipeDetails = gson.fromJson(sb.toString(),JsonObject.class);

//        String leftOrRight = urlDetails[1];
//
//        swipeDetails.addProperty("swipe",leftOrRight);
//        String details = gson.toJson(swipeDetails);
        Request formatRequest = null;

        try{
            Request req = (Request) gson.fromJson(sb.toString(), Request.class);

            if(req.getSwiper() == null || req.getSwipee() == null || req.getComment() == null){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Missing required attribute in JSON payload");
                return;
            }

            if (!isUrlValid(req)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                // do any sophisticated processing with urlParts which contains all the url params
                // TODO: process url params in `urlParts`
                response.getWriter().write("It works!");
            }

            formatRequest = req;


            try (java.sql.Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
                String insertSql = "INSERT INTO TempStore (swiper, swipee, comment, swipe) VALUES (?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(insertSql);
                statement.setString(1, req.getSwiper());
                statement.setString(2, req.getSwipee());
                statement.setString(3, req.getComment());
                statement.setString(4, swipe);
                statement.executeUpdate();
                response.setStatus(200);
                System.out.println("{\"message\": \"Swipe event written to TempStore\"}");
            } catch (SQLException ex) {
                response.setStatus(500);
                System.out.println("{\"message\": \"Failed to write Swipe event to TempStore\"}");
                ex.printStackTrace();
            }


        } catch (JsonSyntaxException e){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid JSON payload.");
        }




        try {
            // Take a channel from the pool and use it to publish the message
            Channel channel = channelPool.take();
            int index = sb.lastIndexOf("}");
            if(index != -1){
                sb.insert(index,",\"swipe\":\"" + swipe + "\"");
            }
            out.println(sb.toString());
            channel.basicPublish("", QUEUE_NAME, null, sb.toString().getBytes());
            channelPool.offer(channel); // Add the channel back to the pool

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Request data sent to remote queue");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to send Request data to remote queue: " + e.getMessage());
        }

    }

    public void destroy() {
        try {
            for (Channel channel : channelPool) {
                channel.close();
            }
            connection.close();
        } catch (IOException | TimeoutException e) {
            log("Failed to close RabbitMQ connection", e);
        }
    }


    private boolean isUrlValid(Request req) {
        if (Integer.valueOf(req.getSwiper()) < 1 || Integer.valueOf(req.getSwiper()) > 5000) {
            return false;
        }
        if (Integer.valueOf(req.getSwipee()) < 1 || Integer.valueOf(req.getSwipee()) > 1000000) {
            return false;
        }
        if (req.getComment().length() > 256) {
            return false;
        }
        return true;
    }
}
