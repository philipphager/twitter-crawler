package de.hpi.msd.model.interaction;

public final class Interaction {
    private final long userId;
    private final long tweetId;
    private final InteractionType type;

    public Interaction(long userId, long tweetId, InteractionType type) {
        this.userId = userId;
        this.tweetId = tweetId;
        this.type = type;
    }

    public long getUserId() {
        return userId;
    }

    public long getTweetId() {
        return tweetId;
    }

    public InteractionType getType() {
        return type;
    }
}
