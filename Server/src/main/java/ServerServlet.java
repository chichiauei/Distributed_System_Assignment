import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@WebServlet(name = "ServerServlet", value = "/swipe/*")
public class ServerServlet extends HttpServlet {


    private static final String QUEUE_NAME = "my_queue";
    private static final String RMQ_USERNAME = "SwipeQueue";
    private static final String RMQ_PASSWORD = "123456";
    private static final String RABBITMQ_HOST = "35.90.11.11";
    private static final int RABBITMQ_PORT = 5672;
    private static final int NumOfChannels = 10;
    private Connection connection;
    private BlockingQueue<Channel> channelPool;




    public void init() throws ServletException {
        super.init();
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBITMQ_HOST);
//            factory.setPort(RABBITMQ_PORT);
//            factory.setUsername(RMQ_USERNAME);
//            factory.setPassword(RMQ_PASSWORD);
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
        else if("/right".equals(url)) swipe = "right";

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
            System.out.println(sb.toString());
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
