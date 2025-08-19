package com.example.blackjack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hand {
    private final List<Card> cards = new ArrayList<>();

    public void add(Card card) {
        cards.add(card);
    }

    public void clear() {
        cards.clear();
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public int getBestValue() {
        int total = 0;
        int aceCount = 0;
        for (Card c : cards) {
            total += c.getRank().getDefaultValue();
            if (c.getRank() == Rank.ACE) aceCount++;
        }
        while (total > 21 && aceCount > 0) {
            total -= 10; // считать туз как 1 вместо 11
            aceCount--;
        }
        return total;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && getBestValue() == 21;
    }

    public boolean isBust() {
        return getBestValue() > 21;
    }

    public boolean isSoft() {
        int totalWithAcesAsOne = 0;
        int aceCount = 0;
        for (Card c : cards) {
            if (c.getRank() == Rank.ACE) {
                aceCount++;
                totalWithAcesAsOne += 1;
            } else {
                totalWithAcesAsOne += c.getRank().getDefaultValue();
            }
        }
        // Мягкая рука: хотя бы один туз может считаться как 11 без перебора
        return aceCount > 0 && totalWithAcesAsOne + 10 <= 21;
    }
}


