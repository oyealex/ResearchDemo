package com.oyealex.treetravel;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author oye
 * @since 2020-07-05 12:35:02
 */
@Slf4j
public class LevelTravelRunnable implements Runnable {

    private final LevelTravelContext context;
    private final Path travelPath;

    public LevelTravelRunnable(LevelTravelContext context, Path travelPath) {
        this.context = context;
        this.travelPath = travelPath;
    }

    @Override
    public void run() {
        LevelTravelVisitor visitor = new LevelTravelVisitor(context, travelPath);
        try {
            Files.walkFileTree(travelPath, visitor);
        } catch (IOException e) {
            log.error("travel path {} on level {} failed: ", travelPath, context.getCurrLevel(), e);
        } finally {
            context.finishOnePathTravel(visitor.getNextLevelPath());
        }
    }
}
