package com.example.blackjack.ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public final class UITheme {
    private UITheme() {}

    // Палитра: серый, зелёный, золото (акцент)
    public static final Color GREY_BG = new Color(0x1E1F22);
    public static final Color GREY_SURFACE = new Color(0x2B2F33);
    public static final Color GREEN_FELT = new Color(0x0B633E);
    public static final Color GREEN_FELT_DARK = new Color(0x074B2E);
    public static final Color GOLD = new Color(0xD4AF37);
    public static final Color TEXT_PRIMARY = new Color(0xEAEAEA);
    public static final Color TEXT_MUTED = new Color(0xB9BEC4);

    public static void applyDefaults() {
        UIManager.put("Panel.background", GREY_BG);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("ToolTip.background", GREY_SURFACE);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
    }

    public static void stylePrimary(JButton b) {
        b.setBackground(GREY_SURFACE);
        b.setForeground(TEXT_PRIMARY);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(GOLD, 2, true));
        b.setOpaque(true);
    }

    public static void styleSecondary(JButton b) {
        b.setBackground(GREY_SURFACE);
        b.setForeground(TEXT_PRIMARY);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(new Color(0x3A3F44), 1, true));
        b.setOpaque(true);
    }
}


