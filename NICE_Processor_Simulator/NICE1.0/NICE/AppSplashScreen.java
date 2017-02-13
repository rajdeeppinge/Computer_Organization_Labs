import java.awt.*;
import javax.swing.*;

/**
* Produces splash screen.
* Last modified: October 4, 2008
**/

public class AppSplashScreen extends JWindow {
    /* duration of splash screen to be visible */
    private int duration;

    public AppSplashScreen(int d) {
        duration = d;
    }

    /**
    *  A method to show a title screen in the center
    *  of the screen for the amount of time given in the constructor
    **/
    public void showSplash() {
        Color backgroundColor = Color.blue;
        Color foregroundColor = Color.white;
        JPanel content = (JPanel)getContentPane();
        content.setBackground(backgroundColor);

        /* Set the window's bounds, centering the window */
        int width = 600;
        int height = 400;
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screen = tk.getScreenSize();
        int x = (screen.width-width)/2;
        int y = (screen.height-height)/2;
        setBounds(x,y,width,height);

        /* Build the splash screen */
        JLabel picture = null;

        picture = new JLabel(new ImageIcon(AppSplashScreen.class.getResource("NICE.GIF")));

        JLabel title = new JLabel("Assembler-cum-Processor Simulator", JLabel.CENTER);

        JLabel copyright1 = new JLabel
        ("Version 1.0 : Copyright February 2009", JLabel.CENTER);
        JLabel copyright2 = new JLabel
        (" ", JLabel.CENTER);  // blank for now
        copyright1.setFont(new Font("Sans-Serif", Font.BOLD, 14));
        copyright2.setFont(new Font("Sans-Serif", Font.BOLD, 14));
        copyright1.setForeground(foregroundColor);
        copyright2.setForeground(foregroundColor);

        title.setFont(new Font("Sans-Serif", Font.BOLD, 20));
        title.setForeground(foregroundColor);
        JPanel titles = new JPanel(new GridLayout(2,1));
        titles.add(new JLabel(" "));
        titles.add(title);
        titles.setBackground(backgroundColor);

        content.add(titles, BorderLayout.NORTH);
        content.add(picture, BorderLayout.CENTER);

        JPanel copyrights = new JPanel(new GridLayout(3,1));
        copyrights.setBackground(backgroundColor);
        copyrights.add(copyright1);
        copyrights.add(copyright2);
        copyrights.add(new JLabel(" "));
        content.add(copyrights, BorderLayout.SOUTH);

        Color colorRed = new Color(156, 20, 20, 255);
        content.setBorder(BorderFactory.createLineBorder(colorRed, 3));
        setVisible(true);				// Display the splash screen
        try {
            Thread.sleep(duration);
        }
        catch (Exception e) {
            System.out.println("huh");
        }
        setVisible(false);				// Remove Display of splash screen
    }
}