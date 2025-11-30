package gym_app;

import javax.swing.*;

public class GymApp {
    public static void main(String[] args) {
        try {
            // Sử dụng FlatLaf hoặc System Look and Feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}