package com.example.blackjack.leaderboard;

import java.time.Instant;

public class LeaderboardEntry implements Comparable<LeaderboardEntry> {
    private final String name;
    private long netProfit;
    private Instant lastUpdated;

    public LeaderboardEntry(String name, long netProfit, Instant lastUpdated) {
        this.name = name;
        this.netProfit = netProfit;
        this.lastUpdated = lastUpdated;
    }

    public String getName() {
        return name;
    }

    public long getNetProfit() {
        return netProfit;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void addProfit(long delta) {
        this.netProfit += delta;
        this.lastUpdated = Instant.now();
    }

    @Override
    public int compareTo(LeaderboardEntry o) {
        return Long.compare(o.netProfit, this.netProfit);
    }
}


