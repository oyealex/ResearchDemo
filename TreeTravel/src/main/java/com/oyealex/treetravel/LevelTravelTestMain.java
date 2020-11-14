package com.oyealex.treetravel;

import java.nio.file.Paths;

/**
 * @author oye
 * @since 2020-07-05 13:53:43
 */
public class LevelTravelTestMain {
    public static void main(String[] args) {
        new LevelTravelContext(8)
                .onOneLevelTravelFinish(((levelInfos, paths) -> levelInfos.size() < 3))
                .startTravel(Paths.get("D:\\"));
    }
}
