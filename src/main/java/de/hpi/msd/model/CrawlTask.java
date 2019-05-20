package de.hpi.msd.model;

public class CrawlTask {
    private final long userId;
    private final InteractionType type;

    public CrawlTask(long userId, InteractionType type) {
        this.userId = userId;
        this.type = type;
    }

    public long getUserId() {
        return userId;
    }

    public InteractionType getType() {
        return type;
    }
}
