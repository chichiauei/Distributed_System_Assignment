package org.eg;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.MatchesApi;
import io.swagger.client.api.StatsApi;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.MatchStats;
import io.swagger.client.model.Matches;
import io.swagger.client.model.SwipeDetails;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Client implements Runnable{
    private final String baseUrl;
    private final Integer lowerSwiperOrSwipee = 1;

    final Integer upperSwiper = 50001;

    final Integer upperSwipee = 50001;

    final Integer lowerASCCode = 33;

    final Integer upperASCCode = 127;

    final Integer times = 50;

    private static AtomicInteger successfulThreads = new AtomicInteger(0);
    private static AtomicInteger failedThreads = new AtomicInteger(0);

    BlockingDeque<Long> postLatencies;
    private final Random random = new Random();


    public Client(String baseUrl,BlockingDeque<Long>postLatencies) {
        this.baseUrl = baseUrl;
        this.postLatencies = postLatencies;
    }


    public void run() {

        Thread getSwipeThread = new Thread(getSwipeRunnable);
        getSwipeThread.start();

        for(int i = 0;i<times;i++) {
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(baseUrl);
            SwipeApi swipeApi = new SwipeApi(apiClient);

            SwipeDetails swipe = new SwipeDetails();
            swipe.setSwiper(getRandomSwiper());
            swipe.setSwipee(getRandomSwipee());
            swipe.setComment(getRandomComment());
            if (sendSwipe(swipeApi, swipe)) {
//                MatchesApi matchesApi = new MatchesApi();
//                try {
//                    Matches list = matchesApi.matches(getRandomSwiper());
//                } catch (ApiException e) {
//                    throw new RuntimeException(e);
//                }
//
//                StatsApi statsApi = new StatsApi();
//                try {
//                    MatchStats matchStats = statsApi.matchStats(getRandomSwiper());
//                } catch (ApiException e) {
//                    throw new RuntimeException(e);
//                }


                Client.successfulThreads.getAndIncrement();
            } else {
                Client.failedThreads.getAndIncrement();
            }
        }

            getSwipeThread.interrupt();

            // Wait for the getSwipe() thread to finish
            try {
                getSwipeThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        Main.completed.countDown();
    }

    private boolean sendSwipe(SwipeApi swipeApi,SwipeDetails swipe){

        final int tryTimes = 5;
        for (int i = 0; i < tryTimes; i++) {
            try {
                ApiResponse<Void> swipeResponse = swipeApi.swipeWithHttpInfo(swipe, getRandomSwipe());
                int statusCode = swipeResponse.getStatusCode();

                if (statusCode == 200 || statusCode == 201) {
                    return true;
                }
            }
            catch(ApiException e){
                e.printStackTrace();
                System.err.println("Error: " + e.getMessage());
            }
        }
        return false;
    }


    private Runnable getSwipeRunnable = new Runnable() {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Timestamp getStart = Timestamp.from(Instant.now());
                    getSwipe(getStart);
                    Thread.sleep(1000); // Wait for 1 second
                } catch (ApiException | InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    };

    private void getSwipe(Timestamp getStart) throws ApiException {
        final int times = 5;
        long latency = 0;
        for(int i = 0;i<times;i++){
            if (random.nextBoolean()) {
                // Call MatchesApi
                MatchesApi matchesApi = new MatchesApi();
                matchesApi.matches(getRandomSwiper());
            }
            else {
                // Call StatsApi
                StatsApi statsApi = new StatsApi();
                statsApi.matchStats(getRandomSwiper());
            }
            Timestamp end = Timestamp.from(Instant.now());
            latency = end.getTime() - getStart.getTime();
            postLatencies.add(latency);
        }
    }
    private String getRandomSwipe() {
        // Generate a random swipe (either "left" or "right")
        return Math.random() < 0.5 ? "left" : "right";
    }

    private String getRandomSwiper() {
        // Generate a random swiper between 1 and 50000
        return String.valueOf(ThreadLocalRandom.current().nextInt(lowerSwiperOrSwipee,upperSwiper));
    }

    private String getRandomSwipee() {
        // Generate a random swipee between 1 and 50000
        return String.valueOf(ThreadLocalRandom.current().nextInt(lowerSwiperOrSwipee,upperSwipee));
    }

    private String getRandomComment() {
        // Generate a random string of 256 characters
        char[] chars = new char[256];
        for (int i = 0; i < 256; i++) {
            chars[i] = (char) ThreadLocalRandom.current().nextInt(lowerASCCode,upperASCCode);
        }
        return new String(chars);
    }

    public static int getSuccessfulThreadsCount() {
        // Count the successful threads
        return successfulThreads.get();
    }

    public static int getFailedThreadsCount() {
        // Count the failed threads
        return failedThreads.get();
    }

}









