package com.example.blackjack.ui;

import com.example.blackjack.engine.BlackjackGame;
import com.example.blackjack.model.Card;
import com.example.blackjack.model.GameState;
import com.example.blackjack.model.Suit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TablePanel extends JPanel {
    private final BlackjackGame game;
    private final List<Animation> animations = new ArrayList<>();
    private final Timer timer;
    private String pendingBannerText;
    private Runnable onAnimationStateChange;
    private boolean lastAnimating = false;
    private int roundBet = 0; // Ставка текущего раунда

    public TablePanel(BlackjackGame game) {
        this.game = game;
        setBackground(UITheme.GREEN_FELT);
        this.timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepAnimations();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        int cardWidth = Math.max(60, Math.min(100, width / 10));
        int cardHeight = (int) (cardWidth * 1.4);
        int spacing = Math.max(12, cardWidth / 5);

        // Дилер сверху, Игрок снизу
        int dealerY = 30;
        int playerY = height - cardHeight - 30;
        drawHand(g2, game.getDealer().getHand().getCards(), true, dealerY, cardWidth, cardHeight, spacing);
        drawHand(g2, game.getPlayer().getHand().getCards(), false, playerY, cardWidth, cardHeight, spacing);

        // Значения рук
        g2.setColor(UITheme.TEXT_PRIMARY);
        g2.setFont(getFont().deriveFont(Font.BOLD, 18f));
        String dealerVal = game.getState() == GameState.PLAYER_TURN ? "?" : String.valueOf(game.getDealer().getHand().getBestValue());
        g2.drawString("Дилер: " + dealerVal, 20, dealerY + cardHeight + 22);
        g2.drawString("Игрок: " + game.getPlayer().getHand().getBestValue(), 20, playerY - 8);

        // Отрисовка фишки ставки в правой верхней четверти экрана
        int currentBet = game.getPlayer().getCurrentBet();
        if (currentBet > 0) {
            int chipX = width - 80; // Правая часть экрана
            int chipY = 80; // Верхняя часть экрана
            drawChip(g2, chipX, chipY, 25, currentBet);
        }

        // Отрисовка выигрыша/проигрыша (если раунд завершён)
        if (game.getState() == GameState.ROUND_OVER && game.getLastOutcome() != null) {
            int chipsBefore = game.getPlayer().getChipsBalance() - roundBet;
            int chipsAfter = game.getPlayer().getChipsBalance();
            int delta = chipsAfter - chipsBefore;
            
            if (delta > 0) {
                // Выигрыш - зелёная фишка справа от ставки
                int winX = width - 80;
                int winY = 140;
                g2.setColor(new Color(0x4CAF50));
                g2.fillOval(winX - 20, winY - 20, 40, 40);
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(winX - 20, winY - 20, 40, 40);
                g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
                g2.drawString("+" + delta, winX - 15, winY + 5);
            } else if (delta < 0) {
                // Проигрыш - красная фишка слева от ставки
                int loseX = width - 140;
                int loseY = 80;
                g2.setColor(new Color(0xF44336));
                g2.fillOval(loseX - 20, loseY - 20, 40, 40);
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(loseX - 20, loseY - 20, 40, 40);
                g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
                g2.drawString(String.valueOf(delta), loseX - 15, loseY + 5);
            }
        }

        // Отрисовка активных анимаций поверх
        for (Animation a : animations) {
            if (a instanceof CardAnimation ca) {
                Point p = ca.currentPosition(getWidth(), getHeight(), cardWidth, cardHeight, spacing);
                drawCardBack(g2, p.x, p.y, cardWidth, cardHeight);
            } else if (a instanceof BetAnimation ba) {
                Point p = ba.currentPosition(getWidth(), getHeight());
                drawChip(g2, p.x, p.y, 18, ba.amount);
            } else if (a instanceof BannerAnimation ba) {
                drawBanner(g2, ba.text, ba.alpha());
            }
        }

        g2.dispose();
    }

    private void drawHand(Graphics2D g2, List<Card> cards, boolean isDealer, int y, int cw, int ch, int spacing) {
        int x = 20;
        boolean hideHole = isDealer && game.getState() == GameState.PLAYER_TURN && cards.size() >= 2;
        for (int i = 0; i < cards.size(); i++) {
            int cx = x + i * (cw + spacing);
            if (isCardUnderAnimation(isDealer, i)) {
                continue; // карту нарисует анимация
            }
            if (hideHole && i == 1) {
                drawCardBack(g2, cx, y, cw, ch);
            } else {
                drawCard(g2, cards.get(i), cx, y, cw, ch);
            }
        }
    }

    private void drawCard(Graphics2D g2, Card card, int x, int y, int w, int h) {
        // Фон карты
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, w, h, 12, 12);
        g2.setColor(new Color(0x22, 0x22, 0x22));
        g2.drawRoundRect(x, y, w, h, 12, 12);

        // Определить цвет масти
        boolean isRed = card.getSuit() == Suit.HEARTS || card.getSuit() == Suit.DIAMONDS;
        Color suitColor = isRed ? new Color(0xC62828) : new Color(0x1B1B1B);

        // Верхний левый угол: ранг и масть
        g2.setFont(getFont().deriveFont(Font.BOLD, Math.max(14f, w * 0.22f)));
        g2.setColor(suitColor);
        String tl = card.getRank().getSymbol() + suitToSymbol(card.getSuit());
        g2.drawString(tl, x + 8, y + (int)(w * 0.22f) + 8);

        // Центр: крупная масть
        g2.setFont(getFont().deriveFont(Font.PLAIN, Math.max(28f, w * 0.5f)));
        FontMetrics fm = g2.getFontMetrics();
        String sym = suitToSymbol(card.getSuit());
        int sw = fm.stringWidth(sym);
        int sh = fm.getAscent();
        g2.drawString(sym, x + (w - sw) / 2, y + (h + sh) / 2 - 8);
    }

    private void drawCardBack(Graphics2D g2, int x, int y, int w, int h) {
        Color blue = new Color(0x1E88E5);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, w, h, 12, 12);
        g2.setColor(blue);
        g2.fillRoundRect(x + 4, y + 4, w - 8, h - 8, 10, 10);
        g2.setColor(Color.WHITE);
        // простая сетка
        for (int i = x + 10; i < x + w - 10; i += 8) {
            g2.drawLine(i, y + 10, i, y + h - 10);
        }
        for (int j = y + 10; j < y + h - 10; j += 8) {
            g2.drawLine(x + 10, j, x + w - 10, j);
        }
        g2.setColor(new Color(0x22, 0x22, 0x22));
        g2.drawRoundRect(x, y, w, h, 12, 12);
    }

    private String suitToSymbol(Suit suit) {
        return switch (suit) {
            case CLUBS -> "♣";
            case DIAMONDS -> "♦";
            case HEARTS -> "♥";
            case SPADES -> "♠";
        };
    }

    private void drawChip(Graphics2D g2, int cx, int cy, int r, int amount) {
        // Фишка — круг с белым кантом и надписью
        g2.setColor(new Color(0xD32F2F));
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        g2.setColor(UITheme.TEXT_PRIMARY);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        g2.setFont(getFont().deriveFont(Font.BOLD, Math.max(10f, r * 0.9f)));
        String text = String.valueOf(amount);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        int th = fm.getAscent();
        g2.setColor(UITheme.GREY_BG);
        g2.drawString(text, cx - tw / 2, cy + th / 2 - 3);
    }

    private boolean isCardUnderAnimation(boolean dealer, int index) {
        for (Animation a : animations) {
            if (a instanceof CardAnimation ca) {
                if (ca.targetIsDealer == dealer && ca.targetIndex == index && !ca.isFinished()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void stepAnimations() {
        boolean any = false;
        long now = System.currentTimeMillis();
        for (Animation a : animations) {
            if (!a.isFinished(now)) {
                any = true;
            }
        }
        animations.removeIf(a -> a.isFinished(now));
        if (!animations.isEmpty()) any = true;
        if (!any) {
            timer.stop();
        }
        boolean nowAnimating = timer.isRunning() || !animations.isEmpty();
        if (nowAnimating != lastAnimating) {
            lastAnimating = nowAnimating;
            if (onAnimationStateChange != null) {
                SwingUtilities.invokeLater(onAnimationStateChange);
            }
        }
        repaint();
    }

    private void ensureTimer() {
        if (!timer.isRunning()) timer.start();
    }

    // Публичные триггеры анимаций
    public void animateInitialDeal() {
        int dealerCount = game.getDealer().getHand().getCards().size();
        int playerCount = game.getPlayer().getHand().getCards().size();
        if (dealerCount < 2 || playerCount < 2) return;
        // последовательность: P0, D0, P1, D1 с увеличенными задержками
        long base = System.currentTimeMillis();
        animations.add(new CardAnimation(false, 0, base + 0, 500));      // Игрок 1-я карта
        animations.add(new CardAnimation(true, 0, base + 400, 500));     // Дилер 1-я карта
        animations.add(new CardAnimation(false, 1, base + 800, 500));    // Игрок 2-я карта
        animations.add(new CardAnimation(true, 1, base + 1200, 500));   // Дилер 2-я карта
        ensureTimer();
    }

    public void animatePlayerHit() {
        int idx = game.getPlayer().getHand().getCards().size() - 1;
        if (idx >= 0) {
            animations.add(new CardAnimation(false, idx, System.currentTimeMillis(), 500));
            ensureTimer();
        }
    }

    public void animateDealerDraw(int addedCount) {
        if (addedCount <= 0) return;
        int total = game.getDealer().getHand().getCards().size();
        long base = System.currentTimeMillis();
        for (int i = total - addedCount, step = 0; i < total; i++, step++) {
            animations.add(new CardAnimation(true, i, base + step * 400L, 500));
        }
        ensureTimer();
    }

    public void animateBet(int amount) {
        this.roundBet = amount; // Сохраняем ставку раунда
        animations.add(new BetAnimation(amount, System.currentTimeMillis(), 400));
        ensureTimer();
    }

    public void showBanner(String text, long durationMs) {
        this.pendingBannerText = text;
        animations.add(new BannerAnimation(text, System.currentTimeMillis(), durationMs));
        ensureTimer();
    }

    public void resetRoundBet() {
        this.roundBet = 0;
    }

    public boolean isAnimating() {
        return timer.isRunning() || !animations.isEmpty();
    }

    public void setOnAnimationStateChange(Runnable onAnimationStateChange) {
        this.onAnimationStateChange = onAnimationStateChange;
    }

    private void drawBanner(Graphics2D g2, String text, float alpha) {
        if (alpha <= 0f) return;
        Composite old = g2.getComposite();
        int w = getWidth();
        int h = getHeight();
        int bw = Math.min( (int)(w * 0.6), 520);
        int bh = 80;
        int x = (w - bw) / 2;
        int y = h / 2 - bh - 10;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.85f));
        g2.setColor(new Color(0, 0, 0, 210));
        g2.fillRoundRect(x, y, bw, bh, 16, 16);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g2.setColor(Color.WHITE);
        g2.setFont(getFont().deriveFont(Font.BOLD, 28f));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        int tx = x + (bw - tw) / 2;
        int ty = y + (bh + fm.getAscent()) / 2 - 6;
        g2.drawString(text, tx, ty);
        g2.setComposite(old);
    }

    // Базовые классы анимаций
    private abstract static class Animation {
        final long startAtMs;
        final long durationMs;
        Animation(long startAtMs, long durationMs) {
            this.startAtMs = startAtMs;
            this.durationMs = durationMs;
        }
        boolean isFinished() { return isFinished(System.currentTimeMillis()); }
        boolean isFinished(long now) { return now - startAtMs >= durationMs; }
        double progress(long now) { return Math.max(0, Math.min(1, (now - startAtMs) / (double) durationMs)); }
    }

    private class CardAnimation extends Animation {
        final boolean targetIsDealer;
        final int targetIndex;
        CardAnimation(boolean targetIsDealer, int targetIndex, long startAtMs, long durationMs) {
            super(startAtMs, durationMs);
            this.targetIsDealer = targetIsDealer;
            this.targetIndex = targetIndex;
        }
        Point currentPosition(int width, int height, int cw, int ch, int spacing) {
            int dealerY = 30;
            int playerY = height - ch - 30;
            int xStart = width / 2 - cw / 2; // позиция колоды по центру сверху
            int yStart = 10;
            int xTarget = 20 + targetIndex * (cw + spacing);
            int yTarget = targetIsDealer ? dealerY : playerY;
            long now = System.currentTimeMillis();
            double t = progress(now);
            // плавная интерполяция
            double tt = (1 - Math.cos(Math.PI * t)) / 2.0;
            int x = (int) Math.round(xStart + (xTarget - xStart) * tt);
            int y = (int) Math.round(yStart + (yTarget - yStart) * tt);
            return new Point(x, y);
        }
    }

    private class BetAnimation extends Animation {
        final int amount;
        BetAnimation(int amount, long startAtMs, long durationMs) {
            super(startAtMs, durationMs);
            this.amount = amount;
        }
        Point currentPosition(int width, int height) {
            int xStart = width - 80;
            int yStart = height - 60;
            int xTarget = width / 2;
            int yTarget = height / 2 + 20;
            long now = System.currentTimeMillis();
            double t = progress(now);
            double tt = (1 - Math.cos(Math.PI * t)) / 2.0;
            int x = (int) Math.round(xStart + (xTarget - xStart) * tt);
            int y = (int) Math.round(yStart + (yTarget - yStart) * tt);
            return new Point(x, y);
        }
    }

    private class BannerAnimation extends Animation {
        final String text;
        BannerAnimation(String text, long startAtMs, long durationMs) {
            super(startAtMs, durationMs);
            this.text = text;
        }
        float alpha() {
            long now = System.currentTimeMillis();
            double t = progress(now);
            if (t < 0.2) return (float)(t / 0.2);
            if (t > 0.8) return (float)((1.0 - t) / 0.2);
            return 1f;
        }
    }
}


