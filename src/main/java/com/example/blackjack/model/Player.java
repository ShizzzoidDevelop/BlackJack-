package com.example.blackjack.model;

public class Player {
    private final String name;
    private final Hand hand = new Hand();
    private int chipsBalance;
    private int currentBet;
    private boolean doubled;

    public Player(String name, int startingChips) {
        this.name = name;
        this.chipsBalance = startingChips;
    }

    public String getName() {
        return name;
    }

    public Hand getHand() {
        return hand;
    }

    public int getChipsBalance() {
        return chipsBalance;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public boolean isDoubled() {
        return doubled;
    }

    public void clearForNewRound() {
        hand.clear();
        currentBet = 0;
        doubled = false;
    }

    public boolean canPlaceBet(int amount) {
        return amount > 0 && amount <= chipsBalance;
    }

    public void placeBet(int amount) {
        if (!canPlaceBet(amount)) {
            throw new IllegalArgumentException("Недостаточно фишек для ставки");
        }
        chipsBalance -= amount;
        currentBet += amount;
    }

    public void doubleDown() {
        if (chipsBalance < currentBet) {
            throw new IllegalStateException("Недостаточно фишек для удвоения");
        }
        chipsBalance -= currentBet;
        currentBet *= 2;
        doubled = true;
    }

    public void payoutBlackjack() {
        int win = (int) Math.round(currentBet * 2.5); // ставка + 3:2 выплата
        chipsBalance += win;
    }

    public void payoutWin() {
        chipsBalance += currentBet * 2; // ставка возвращается + выигрыш 1:1
    }

    public void payoutPush() {
        chipsBalance += currentBet; // вернуть ставку
    }
}


