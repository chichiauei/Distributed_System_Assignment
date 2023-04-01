package org.client2;

import io.swagger.client.ApiException;
import io.swagger.client.api.MatchesApi;
import io.swagger.client.api.StatsApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;

public class GetThread extends Thread {
    private final Integer lowerSwiperOrSwipee = 1;

    final Integer upperSwiper = 50001;
    private final CountDownLatch startLatch;
    private final CyclicBarrier endBarrier;
    private final List<Long> latencies = new ArrayList<>();
    private final Random random = new Random();



    public GetThread(CountDownLatch startLatch, CyclicBarrier endBarrier) {
        this.startLatch = startLatch;
        this.endBarrier = endBarrier;
    }

    @Override
    public void run() {
        try {
            startLatch.await();

            while (endBarrier.getNumberWaiting() < endBarrier.getParties() - 1) {
                for (int i = 0; i < 5; i++) {
                    long startTime = System.currentTimeMillis();

                    if (random.nextBoolean()) {
                        // Call MatchesApi
                        MatchesApi matchesApi = new MatchesApi();
                        matchesApi.matches(getRandomSwiper());
                    } else {
                        // Call StatsApi
                        StatsApi statsApi = new StatsApi();
                        statsApi.matchStats(getRandomSwiper());
                    }

                    long endTime = System.currentTimeMillis();
                    latencies.add(endTime - startTime);
                }

                Thread.sleep(1000);
            }

            printLatencyStats();

            endBarrier.await(); // Signal that the GetThread has finished

        } catch (InterruptedException | ApiException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }

    private String getRandomSwiper() {
        // Implement your logic to generate a random swiper ID
        return String.valueOf(ThreadLocalRandom.current().nextInt(lowerSwiperOrSwipee, upperSwiper));
    }


    private void printLatencyStats() {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long sum = 0;

        for (long latency : latencies) {
            min = Math.min(min, latency);
            max = Math.max(max, latency);
            sum += latency;
        }

        double mean = (double) sum / latencies.size();

        System.out.println("Min latency: " + min);
        System.out.println("Mean latency: " + mean);
        System.out.println("Max latency: " + max);
    }
}