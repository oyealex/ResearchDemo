package com.oyealex.treetravel;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiFunction;

/**
 * @author oye
 * @since 2020-07-04 22:09:55
 */
@Slf4j
public class LevelTravelContext {

    private static final int EXPECTED_MAX_LEVEL = 32;

    private final ExecutorService executorService;

    /* Phaser synchronizer: used to control travel tasks in every level.
     * Controller thread will register every travel task on this phaser before submit it in current level,
     * And every travel task will notify this phaser on task finish.
     * Controller thread will await on this phaser after all travel tasks submitted, until all the travel tasks finish.
     * Then controller thread will get into next level and keep traveling, and so on.
     * Just like:
     *
     * |                    level 3                      |           |                    level 4                      |
     * |=================================================|           |=================================================|
     * | TravelTask_1: register -> travel path -> notify |           | TravelTask_1: register -> travel path -> notify |
     * | TravelTask_2: register -> travel path -> notify |  get into | TravelTask_2: register -> travel path -> notify |
     * | TravelTask_3: register -> travel path -> notify | next level| TravelTask_3: register -> travel path -> notify |
     * |                      ...                        |           |                      ...                        |
     * | TravelTask_n: register -> travel path -> notify |           | TravelTask_n: register -> travel path -> notify |
     *
     * PS: The init value "1" in this Phaser represent the controller thread.
     */
    private final Phaser phaser = new Phaser(1);
    private volatile int level = 1; // current level, init with 1

    // father level paths - store paths to travel in current level
    // next level paths - store the paths to travel in next level
    private Queue<Path> fatherLevelPaths = new LinkedList<>();
    private Queue<Path> nextLevelPaths = new LinkedList<>();
    // sync object for next level paths
    private final Object lockOfNextLevelPaths = new Object();

    // current level file and folder statistics info
    private final LongAdder fileAmountCounter = new LongAdder();
    private final LongAdder dirAmountCounter = new LongAdder();
    private final LongAdder fileSizeCounter = new LongAdder();
    private final LongAdder visitFailedCounter = new LongAdder();

    // level travel cost time and total cost time statistics
    private final Stopwatch levelTravelWatch = Stopwatch.createUnstarted();
    private long totalTravelCostMills = 0L;

    // store all level infos
    private final List<LevelInfo> allLevelInfos = new ArrayList<>(EXPECTED_MAX_LEVEL);

    // TD add file and folder statistics

    // function to handle level info on every level travel finish
    private BiFunction<List<LevelInfo>, List<Path>, Boolean> onLevelTravelFinishHandler;

