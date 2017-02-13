   /**
    * This class creates a frame with a JEditorPane for loading HTML help files
    */

  import java.io.*;
  import javax.swing.event.*;
  import javax.swing.*;
  import java.net.*;
  import java.awt.event.*;
  import java.awt.*;
  
  public class HelpWindow extends JFrame implements ActionListener{
      private final int WIDTH = 800;
      private final int HEIGHT = 600;
      private JEditorPane editorpane;
      private URL helpURL;

	  /**
	   * HelpWindow constructor
	   * @param String and URL
	   */
	  public HelpWindow(String title, URL hlpURL) {
	      super(title);
	      helpURL = hlpURL; 
	      editorpane = new JEditorPane();
	      editorpane.setEditable(false);
	      try {
	          editorpane.setPage(helpURL);
	      } catch (Exception ex) {
	          ex.printStackTrace();
	      }
	      //anonymous inner listener
	      editorpane.addHyperlinkListener(new HyperlinkListener() {
	          public void hyperlinkUpdate(HyperlinkEvent ev) {
	              try {
	                  if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
	                      editorpane.setPage(ev.getURL());
	                  }
	              } catch (IOException ex) {
	                  //put message in window
	                  ex.printStackTrace();
	              }
	          }
	      });
	      getContentPane().add(new JScrollPane(editorpane));
	      addButton();
	      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	      calculateLocation(); 	     // dynamically set location
	      setVisible(true);
	  }
	  
	  /**
	   * An Actionlistener so must implement this method
	   *
	   */
	  public void actionPerformed(ActionEvent e) {
	      String strAction = e.getActionCommand();
	          if (strAction == "Close")
	              processWindowEvent(new WindowEvent(this,WindowEvent.WINDOW_CLOSING));
	  }
	  
	  /**
	   * add close button at the south
	   */
	  private void addButton() {
	      JButton btnclose = new JButton("Close");
	      btnclose.addActionListener(this);
	      /* put into JPanel */
	      JPanel panebutton = new JPanel();
	      panebutton.add(btnclose);
	      /* add panel south */
	      getContentPane().add(panebutton, BorderLayout.SOUTH);
	  }
	  
	  /**
	   * locate help screen in middle of screen
	   */
	  private void calculateLocation() {
	      Dimension screendim = Toolkit.getDefaultToolkit().getScreenSize();
	      setSize(new Dimension(WIDTH, HEIGHT));
	      int locationx = (screendim.width - WIDTH) / 2;
	      int locationy = (screendim.height - HEIGHT) / 2;
	      setLocation(locationx, locationy);
	}
	
  }