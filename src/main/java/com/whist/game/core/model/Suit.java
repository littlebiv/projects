package com.whist.game.core.model;

public enum Suit {
    CLUBS(1),
    HEARTS(2),
    DIAMONDS(3),
    SPADES(4);

    private final int auctionPriority;

    Suit(int auctionPriority) {
        this.auctionPriority = auctionPriority;
    }

    public int auctionPriority() {
        return auctionPriority;
    }
}
