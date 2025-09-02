package com.example.blackjack.model;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards = new ArrayList<>();
    private final SecureRandom random = new SecureRandom();

    public Deck() {
        resetAndShuffle();
    }

    public void resetAndShuffle() {
        cards.clear();
        for (Suit s : Suit.values()) {
            for (Rank r : Rank.values()) {
                cards.add(new Card(r, s));
            }
        }
        Collections.shuffle(cards, random);
    }

    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Колода пуста. Нужно начать новый раунд.");
        }
        return cards.remove(cards.size() - 1);
    }

    public void resetForNewRound() {
        resetAndShuffle();
    }

    public int size() {
        return cards.size();
    }

    public boolean needsReshuffle() {
        return cards.size() < 15;
    }
}


