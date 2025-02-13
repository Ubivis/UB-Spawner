package com.ubivismedia.spawnerplugin;

import java.util.UUID;

public class Spawner {
    private final String entityType;
    private final int radius;
    private final int concurrentSpawns;
    private final long spawnInterval;
    private final int maxSpawns;
    private int spawnCount;
    private final UUID armorStandUUID;

    public Spawner(String entityType, int radius, int concurrentSpawns, long spawnInterval, int maxSpawns, UUID armorStandUUID) {
        this.entityType = entityType;
        this.radius = radius;
        this.concurrentSpawns = concurrentSpawns;
        this.spawnInterval = spawnInterval;
        this.maxSpawns = maxSpawns;
        this.armorStandUUID = armorStandUUID;
        this.spawnCount = 0;
    }

    public String getEntityType() {
        return entityType;
    }

    public int getRadius() {
        return radius;
    }

    public int getConcurrentSpawns() {
        return concurrentSpawns;
    }

    public long getSpawnInterval() {
        return spawnInterval;
    }

    public int getMaxSpawns() {
        return maxSpawns;
    }

    public int getSpawnCount() {
        return spawnCount;
    }

    public UUID getArmorStandUUID() {
        return armorStandUUID;
    }

    public void incrementSpawnCount() {
        spawnCount++;
    }

    public boolean hasReachedSpawnLimit() {
        return maxSpawns > 0 && spawnCount >= maxSpawns;
    }

    public boolean canSpawn() {
        return spawnCount < maxSpawns;
    }
}
