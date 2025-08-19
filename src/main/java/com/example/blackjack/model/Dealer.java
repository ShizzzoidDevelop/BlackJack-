package com.example.blackjack.model;

public class Dealer {
    private final Hand hand = new Hand();

    public Hand getHand() {
        return hand;
    }

    public void clearForNewRound() {
        hand.clear();
    }
}


