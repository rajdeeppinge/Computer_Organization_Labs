import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Calculator extends JPanel implements ActionListener {
  private JTextField display = new JTextField("Starting . .");
  private String buttonText = "Submit";

  public Calculator() {
    setLayout(new BorderLayout());
    display.setEditable(true);
    add(display, "North");
    JPanel p = new JPanel();
    JButton b = new JButton(buttonText);
    p.add(b);
    b.addActionListener(this);
    add(p, "Center");
  }

  public void actionPerformed(ActionEvent evt) {
    String typed;
    typed = display.getText();
    display.setText(typed.toUpperCase());
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setTitle("Calculator");
    frame.setSize(200, 100);
    frame.setLocation(600, 300);
    frame.setResizable(false);
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    Container contentPane = frame.getContentPane();
    contentPane.add(new Calculator());
    frame.show();
  }
}