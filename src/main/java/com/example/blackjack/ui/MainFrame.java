package com.example.blackjack.ui;

import com.example.blackjack.engine.BlackjackGame;
import com.example.blackjack.leaderboard.LeaderboardEntry;
import com.example.blackjack.leaderboard.LeaderboardRepository;
import com.example.blackjack.model.GameState;
import com.example.blackjack.model.Outcome;
import com.example.blackjack.model.Player;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MainFrame extends JFrame {
    private final BlackjackGame game;
    private final LeaderboardRepository leaderboardRepository;

    private final TablePanel tablePanel;
    private final JLabel statusLabel = new JLabel();
    private final JLabel chipsLabel = new JLabel();
    private final JTextField betField = new JTextField("50", 6);
    private final JButton betButton = new JButton("Ставка");
    private final JButton hitButton = new JButton("Ещё");
    private final JButton standButton = new JButton("Стоп");
    private final JButton doubleButton = new JButton("Х2");
    private final JButton chip10 = new JButton("10");
    private final JButton chip25 = new JButton("25");
    private final JButton chip50 = new JButton("50");
    private final JButton chip100 = new JButton("100");
    private final JButton chip500 = new JButton("500");
    private final JButton chipClear = new JButton("Сброс");
    private final DefaultListModel<String> leaderboardModel = new DefaultListModel<>();

    private int chipsBeforeRound;

    public MainFrame(String playerName) {
        super("BlackJack: " + playerName);
        UITheme.applyDefaults();
        this.game = new BlackjackGame(new Player(playerName, 1000));
        this.leaderboardRepository = new LeaderboardRepository(new File(System.getProperty("user.home"), ".blackjack_leaderboard.csv"));

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        this.tablePanel = new TablePanel(game);
        this.tablePanel.setOnAnimationStateChange(this::refreshUI);
        add(tablePanel, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel bankLbl = new JLabel("Банк:");
        bankLbl.setForeground(UITheme.TEXT_PRIMARY);
        chipsLabel.setForeground(UITheme.TEXT_PRIMARY);
        left.add(bankLbl);
        left.add(chipsLabel);
        south.add(left, BorderLayout.WEST);

        JPanel centerControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        JLabel betLbl = new JLabel("Ставка:");
        betLbl.setForeground(UITheme.TEXT_PRIMARY);
        centerControls.add(betLbl);
        betField.setColumns(6);
        betField.setBackground(UITheme.GREY_SURFACE);
        betField.setForeground(UITheme.TEXT_PRIMARY);
        betField.setCaretColor(UITheme.TEXT_PRIMARY);
        centerControls.add(betField);
        centerControls.add(chip10);
        centerControls.add(chip25);
        centerControls.add(chip50);
        centerControls.add(chip100);
        centerControls.add(chip500);
        centerControls.add(chipClear);
        south.add(centerControls, BorderLayout.CENTER);

        // Правый столбец: крупные действия, включая ставку
        JPanel right = new JPanel(new GridLayout(4, 1, 8, 8));
        Font baseFont = UIManager.getFont("Label.font");
        if (baseFont == null) baseFont = new JLabel().getFont();
        Font actionFont = baseFont.deriveFont(Font.BOLD, 16f);
        Dimension actionSize = new Dimension(160, 44);
        betButton.setFont(actionFont);
        betButton.setPreferredSize(actionSize);
        hitButton.setFont(actionFont);
        standButton.setFont(actionFont);
        doubleButton.setFont(actionFont);
        hitButton.setPreferredSize(actionSize);
        standButton.setPreferredSize(actionSize);
        doubleButton.setPreferredSize(actionSize);
        right.add(betButton);
        right.add(hitButton);
        right.add(standButton);
        right.add(doubleButton);
        south.add(right, BorderLayout.EAST);

        // Применить тему к панелям и кнопкам
        south.setBackground(UITheme.GREY_BG);
        left.setBackground(UITheme.GREY_BG);
        centerControls.setBackground(UITheme.GREY_BG);
        right.setBackground(UITheme.GREY_BG);
        UITheme.stylePrimary(betButton);
        UITheme.styleSecondary(hitButton);
        UITheme.styleSecondary(standButton);
        UITheme.styleSecondary(doubleButton);
        UITheme.styleSecondary(chip10);
        UITheme.styleSecondary(chip25);
        UITheme.styleSecondary(chip50);
        UITheme.styleSecondary(chip100);
        UITheme.styleSecondary(chip500);
        UITheme.styleSecondary(chipClear);

        add(south, BorderLayout.SOUTH);

        // Лидерборд с темой
        JPanel east = new JPanel(new BorderLayout());
        east.setBackground(UITheme.GREY_BG);
        JList<String> leaderboardList = new JList<>(leaderboardModel);
        leaderboardList.setBackground(UITheme.GREY_SURFACE);
        leaderboardList.setForeground(UITheme.TEXT_PRIMARY);
        leaderboardList.setSelectionBackground(UITheme.GOLD);
        leaderboardList.setSelectionForeground(UITheme.GREY_BG);
        leaderboardList.setFixedCellHeight(26);
        leaderboardList.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        leaderboardList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setBackground(isSelected ? UITheme.GOLD : UITheme.GREY_SURFACE);
                c.setForeground(isSelected ? UITheme.GREY_BG : UITheme.TEXT_PRIMARY);
                return c;
            }
        });
        JLabel lbTitle = new JLabel("Лидерборд");
        lbTitle.setForeground(UITheme.GOLD);
        lbTitle.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        east.add(lbTitle, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(leaderboardList);
        sp.getViewport().setBackground(UITheme.GREY_SURFACE);
        sp.setBorder(BorderFactory.createEmptyBorder());
        east.add(sp, BorderLayout.CENTER);
        east.setPreferredSize(new Dimension(240, 0));
        add(east, BorderLayout.EAST);

        JPanel north = new JPanel(new BorderLayout());
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        statusLabel.setForeground(UITheme.TEXT_PRIMARY);
        north.add(statusLabel, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        wireActions();
        refreshUI();
        loadLeaderboard();
    }

    private void wireActions() {
        java.util.function.Consumer<Integer> setBet = amount -> betField.setText(String.valueOf(amount));
        chip10.addActionListener(e -> setBet.accept(10));
        chip25.addActionListener(e -> setBet.accept(25));
        chip50.addActionListener(e -> setBet.accept(50));
        chip100.addActionListener(e -> setBet.accept(100));
        chip500.addActionListener(e -> setBet.accept(500));
        chipClear.addActionListener(e -> betField.setText(""));

        // Подсказки
        hitButton.setToolTipText("Ещё (Enter/Space)");
        standButton.setToolTipText("Стоп (S)");
        doubleButton.setToolTipText("Удвоить (D)");
        betButton.setToolTipText("Сделать ставку и начать раунд");
        chip10.setToolTipText("Быстрая ставка 10");
        chip25.setToolTipText("Быстрая ставка 25");
        chip50.setToolTipText("Быстрая ставка 50");
        chip100.setToolTipText("Быстрая ставка 100");
        chip500.setToolTipText("Быстрая ставка 500");
        chipClear.setToolTipText("Очистить поле ставки");

        // Горячие клавиши
        javax.swing.InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = getRootPane().getActionMap();
        im.put(KeyStroke.getKeyStroke("ENTER"), "HIT");
        im.put(KeyStroke.getKeyStroke("SPACE"), "HIT");
        im.put(KeyStroke.getKeyStroke('S'), "STAND");
        im.put(KeyStroke.getKeyStroke('D'), "DOUBLE");
        im.put(KeyStroke.getKeyStroke('1'), "BET10");
        im.put(KeyStroke.getKeyStroke('2'), "BET25");
        im.put(KeyStroke.getKeyStroke('3'), "BET50");
        im.put(KeyStroke.getKeyStroke('4'), "BET100");
        im.put(KeyStroke.getKeyStroke('5'), "BET500");
        am.put("HIT", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { if (hitButton.isEnabled()) hitButton.doClick(); }});
        am.put("STAND", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { if (standButton.isEnabled()) standButton.doClick(); }});
        am.put("DOUBLE", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { if (doubleButton.isEnabled()) doubleButton.doClick(); }});
        am.put("BET10", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { if (betField.isEnabled()) betField.setText("10"); }});
        am.put("BET25", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { if (betField.isEnabled()) betField.setText("25"); }});
        am.put("BET50", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { if (betField.isEnabled()) betField.setText("50"); }});
        am.put("BET100", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { if (betField.isEnabled()) betField.setText("100"); }});
        am.put("BET500", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { if (betField.isEnabled()) betField.setText("500"); }});

        betButton.addActionListener(e -> {
            try {
                int bet = Integer.parseInt(betField.getText().trim());
                chipsBeforeRound = game.getPlayer().getChipsBalance();
                tablePanel.animateBet(bet);
                game.newRound(bet);
                tablePanel.animateInitialDeal();
                if (game.getState() == GameState.ROUND_OVER) {
                    finishRound();
                } else {
                    statusLabel.setText("Ход игрока");
                    tablePanel.showBanner("Ход игрока", 700);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            refreshUI();
        });

        hitButton.addActionListener(e -> {
            try {
                game.hit();
                tablePanel.animatePlayerHit();
                if (game.getState() == GameState.ROUND_OVER) finishRound();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            refreshUI();
        });

        standButton.addActionListener(e -> {
            try {
                int before = game.getDealer().getHand().getCards().size();
                game.stand();
                tablePanel.showBanner("Ход дилера", 800);
                int added = game.dealerPlayToEndAndSettle();
                if (added > 0) tablePanel.animateDealerDraw(added);
                if (game.getState() == GameState.ROUND_OVER) finishRound();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            refreshUI();
        });

        doubleButton.addActionListener(e -> {
            try {
                game.doubleDown();
                tablePanel.animatePlayerHit();
                if (game.getState() == GameState.ROUND_OVER) finishRound();
                else {
                    tablePanel.showBanner("Ход дилера", 800);
                    int added = game.dealerPlayToEndAndSettle();
                    if (added > 0) tablePanel.animateDealerDraw(added);
                    if (game.getState() == GameState.ROUND_OVER) finishRound();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            refreshUI();
        });
    }

    private void finishRound() {
        int chipsAfter = game.getPlayer().getChipsBalance();
        long delta = chipsAfter - chipsBeforeRound;
        String name = game.getPlayer().getName();
        leaderboardRepository.upsert(name, delta);
        String msg = resultText();
        statusLabel.setText(msg + " Сделайте новую ставку.");
        tablePanel.showBanner(msg, 1200);
        loadLeaderboard();
    }

    private String resultText() {
        Outcome outcome = game.getLastOutcome();
        return switch (outcome) {
            case PLAYER_BLACKJACK -> "Блэкджек! Вы выиграли (3:2).";
            case PLAYER_WIN -> "Вы выиграли!";
            case DEALER_WIN -> "Дилер выиграл.";
            case PUSH -> "Ничья (push).";
        };
    }

    private void refreshUI() {
        chipsLabel.setText(String.valueOf(game.getPlayer().getChipsBalance()));

        boolean betting = game.getState() == GameState.WAITING_FOR_BET || game.getState() == GameState.ROUND_OVER;
        boolean animating = tablePanel.isAnimating();
        
        // Сбросить ставку раунда при начале нового раунда
        if (betting && !animating && game.getState() == GameState.WAITING_FOR_BET) {
            tablePanel.resetRoundBet();
        }
        
        betButton.setEnabled(betting && !animating);
        chip10.setEnabled(betting && !animating);
        chip25.setEnabled(betting && !animating);
        chip50.setEnabled(betting && !animating);
        chip100.setEnabled(betting && !animating);
        chip500.setEnabled(betting && !animating);
        chipClear.setEnabled(betting && !animating);
        hitButton.setEnabled(game.getState() == GameState.PLAYER_TURN && !animating);
        standButton.setEnabled(game.getState() == GameState.PLAYER_TURN && !animating);
        doubleButton.setEnabled(game.getState() == GameState.PLAYER_TURN && game.getPlayer().getHand().getCards().size() == 2 && !animating);
        tablePanel.repaint();
    }

    private void loadLeaderboard() {
        leaderboardModel.clear();
        List<LeaderboardEntry> list = leaderboardRepository.readAll();
        for (int i = 0; i < Math.min(20, list.size()); i++) {
            LeaderboardEntry e = list.get(i);
            leaderboardModel.addElement((i+1) + ". " + e.getName() + " — " + e.getNetProfit());
        }
    }
}


