package com.whist.game.core.model;

import java.util.Objects;

public class Bid implements Comparable<Bid> {
    private final int amount;
    private final Suit trump;

    public Bid(int amount, Suit trump) {
        if (amount < 0 || amount > 13) {
            throw new IllegalArgumentException("Bid amount must be between 0 and 13.");
        }
        this.amount = amount;
        this.trump = Objects.requireNonNull(trump);
    }

    public int amount() {
        return amount;
    }

    public Suit trump() {
        return trump;
    }

    @Override
    public int compareTo(Bid other) {
        int amountCompare = Integer.compare(this.amount, other.amount);
        if (amountCompare != 0) {
            return amountCompare;
        }
        return Integer.compare(this.trump.auctionPriority(), other.trump.auctionPriority());
    }
}
