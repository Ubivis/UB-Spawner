package com.ubivismedia.spawnerplugin;

public class Spawner {
    private final String entityType;
    private final int radius;
    private final int spawnLimit;
    private final long interval;
    private final boolean destroyOnLimit;
    private int spawnCount;

    public Spawner(String entityType, int radius, int spawnLimit, long interval, boolean destroyOnLimit) {
        this.entityType = entityType;
        this.radius = radius;
        this.spawnLimit = spawnLimit;
        this.interval = interval;
        this.destroyOnLimit = destroyOnLimit;
        this.spawnCount = 0;
    }

    public String getEntityType() {
        return entityType;
    }

    public int getRadius() {
        return radius;
    }

    public int getSpawnLimit() {
        return spawnLimit;
    }

    public long getInterval() {
        return interval;
    }

    public boolean shouldDestroyOnLimit() {
        return destroyOnLimit;
    }

    public int getSpawnCount() {
        return spawnCount;
    }

    public void incrementSpawnCount() {
        spawnCount++;
    }

    public boolean hasReachedSpawnLimit() {
        return spawnLimit > 0 && spawnCount >= spawnLimit;
    }
}
