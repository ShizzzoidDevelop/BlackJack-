package com.example.blackjack.leaderboard;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardRepository {
    private final File storageFile;

    public LeaderboardRepository(File storageFile) {
        this.storageFile = storageFile;
    }

    public synchronized List<LeaderboardEntry> readAll() {
        Map<String, LeaderboardEntry> map = new HashMap<>();
        if (!storageFile.exists()) {
            return new ArrayList<>();
        }
        try (BufferedReader br = new BufferedReader(new FileReader(storageFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) continue;
                String name = parts[0];
                long profit = Long.parseLong(parts[1]);
                Instant t = Instant.ofEpochMilli(Long.parseLong(parts[2]));
                map.put(name, new LeaderboardEntry(name, profit, t));
            }
        } catch (IOException | NumberFormatException e) {
            // игнорировать испорченные записи
        }
        List<LeaderboardEntry> list = new ArrayList<>(map.values());
        Collections.sort(list);
        return list;
    }

    public synchronized void upsert(String name, long deltaProfit) {
        Map<String, LeaderboardEntry> map = new HashMap<>();
        for (LeaderboardEntry e : readAll()) {
            map.put(e.getName(), e);
        }
        LeaderboardEntry entry = map.get(name);
        if (entry == null) {
            entry = new LeaderboardEntry(name, 0, Instant.now());
            map.put(name, entry);
        }
        entry.addProfit(deltaProfit);
        saveAll(new ArrayList<>(map.values()));
    }

    private void saveAll(List<LeaderboardEntry> entries) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(storageFile))) {
            for (LeaderboardEntry e : entries) {
                bw.write(e.getName()+","+e.getNetProfit()+","+e.getLastUpdated().toEpochMilli());
                bw.newLine();
            }
        } catch (IOException ignored) {
        }
    }
}


