package com.oyealex.treetravel;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author oye
 * @since 2020-07-04 22:10:24
 */
public class LevelTravelVisitor extends SimpleFileVisitor<Path> {

    private final LevelTravelContext context;
    private final Path travelPath;

    @Getter
    private final List<Path> nextLevelPath = new LinkedList<>();

    public LevelTravelVisitor(LevelTravelContext context, Path travelPath) {
        this.context = context;
        this.travelPath = travelPath;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (Objects.equals(travelPath, dir)) {
            return FileVisitResult.CONTINUE;
        } else {
            nextLevelPath.add(dir);
            return FileVisitResult.SKIP_SUBTREE;
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        context.visitFile(attrs.size());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        context.visitFailed(file, exc);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.TERMINATE;
    }
}
