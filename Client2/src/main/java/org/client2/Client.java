package org.client2;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;

import java.sql.Timestamp;


import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Client implements Runnable {
    private final String baseUrl;
    private final Integer lowerSwiperOrSwipee = 1;

    final Integer upperSwiper = 5001;

    final Integer upperSwipee = 1000001;

    final Integer lowerASCCode = 33;

    final Integer upperASCCode = 127;

    final Integer times = 2;

    private static AtomicInteger successfulThreads = new AtomicInteger(0);
    private static AtomicInteger failedThreads = new AtomicInteger(0);

    BlockingDeque<Record> arr;
//    BlockingQueue<>
    public Client(String baseUrl,BlockingDeque<Record> arr) {
        this.baseUrl = baseUrl;
        this.arr = arr;
    }


    public void run() {

        for (int i = 0; i < times; i++) {
            Timestamp start = Timestamp.from(Instant.now());
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(baseUrl);
            SwipeApi swipeApi = new SwipeApi(apiClient);
            SwipeDetails swipe = new SwipeDetails();
            swipe.setSwiper(getRandomSwiper());
            swipe.setSwipee(getRandomSwipee());
            swipe.setComment(getRandomComment());

            if (sendSwipe(swipeApi, swipe, start)) {

                Client.successfulThreads.getAndIncrement();
            } else {
                Client.failedThreads.getAndIncrement();
            }

        }
        Main.completed.countDown();


    }

    private boolean sendSwipe(SwipeApi swipeApi, SwipeDetails swipe, Timestamp start) {

        final int tryTimes = 5;
        for (int i = 0; i < tryTimes; i++) {
            try {
                ApiResponse<Void> swipeResponse = swipeApi.swipeWithHttpInfo(swipe, getRandomSwipe());
                int statusCode = swipeResponse.getStatusCode();

                if (statusCode == 200 || statusCode == 201) {
                    Timestamp end = Timestamp.from(Instant.now());
                    long latency = end.getTime() - start.getTime();
                    arr.add(new Record(start, "POST", latency,statusCode));
                    return true;
                }
            } catch (ApiException e) {
                e.printStackTrace();
                System.err.println("Error: " + e.getMessage());
            }
        }
        return false;
    }

    private String getRandomSwipe() {
        // Generate a random swipe (either "left" or "right")
        return Math.random() < 0.5 ? "left" : "right";
    }

    private String getRandomSwiper() {
        // Generate a random swiper between 1 and 5000
        return String.valueOf(ThreadLocalRandom.current().nextInt(lowerSwiperOrSwipee, upperSwiper));
    }

    private String getRandomSwipee() {
        // Generate a random swipee between 1 and 1000000
        return String.valueOf(ThreadLocalRandom.current().nextInt(lowerSwiperOrSwipee, upperSwipee));
    }

    private String getRandomComment() {
        // Generate a random string of 256 characters
        char[] chars = new char[256];
        for (int i = 0; i < 256; i++) {
            chars[i] = (char) ThreadLocalRandom.current().nextInt(lowerASCCode, upperASCCode);
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

