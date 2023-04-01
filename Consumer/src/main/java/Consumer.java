import com.google.gson.Gson;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Consumer {
    private final static String QUEUE_NAME = "my_queue";
    private final static String HOST_NAME = "localhost";
    private final static int numThreads = 100;
    private static final ConcurrentHashMap<String, LikeAndDislike> swipeCountMap = new ConcurrentHashMap<>();

    private static final AtomicInteger processedMessageCount = new AtomicInteger(0);
    private static String url = "jdbc:mysql://database-2.cmluuqszwcgn.us-west-2.rds.amazonaws.com:3306/sys?user=admin&password=admin123";
    private static String username = "admin";
    private static String password = "admin123";


    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // number of threads to use for consuming messages
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);


        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    channel.basicConsume(QUEUE_NAME, true, (consumerTag, delivery) -> {
                        String message = new String(delivery.getBody(), "UTF-8");
//                        System.out.println(message);
                        handleMessage(message);
                        latch.countDown(); // Decrease the count of the latch
                    }, consumerTag -> {
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        latch.await();
//        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

//        System.out.println("i GET THERE");
//        try (java.sql.Connection conn = DriverManager.getConnection(url, username, password)) {
//            System.out.println("i GET THERE before database");
//            String sql = "INSERT INTO sys.user (id, likes, dislikes) VALUES (?, ?, ?)";
//            System.out.println("i GET THERE before sql");
//            PreparedStatement stmt = conn.prepareStatement(sql);
//
//
//
//            for(String id: swipeCountMap.keySet()){
//                System.out.println("Am I in the hashmap loop?");
//                stmt.setString(1, id);
//                int likes = swipeCountMap.get(id).getLikes().get();
//                int dislikes = swipeCountMap.get(id).getDislikes().get();
//                System.out.println( "I am " + id  +  "likes: " + likes + "dislikes: " + dislikes + ".");
//                stmt.setInt(2, likes);
//                stmt.setInt(3, dislikes);
//                stmt.executeUpdate();
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

        executor.shutdown();
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
//        System.out.println("swipe is " + data.getSwipe() + "person is " +  data.getSwiper());
//        System.out.println("Parsed swipeData: " + data); // Add this line to log the parsed swipeData object
        return data;
    }

    private static void updateMap(swipeData data) {
        // Update userMap based on user object
        // Thread-safe operations should be used

        String id = data.getSwipee();
        if(!swipeCountMap.containsKey(id)){
            AtomicInteger likes = new AtomicInteger(0);
            AtomicInteger dislikes = new AtomicInteger(0);
            swipeCountMap.put(id,new LikeAndDislike(likes,dislikes));
        }

        if(Objects.equals(data.getSwipe(), "right")){
            swipeCountMap.get(id).incrementLike();
        }
        else{
            swipeCountMap.get(id).incrementDislike();
        }

//        System.out.println("Updated swipeCountMap: " + swipeCountMap); // Add this line to log the updated swipeCountMap entries
//        System.out.println("id is " + id + " and number of likes is " + swipeCountMap.get(id).getLikes() + " also dislikes is " + swipeCountMap.get(id).getDislikes());
    }
}