    /**
     * 创建一个BFS基于层级的文件树多线程遍历上下文对象
     *
     * @param parallel 最大并行遍历文件夹的线程数量
     */
    public LevelTravelContext(int parallel) {
        executorService = new ThreadPoolExecutor(parallel, parallel,
                                                 0L,
                                                 TimeUnit.MILLISECONDS,
                                                 new LinkedBlockingQueue<>(),
                                                 new LevelTravelThreadFactory(),
                                                 new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 每当一个层级遍历完成时需要执行的处理方法，该处理方法的返回值决定了是否继续下一层级的遍历
     *
     * @param onLevelTravelFinishHandler 层级遍历完成时的处理方法
     *                                   <p>{@code BiFunction<List<LevelInfo>, List<Path>, Boolean>}
     *                                   <p>参数 {@code List<LevelInfo> allLevelInfos} 当前所有已经遍历的层级的统计信息
     *                                   <p>参数 {@code List<Path> nextLevelPaths} 下一层级所有需要遍历的文件夹
     *                                   <p>返回值 {@code Boolean shouldContinueTravel} 仅当返回{@code Boolean.TRUE}时，
     *                                   继续下一层级的遍历，否则停止遍历
     *                                   <p>默认一直遍历直到全部层级遍历完成，即
     *                                   {@code (levelInfoList, pathsToTravel) -> !pathsToTravel.isEmpty();}
     * @return 遍历上下文本身
     */
    public LevelTravelContext onOneLevelTravelFinish(
            BiFunction<List<LevelInfo>, List<Path>, Boolean> onLevelTravelFinishHandler) {
        this.onLevelTravelFinishHandler = onLevelTravelFinishHandler;
        return this;
    }

    /**
     * 从一个给定目录开始遍历
     *
     * @param path 开始遍历的目录路径
     */
    public void startTravel(Path path) {
        prepare(path);
        loopTravel();
        finishLoopTravel();
    }

    private void prepare(Path path) {
        if (onLevelTravelFinishHandler == null) {
            onLevelTravelFinishHandler = (levelInfoList, pathsToTravel) -> !pathsToTravel.isEmpty();
        }
        fatherLevelPaths.offer(path);
    }

    private void loopTravel() {
        while (callLevelFinishHandler() == Boolean.TRUE) {
            travelOneLevelAndAwait();
            generateAndCollectLevelInfo();
            getIntoNextLevel();
        }
    }

    private Boolean callLevelFinishHandler() {
        return onLevelTravelFinishHandler.apply(new ArrayList<>(allLevelInfos), new ArrayList<>(fatherLevelPaths));
    }

    private void finishLoopTravel() {
        executorService.shutdown();
        LevelInfo totalInfo = calcTotalLevelInfo();
        log.info("loop travel finished, all level: {}, total cost time: {} mills, total level info: {}", level - 1,
                 totalTravelCostMills, totalInfo);
    }

    private void travelOneLevelAndAwait() {
        levelTravelWatch.start();
        Path path;
        while ((path = fatherLevelPaths.poll()) != null) {
            submitOneTravelTask(path);
        }
        phaser.awaitAdvance(phaser.arrive()); // wait all travel task finish
        levelTravelWatch.stop();
    }

    private LevelInfo calcTotalLevelInfo() {
        allLevelInfos.forEach(info -> {
            fileAmountCounter.add(info.getFileAmount());
            dirAmountCounter.add(info.getDirAmount());
            fileSizeCounter.add(info.getFileSize());
            visitFailedCounter.add(info.getVisitFailedAmount());
        });
        return new LevelInfo(
                -1,
                fileAmountCounter.sumThenReset(),
                dirAmountCounter.sumThenReset(),
                fileSizeCounter.sumThenReset(),
                visitFailedCounter.sumThenReset());
    }

    private void generateAndCollectLevelInfo() {
        LevelInfo info = new LevelInfo(
                level,
                fileAmountCounter.sumThenReset(),
                dirAmountCounter.sumThenReset(),
                fileSizeCounter.sumThenReset(),
                visitFailedCounter.sumThenReset());
        allLevelInfos.add(info);

        long currLevelTravelCostTimeMills = levelTravelWatch.elapsed(TimeUnit.MILLISECONDS);
        log.info("level {} visit finished, cost time: {} mills, level info: {}", level, currLevelTravelCostTimeMills,
                 info);
        totalTravelCostMills += currLevelTravelCostTimeMills;
        levelTravelWatch.reset();
    }

    private void getIntoNextLevel() {
        level++;
        Queue<Path> tmpQueue = fatherLevelPaths;
        fatherLevelPaths = nextLevelPaths;
        nextLevelPaths = tmpQueue;
    }

    private boolean shouldContinueTravel() {
        return !fatherLevelPaths.isEmpty() || !nextLevelPaths.isEmpty();
    }

    private void submitOneTravelTask(Path path) {
        phaser.register();
        executorService.submit(new LevelTravelRunnable(this, path));
    }

    void visitFile(long fileSize) {
        fileAmountCounter.increment();
        fileSizeCounter.add(fileSize);
    }

    void visitFailed(Path file, IOException exc) {
        visitFailedCounter.increment();
        log.error("visit path {} failed on level {}: {}", file, level, exc.toString());
    }

    int getCurrLevel() {
        return level;
    }

    void finishOnePathTravel(List<Path> collectedPaths) {
        synchronized (lockOfNextLevelPaths) {
            nextLevelPaths.addAll(collectedPaths);
            dirAmountCounter.add(collectedPaths.size()); // record dir amount
        }
        phaser.arriveAndDeregister(); // one travel task finished, mark arrived and deregister
    }

    private static class LevelTravelThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        LevelTravelThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "LevelTravel-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
