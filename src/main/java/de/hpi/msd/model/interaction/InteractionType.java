package de.hpi.msd.model.interaction;

public enum InteractionType {
    TWEETS(0),
    FAVORITES(1),
    RETWEETS(2);

    private final int edgeType;

    InteractionType(final int edgeType) {
        this.edgeType = edgeType;
    }

    public int getEdgeType() {
        return edgeType;
    }
}
