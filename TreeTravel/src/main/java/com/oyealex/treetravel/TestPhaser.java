package com.oyealex.treetravel;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

/**
 * @author oye
 * @since 2020-07-05 12:54:26
 */
@Slf4j
public class TestPhaser {
    public static void main(String[] args) {
        final Phaser phaser = new Phaser(1);
        final ExecutorService executorService = Executors.newFixedThreadPool(16);

        submitTask(phaser, executorService, 10);
        phaser.awaitAdvance(phaser.arrive());
        log.info("done 1");

        submitTask(phaser, executorService, 15);
        phaser.awaitAdvance(phaser.arrive());
        log.info("done 2");

        submitTask(phaser, executorService, 9);
        phaser.awaitAdvance(phaser.arrive());
        log.info("done 2");

        executorService.shutdown();
    }

    private static void submitTask(Phaser phaser, ExecutorService executorService, int amount) {
        log.info("start to submit task");
        for (int i = 0; i < amount; i++) {
            phaser.register();
            executorService.submit(() -> {
                log.info("start");
                sleep();
                int re = phaser.getRegisteredParties();
                log.info("end {} {} ", re, phaser.arriveAndDeregister());
            });
        }
        log.info("submit all task done, phaser: {}", phaser.getPhase());
    }

    private static void sleep() {
        try {
            Thread.sleep((long) (Math.random() * 2_000) + 1_000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
