package com.example.blackjack;

import com.example.blackjack.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                String name = JOptionPane.showInputDialog(null, "Введите имя игрока", "BlackJack", JOptionPane.QUESTION_MESSAGE);
                if (name == null || name.isBlank()) {
                    name = "Игрок";
                }
                new MainFrame(name).setVisible(true);
            } catch (Throwable t) {
                t.printStackTrace();
                JOptionPane.showMessageDialog(null, String.valueOf(t), "Ошибка запуска", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}


