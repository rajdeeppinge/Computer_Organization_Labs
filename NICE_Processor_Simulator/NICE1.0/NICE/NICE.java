
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.JOptionPane;

/**
*  Launch the application
**/

public class NICE {
    private static final int splashDuration = 5000; // time in MS to show splash screen

    public NICE() {
        new AppSplashScreen( splashDuration ).showSplash();
        SwingUtilities.invokeLater(
        new Runnable() {
            public void run() {
                LC frame = new LC ();
                frame.setVisible(true);
            }
        } );
    }

    public static void main(String[] args) {
        new NICE();
    }
}