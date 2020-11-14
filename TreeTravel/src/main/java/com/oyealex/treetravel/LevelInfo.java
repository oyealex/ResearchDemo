package com.oyealex.treetravel;

import lombok.Data;

/**
 * @author oye
 * @since 2020-07-04 22:10:07
 */
@Data
public class LevelInfo {
    private final int level;
    private final long fileAmount;
    private final long dirAmount;
    private final long fileSize;
    private final long visitFailedAmount;
}
