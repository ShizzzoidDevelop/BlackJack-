package com.example.blackjack.engine;

import com.example.blackjack.model.*;

public class BlackjackGame {
    private final Deck deck = new Deck();
    private final Player player;
    private final Dealer dealer = new Dealer();
    private GameState state = GameState.WAITING_FOR_BET;
    private Outcome lastOutcome;
    private boolean dealerHitsSoft17 = false; // классика: дилер стоит на 17

    public BlackjackGame(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public Dealer getDealer() {
        return dealer;
    }

    public GameState getState() {
        return state;
    }

    public Outcome getLastOutcome() {
        return lastOutcome;
    }

    public void newRound(int bet) {
        if (state != GameState.WAITING_FOR_BET && state != GameState.ROUND_OVER) {
            throw new IllegalStateException("Невозможно начать новый раунд сейчас");
        }
        player.clearForNewRound();
        dealer.clearForNewRound();
        deck.resetForNewRound(); // Сбросить и перемешать колоду для нового раунда
        if (!player.canPlaceBet(bet)) {
            throw new IllegalArgumentException("Недостаточно фишек для ставки");
        }
        player.placeBet(bet);
        dealInitial();
    }

    private void dealInitial() {
        // Игрок, дилер, игрок, дилер
        player.getHand().add(deck.draw());
        dealer.getHand().add(deck.draw());
        player.getHand().add(deck.draw());
        dealer.getHand().add(deck.draw());

        if (player.getHand().isBlackjack()) {
            // проверить дилера на блэкджек
            if (dealer.getHand().isBlackjack()) {
                state = GameState.ROUND_OVER;
                lastOutcome = Outcome.PUSH;
                player.payoutPush();
            } else {
                state = GameState.ROUND_OVER;
                lastOutcome = Outcome.PLAYER_BLACKJACK;
                player.payoutBlackjack();
            }
        } else {
            state = GameState.PLAYER_TURN;
        }
    }

    public void hit() {
        if (state != GameState.PLAYER_TURN) {
            throw new IllegalStateException("Сейчас нельзя брать карту");
        }
        player.getHand().add(deck.draw());
        if (player.getHand().isBust()) {
            state = GameState.ROUND_OVER;
            lastOutcome = Outcome.DEALER_WIN;
        }
    }

    public void stand() {
        if (state != GameState.PLAYER_TURN) {
            throw new IllegalStateException("Сейчас нельзя остановиться");
        }
        state = GameState.DEALER_TURN;
    }

    public void doubleDown() {
        if (state != GameState.PLAYER_TURN) {
            throw new IllegalStateException("Сейчас нельзя удваивать");
        }
        if (player.getHand().getCards().size() != 2) {
            throw new IllegalStateException("Удвоение возможно только на первых двух картах");
        }
        player.doubleDown();
        player.getHand().add(deck.draw());
        if (player.getHand().isBust()) {
            state = GameState.ROUND_OVER;
            lastOutcome = Outcome.DEALER_WIN;
            return;
        }
        state = GameState.DEALER_TURN;
    }

    private int dealerPlay() {
        // Правила дилера: брать до 17. Если hitsSoft17=true, то брать на soft 17.
        int before = dealer.getHand().getCards().size();
        while (true) {
            int value = dealer.getHand().getBestValue();
            boolean soft = dealer.getHand().isSoft();
            if (value < 17) {
                dealer.getHand().add(deck.draw());
                continue;
            }
            if (value == 17 && soft && dealerHitsSoft17) {
                dealer.getHand().add(deck.draw());
                continue;
            }
            break;
        }
        return dealer.getHand().getCards().size() - before;
    }

    public int dealerPlayToEndAndSettle() {
        int added = dealerPlay();
        settle();
        return added;
    }

    private void settle() {
        int playerVal = player.getHand().getBestValue();
        int dealerVal = dealer.getHand().getBestValue();
        if (dealer.getHand().isBust()) {
            lastOutcome = Outcome.PLAYER_WIN;
            player.payoutWin();
        } else if (playerVal > dealerVal) {
            lastOutcome = Outcome.PLAYER_WIN;
            player.payoutWin();
        } else if (playerVal < dealerVal) {
            lastOutcome = Outcome.DEALER_WIN;
        } else {
            lastOutcome = Outcome.PUSH;
            player.payoutPush();
        }
        state = GameState.ROUND_OVER;
    }
}


