import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Consumer {
    private final static String QUEUE_NAME = "my_queue";
    //52.36.77.145
    private final static String HOST_NAME = "localhost";
    private final static int numThreads = 100;
    private static final ConcurrentHashMap<String, BlockingDeque<String>> matches = new ConcurrentHashMap<>();

//    private static String url = "jdbc:mysql://database-2.cmluuqszwcgn.us-west-2.rds.amazonaws.com:3306/sys?user=admin&password=admin123";
//    private static String username = "admin";
//    private static String password = "admin123";
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // number of threads to use for consuming messages
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    channel.basicConsume(QUEUE_NAME, true, (consumerTag, delivery) -> {
                        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
//                        System.out.println(message);
                        handleMessage(message);
                    }, consumerTag -> {});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

        private static void handleMessage(String message) {
            // Parse message into User object
            swipeData data = parseUser(message);

            // Update userMap based on message
            updateMap(data);
        }

        private static swipeData parseUser(String message) {
            // Implement parsing logic here
            // Return User object
            Gson gson = new Gson();
            swipeData data = (swipeData) gson.fromJson(message, swipeData.class);
            ConsumerDao consumerDao = new ConsumerDao();
            consumerDao.createSwipeData(new swipeData(data.getSwiper(), data.getSwipee(), data.getSwipe()));
            return data;
        }

        private static <PreparedStatement> void updateMap(swipeData data) {
            // Update userMap based on user object
            // Thread-safe operations should be used


            String id = data.getSwipee();
            if(!matches.containsKey(id)){
                matches.put(id,new LinkedBlockingDeque<>());
            }
            //计数right的数量，或者提前设立list的长度为100，然后此时list的长度大于100，不进行任何操作
            if(matches.get(id).size() > 100){
                return;
            }
            else{
                if(data.getSwipe() == "right"){
                    matches.get(id).push(data.getSwipee());



                }
            }
//            if(matches.get(id).size() < 100){
//                if(Objects.equals(data.getSwipe(), "right")){
//                    matches.get(id).push(data.getSwipee());
//                    try (java.sql.Connection conn = DriverManager.getConnection(url, username, password)) {
//                            String sql = "INSERT INTO matches (id, `match`) VALUES (?, ?)";
//                            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
//
//                        stmt.setString(1, id);
//                        for (String match : matches.get(id)) {
//                            stmt.setString(2, match);
//                            stmt.executeUpdate();
//                        }
//                        stmt.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
        }
}


