import com.google.gson.Gson;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Consumer {
    private final static String QUEUE_NAME = "my_queue";
    private final static String HOST_NAME = "http://35.90.11.11/";
    private final static int numThreads = 10;
    private static final ConcurrentHashMap<String, LikeAndDislike> swipeCountMap = new ConcurrentHashMap<>();





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
                        String message = new String(delivery.getBody(), "UTF-8");
                        System.out.println(message);
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

        if(data.getSwipe() == "left"){
            swipeCountMap.get(id).incrementLike();
        }
        else{
            swipeCountMap.get(id).incrementDislike();
        }
    }
}



