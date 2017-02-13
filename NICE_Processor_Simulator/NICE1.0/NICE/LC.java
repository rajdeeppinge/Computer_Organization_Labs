import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import java.util.*;
import java.io.*;

/**
*   class LC:
*   This is the main class. It creates the interface for the
*   application and handles the events generated.
*   Written by: Pradeep Singh, Yogesh Bansal (DA-IICT), April 2008
*
*   Modified by: Naresh Jotwani, November 2008
*
*/

class LC extends JFrame implements KeyListener, ActionListener {
    /* constants for JFrame's height,width and origin */
    private static final int FRAME_WIDTH  = 1024;
    private static final int FRAME_HEIGHT  = 750;
    private static final int FRAME_X_ORIGIN  = 0;
    private static final int FRAME_Y_ORIGIN  = 0;

    public static LC frame;     	/* self-referencing Object of LC class */

    /* Declaration of Objects for various components as TextArea, Table etc */

    public static JTextArea  assembleCodeArea, output;
    public static JTextField inFld;
    public static CodeEditor codeEditor;
    public static CustTable  memPanel;
    public static TablePanel flagPanel;
    public static TablePanel regPanel;
    public static CustTable  IOdevicePanel;

    /* Declaration of Objects for various components
    *  as MenuBar, MenuItem, ToolBar and Labels etc. */
    JMenuItem  pauseItem, item, resumeItem, goItem, stepItem;
    JMenuBar menu;
    JToolBar toolbar;
    JLabel lineNumbers;			/* to display line numbers in assembleCodeArea */
    public static JCheckBox showHex, showBinary;
    public static JPanel modeSelect;

    private int lastKeyCode = 10;        /* to track last key pressed in codeArea */

    public static boolean BPseen = false;
    public static String lastPC="000000";/* holds value of last PC */

    public static int delayTime=0;	 /* varies the execution speed in different modes */
    public static int format=1;          /* selects the display mode for assembled code 0-binary:1-Hex */
    public static int codeFormat=1;      /* selects the writing mode for code 0-binary:1-assembly */

    /* By default code is done in assembly and assembled code displayed in Binary format */

    /* path for address of stored images used */
    public static final String imagesPath = "";
    /* font for textArea */
    public static final Font MONOS = new Font("monospaced", Font.BOLD, 12);

    public static String title="NICE Processor Simulator";	 /* title for frame window */

    public static String instAddress="000000";       /* tracks address of instructions */
    public static String pc ="000000";		     /* holds the PC contents for simulator */

    public static int memsize=(int)Math.pow(2,12);   /* size of memory - NJ - 27/7/08 */
    public static int IOPanelSize = 24;

    public static String lab[][]=new String[100][2]; /* hold labels used in assembly code */
    Object[][] data; 				     /* store contents of Table used */

    public static ExeInst exeInst = new ExeInst();

    public static String error = "false";           /* to indicate occurence errors during assembling */
    public static boolean halt=false;               /* flag set to halt machine */
    public static boolean pause=false; 		    /* flag set to pause machine */
    public static boolean assembleError=false;      /* to indicate occurence errors during assembling */

    /*Declaration for the components of Menubar*/

    public static JMenu file, run, assemble, edit, display, execution, help;
    public static JMenuItem fileNew, fileOpen, fileClose, fileSave, fileSaveAs, fileInclude, fileExit;
    public static JMenuItem editUndo, editRedo, editCut, editCopy, editPaste;
    public static JMenuItem runGo, runStep, runPause, runReset, runResume, runAssemble;
    public static JMenuItem displayBinary, displayHex;
    public static JMenuItem executionSlow, executionMedium, executionFast;
    public static JMenuItem helpHelp, helpArch, helpAbout;

    /*Declaration for the components of Toolbar*/

    public static JButton Undo, Redo, Cut, Copy, Paste;
    public static JButton New, Open, Save, SaveAs, Load, Print;
    public static JButton Run, Assemble, Reset, Step, Go, Resume, Pause;
    public static JButton ToHex, ToBin;
    public static JButton Help;

    /* "action" objects, which include action listeners. One of each will be created
    * and then shared between a menu item and its corresponding toolbar button.
    * It relates the button and menu item closely. */

    public static Action fileNewAction, fileOpenAction, fileCloseAction, fileSaveAction, fileIncludeAction;
    public static Action fileSaveAsAction, fileLoadAction, filePrintAction, fileExitAction;
    public static Action editUndoAction, editRedoAction, editCutAction, editCopyAction, editPasteAction;
    public static Action runAssembleAction, runGoAction, runStepAction, runPauseAction, runResumeAction, runResetAction;
    public static Action outputDisplayBinaryAction, outputDisplayHexAction;
    public static Action executionSlowAction, executionMediumAction, executionFastAction;
    public static Action helpHelpAction, helpArchAction, helpAboutAction;

    static UndoManager undomanager;
    static class UndoHandler implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent e) {
            if (undomanager != null) {
                undomanager.addEdit(e.getEdit());
            }
        }
    }

    public LC () {
        super();
        frame = this;
        Container contentPane;
        setTitle (title);
        setSize (FRAME_WIDTH, FRAME_HEIGHT);
        setResizable (false);
        setLocation (FRAME_X_ORIGIN, FRAME_Y_ORIGIN);
        contentPane = getContentPane();
        contentPane.setLayout(null);
        contentPane.setBackground(Color.white);
        Font font = new Font("monospaced", Font.PLAIN, 12);

        /* setting up the area where messages to user are displayed */
        output= new JTextArea();
        output.setEditable(false);
        output.setFont(MONOS);
        
        JPanel outPanel = new JPanel(new BorderLayout());
        outPanel.add(output,BorderLayout.CENTER);
        JScrollPane outScroll = new JScrollPane(outPanel,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel contentOutput = new JPanel();
        contentOutput.setLayout(new BorderLayout());
        contentOutput.setBounds(0,545,655,140);
        contentOutput.add(outScroll, BorderLayout.CENTER);
        contentOutput.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Messages to user"),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        contentPane.add(contentOutput);

        /* setting up the area where Code is written */
        codeEditor= new CodeEditor();
        codeEditor.setLayout(new ScrollPaneLayout());
        codeEditor.setBounds(0,250,350,290);
        // codeEditor.codeArea.setBackground(Color.lightGray);
        codeEditor.codeArea.setEditable(false);
        codeEditor.codeArea.addKeyListener(this);
        codeEditor.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Code area"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        contentPane.add(codeEditor);

        /* setting up the area where Assembler output is displayed */
        assembleCodeArea = new JTextArea();
        assembleCodeArea.setEditable(false);
        // assembleCodeArea.setBackground(Color.lightGray);
        assembleCodeArea.setFont(font);
        contentPane.add(assembleCodeArea);

        lineNumbers = new JLabel();
        lineNumbers.setForeground(Color.RED);
        lineNumbers.setBackground(Color.YELLOW);
        lineNumbers.setVerticalAlignment(JLabel.TOP);
        lineNumbers.setText("");
        lineNumbers.setFont(font);
        lineNumbers.setVisible(true);

        showBinary = new JCheckBox("Binary");
        showBinary.setToolTipText("If checked, will show assembled code in binary format.");
        showBinary.setEnabled(false);
        showBinary.setVisible(true);
        showHex = new JCheckBox("Hex");
        showHex.setToolTipText("If checked, will show assembled code in hexadecimal format.");
        showHex.setEnabled(false);
        showHex.setVisible(true);
        modeSelect=new JPanel(new GridLayout(1,2));
        modeSelect.add(showBinary);
        modeSelect.add(showHex);

        // Listener fires when "showBinary" check box is clicked.
        showBinary.addItemListener(
        new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if ( showBinary.isSelected()) {
                    showHex.setSelected(false);
                    showHex.setEnabled(true);
                    format=0;
                    // assAgain();
                }
                else {
                    showHex.setSelected(true);
                    showHex.setEnabled(true);
                    format=1;
                    // assAgain();
                }
            }
        } );

        // Listener fires when "showHex" check box is clicked.
        showHex.addItemListener(
        new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if ( showHex.isSelected()) {
                    showBinary.setSelected(false);
                    showBinary.setEnabled(true);
                    format=1;
                    // assAgain();
                } else {
                    showBinary.setSelected(true);
                    showBinary.setEnabled(true);
                    format=0;
                    // assAgain();
                }
            }
        } );

        assembleCodeArea.getDocument().addDocumentListener(
        new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) {
                lineNumbers.setText(getLineNumbersList());
            }
            public void removeUpdate(DocumentEvent evt) {
                this.insertUpdate(evt);
            }
            public void changedUpdate(DocumentEvent evt) {
                this.insertUpdate(evt);
            }
        } );

        JPanel source = new JPanel(new BorderLayout());
        source.add(assembleCodeArea,BorderLayout.CENTER);
        JScrollPane scrollArea  =
        new JScrollPane(source,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);     
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBounds(350,250,305,290);
        contentPanel.add(scrollArea, BorderLayout.CENTER);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Assembler output"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JPanel contentPane2 = new JPanel();
        contentPane2.setLayout(new BorderLayout());
        contentPane2.setBounds(465,195,190,55);
        contentPane2.add(modeSelect,BorderLayout.NORTH);
        contentPane2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Assembler output mode"), BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        contentPane.add(contentPanel);
        contentPane.add(contentPane2);

        /* setting up the area where Register content is shown
        *  - initially all registers zero - NJ - 27/7/08 */

        String[] columnNames = {
            "Register","value","Register","value"
        } ;
        Object[][] data = {
            {
                "PC", "x000000",  "IR", "x00000000"
            } , {
                "R0", "x00000000","R1", "x00000000"
            } , {
                "R2", "x00000000","R3", "x00000000"
            } , {
                "R4", "x00000000","R5", "x00000000"
            } , {
                "R6", "x00000000","R7", "x00000000"
            } , {
                "R8", "x00000000","R9", "x00000000"
            } , {
                "R10", "x00000000","R11", "x00000000"
            } , {
                "R12", "x00000000","R13", "x00000000"
            } , {
                "R14", "x00000000","R15", "x00000000"
            } ,
        } ;
        int [] editableColNos= {
        } ;
        regPanel = new TablePanel(data, columnNames, editableColNos);

        // To resize columns
        final Object[] longestValues = {
            "Register","x0000000000","Register","x0000000000"
        } ;
        regPanel.initColumnSizes(LC.regPanel.valTable, longestValues);

        regPanel.setBounds(0,45,460,199);
        regPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Registers"),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        regPanel.setVisible(true);
        contentPane.add(regPanel);

        /* setting up the area where flag values are displayed */
        String[] flagColumn = {
            "Flag","value"
        } ;
        Object[][] flagData = {
            {
                "Z", "0"
            } , {
                "N", "0"
            } , {
                "O", "0"
            } , {
                "C", "0"
            } , {
                "P", "0"
            }
        } ;
        int [] editableColNo= {
        } ;
        flagPanel = new TablePanel(flagData, flagColumn,editableColNo);
        flagPanel.setBounds(465,45,190,135);
        flagPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Flags"),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        flagPanel.setVisible(true);
        contentPane.add(flagPanel);

        /* setting up the area where memory content is displayed */
        Toolkit tk = Toolkit.getDefaultToolkit();
        Class cs = this.getClass();
        Object[][] data2 = new Object[memsize][3];
        for (int i=0;i<memsize;i++) {
            data2[i][0]= new Boolean(false);
            data2[i][1]="x"+CustTable.pad(Integer.toHexString(i).toUpperCase(),-6,"0");
            data2[i][2]="00000000";
        }
        String [] colNames = {
            "BP","Address","Content"
        } ;
        int [] editableColumnNos= {
            0
        } ;
        memPanel = new CustTable(data2, colNames, editableColumnNos);

        // To resize columns
        final Object[] longValues = {
            Boolean.TRUE,"x000000000000","x000000000000"
        } ;
        memPanel.initColumnSizes(memPanel.table,longValues);

        memPanel.setBounds(660,45,355,390);
        memPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Memory"),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        memPanel.setVisible(true);
        contentPane.add(memPanel);

        moveHighlight(); /* show where PC lies in memory */

        /* Setting up the area where IO devices content for input/output 
        * 
        *  For simplicity, only port numbers from 0 to 23 (dec) are provided.
        *
        */
        Object[][] IOdata = new Object[IOPanelSize/2][4];
        int[] editableColumnNo= {
            1, 3
        } ;
        for (int i=0;i<IOPanelSize/2;i++) {
            IOdata[i][0]="x"+CustTable.pad(Integer.toHexString(2*i).toUpperCase(),-2,"0");
            IOdata[i][1]="00000000";
            IOdata[i][2]="x"+CustTable.pad(Integer.toHexString(2*i+1).toUpperCase(),-2,"0");
            IOdata[i][3]="00000000";
        }
        String [] IOCol = {
            "Port #","value", "Port #","value"
        } ;
        
        IOdevicePanel= new CustTable(IOdata,IOCol,editableColumnNo);
        // To resize columns
        final Object[] IOsizes = {
            "x0000000","x00000000000000","x0000000","x00000000000000"
        } ;
        IOdevicePanel.initColumnSizes(IOdevicePanel.table,IOsizes);
        IOdevicePanel.setBounds(660,438,355,247);
        IOdevicePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("I/O ports"),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        IOdevicePanel.setVisible(true);
        contentPane.add(IOdevicePanel);
        
        /* initialise the action components,toolbar and menubar */
        createActionObjects();
        menu = setUpMenuBar();
        setJMenuBar(menu);
        toolbar= setUpToolBar();

        /* add the toolbar area to the Frame */
        JPanel jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jp.add(toolbar);
        jp.setBounds(0,2,600,40);
        jp.setVisible(true);
        contentPane.add(jp);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
    /* Move highlighted row in memory area as PC changes.
    **/
    public static void moveHighlight() {
        // remove "<<< PC" from old PC value
        int temp = Integer.parseInt(frame.lastPC,16);
        String valGet = (String)LC.memPanel.table.getValueAt(temp,1);
        valGet = valGet.substring(0,7); // hex length of mem address
        LC.memPanel.table.setValueAt((Object) valGet, temp, 1);

        // at new PC location add "<<< PC"
        temp = Integer.parseInt(frame.pc,16);
        valGet = (String) LC.memPanel.table.getValueAt(temp,1);
        valGet = valGet.substring(0,7); // remove old "<<< PC" if present
        valGet = valGet + " <<< PC";    // add it
        LC.memPanel.table.setValueAt((Object) valGet, temp, 1);

        frame.lastPC = frame.pc; // keep track where "<<< PC" was added
        
        // Scroll to new location. Separate thread to avoid
        // null pointer exception in scrollRectToVisible.
        // Found solution in a blog - NJ - Oct 2008.
        //
        Runnable scrollRectToVisibleMethod = new Runnable () {
            public void run () {
                int temp = Integer.parseInt(frame.pc,16);
                Rectangle cellRect = LC.memPanel.table.getCellRect(temp, 0, true);
                LC.memPanel.table.scrollRectToVisible(cellRect);
            }
        } ;
        SwingUtilities.invokeLater (scrollRectToVisibleMethod);
    }
    
    /**
    * Implements the method of KeyListener interface
    * Catches key releas event in codeArea and performs action if needed
    *
    * @param KeyEvent generated
    */
    public void keyReleased (KeyEvent e) {
        int key=e.getKeyCode();
        // catch key value
        // take action as needed
        lastKeyCode=key;
        return;
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    /* Sets up the action objects for menubar and toolbar events */
    private void createActionObjects() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Class cs = this.getClass();
        try{    
	fileNewAction = new FileEditMenuAction("New",
            new ImageIcon(LC.class.getResource("New22.gif")),
            "Create a new file for editing", new Integer(KeyEvent.VK_N),
            KeyStroke.getKeyStroke( KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);

            fileIncludeAction = new FileEditMenuAction("Import",null,
            "Import some file's text in current file", new Integer(KeyEvent.VK_I),null,frame);
            fileOpenAction = new FileEditMenuAction("Open ...",
            new ImageIcon(LC.class.getResource("Open22.gif")),
            "Open a file for editing", new Integer(KeyEvent.VK_O),
            KeyStroke.getKeyStroke( KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);

            fileCloseAction = new FileEditMenuAction("Close", null,
            "Close the current file", new Integer(KeyEvent.VK_W),
            KeyStroke.getKeyStroke( KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);
            fileSaveAction = new FileEditMenuAction("Save",
            new ImageIcon(LC.class.getResource("Save22.png")),
            "Save the current file", new Integer(KeyEvent.VK_S),
            KeyStroke.getKeyStroke( KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);
            fileSaveAsAction = new FileEditMenuAction("Save as ...",
            new ImageIcon(LC.class.getResource("SaveAs22.png")),
            "Save current file with different name", new Integer(KeyEvent.VK_A),
            null, frame);
            fileExitAction = new FileEditMenuAction("Exit", null,
            "Exit", new Integer(KeyEvent.VK_X),
            KeyStroke.getKeyStroke( KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), frame);

            editUndoAction = new FileEditMenuAction("Undo",
            new ImageIcon(LC.class.getResource("Undo22.png")),
            "Undo last edit", new Integer(KeyEvent.VK_U),
            KeyStroke.getKeyStroke( KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);

            editRedoAction = new FileEditMenuAction("Redo",
            new ImageIcon(LC.class.getResource("Redo22.png")),
            "Redo last edit", new Integer(KeyEvent.VK_R),
            KeyStroke.getKeyStroke( KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);

            editCutAction = new FileEditMenuAction("Cut",
            new ImageIcon(LC.class.getResource("Cut22.gif")),
            "Cut", new Integer(KeyEvent.VK_C),
            KeyStroke.getKeyStroke( KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);

            editCopyAction = new FileEditMenuAction("Copy",
            new ImageIcon(LC.class.getResource("Copy22.png")),
            "Copy", new Integer(KeyEvent.VK_O),
            KeyStroke.getKeyStroke( KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);
            editPasteAction = new FileEditMenuAction("Paste",
            new ImageIcon(LC.class.getResource("Paste22.png")),
            "Paste", new Integer(KeyEvent.VK_P),
            KeyStroke.getKeyStroke( KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);
            runStepAction = new RunMenuAction("Step",
            new ImageIcon(LC.class.getResource("StepForward22.png")),
            "Run one instruction at a time", new Integer(KeyEvent.VK_T),
            KeyStroke.getKeyStroke( KeyEvent.VK_F4, 0),
            frame);
            runGoAction = new RunMenuAction("Go",
            new ImageIcon(LC.class.getResource("Play22.png")),
            "Run whole program", new Integer(KeyEvent.VK_G),
            KeyStroke.getKeyStroke( KeyEvent.VK_F5, 0),
            frame);
            runPauseAction = new RunMenuAction("Pause",
            new ImageIcon(LC.class.getResource("Pause22.png")),
            "Pause Program", new Integer(KeyEvent.VK_L),
            KeyStroke.getKeyStroke( KeyEvent.VK_F6, 0),
            frame);
            runResumeAction = new RunMenuAction("Change PC",
            new ImageIcon(LC.class.getResource("Resume.png")),
            "Change PC value", new Integer(KeyEvent.VK_C),
            KeyStroke.getKeyStroke( KeyEvent.VK_F7, 0),
            frame);
            runResetAction = new RunMenuAction("Reset",
            new ImageIcon(LC.class.getResource("Reset22.png")),
            "Reset the sim", new Integer(KeyEvent.VK_Q),
            KeyStroke.getKeyStroke( KeyEvent.VK_F8, 0),
            frame);
            runAssembleAction = new RunMenuAction("Assemble",
            new ImageIcon(LC.class.getResource("Assemble22.png")),
            "Assemble the code", new Integer(KeyEvent.VK_A),
            KeyStroke.getKeyStroke( KeyEvent.VK_F3, 0),
            frame);
            outputDisplayBinaryAction = new DisplayMenuAction("Binary Format",
            new ImageIcon(LC.class.getResource("MyBlank22.gif")),
            "Binary Format for instructions assembled", new Integer(KeyEvent.VK_B),
            KeyStroke.getKeyStroke( KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);
            outputDisplayHexAction = new DisplayMenuAction("Hexadecimal Format",
            new ImageIcon(LC.class.getResource("MyBlank22.gif")),
            "Hexadecimal Format for instructions assembled", new Integer(KeyEvent.VK_H),
            KeyStroke.getKeyStroke( KeyEvent.VK_H, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);
            executionSlowAction = new ExecutionMenuAction("Slow",
            new ImageIcon(LC.class.getResource("Next22.png")),
            "Execute instruction in slow mode", new Integer(KeyEvent.VK_1),
            KeyStroke.getKeyStroke( KeyEvent.VK_1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);
            executionMediumAction = new ExecutionMenuAction("Medium",
            new ImageIcon(LC.class.getResource("Next22.png")),
            "Execute instruction in medium mode", new Integer(KeyEvent.VK_2),
            KeyStroke.getKeyStroke( KeyEvent.VK_2, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);
            executionFastAction = new ExecutionMenuAction("Fast",
            new ImageIcon(LC.class.getResource("Next22.png")),
            "Execute instruction in fast mode", new Integer(KeyEvent.VK_3),
            KeyStroke.getKeyStroke( KeyEvent.VK_3, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);
            helpHelpAction = new HelpMenuAction("Help",
            new ImageIcon(LC.class.getResource("NICEsmall.GIF")),
            "NICE user manual", new Integer(KeyEvent.VK_1),
            KeyStroke.getKeyStroke( KeyEvent.VK_1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);
            helpArchAction = new HelpMenuAction("Asm",
            new ImageIcon(LC.class.getResource("Assemble22.png")),
            "NICE assembly language", new Integer(KeyEvent.VK_2),
            KeyStroke.getKeyStroke( KeyEvent.VK_2, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);
            helpAboutAction = new HelpMenuAction("About",
            new ImageIcon(LC.class.getResource("Help22.png")),
            "Find the culprits", new Integer(KeyEvent.VK_3),
            KeyStroke.getKeyStroke( KeyEvent.VK_3, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            frame);
        }
        catch (NullPointerException e) {
            System.out.println("Internal Error: images folder not found, or other null pointer exception while creating Action objects");
            e.printStackTrace();
            System.exit(0);
        }
    }

    /* Sets the Toolbar items */
    JToolBar setUpToolBar() {
        JToolBar toolBar = new JToolBar();

        New = new JButton(fileNewAction);
        New.setText("");
        New.setActionCommand("New");
        Open = new JButton(fileOpenAction);
        Open.setText("");
        Open.setActionCommand("Open ...");
        Save = new JButton(fileSaveAction);
        Save.setText("");
        Save.setActionCommand("Save");
        Save.setEnabled(false);
        SaveAs = new JButton(fileSaveAsAction);
        SaveAs.setText("");
        SaveAs.setActionCommand("Save as ...");
        SaveAs.setEnabled(false);

        Undo = new JButton(editUndoAction);
        Undo.setText("");
        Undo.setActionCommand("Undo");
        Undo.setEnabled(false);
        Redo = new JButton(editRedoAction);
        Redo.setText("");
        Redo.setActionCommand("Redo");
        Redo.setEnabled(false);
        Cut= new JButton(editCutAction);
        Cut.setText("");
        Cut.setActionCommand("Cut");
        Cut.setEnabled(false);
        Copy= new JButton(editCopyAction);
        Copy.setText("");
        Copy.setActionCommand("Copy");
        Copy.setEnabled(false);
        Paste= new JButton(editPasteAction);
        Paste.setText("");
        Paste.setActionCommand("Paste");
        Paste.setEnabled(false);

        Assemble = new JButton(runAssembleAction);
        Assemble.setText("");
        Assemble.setActionCommand("Assemble");
        Assemble.setEnabled(false);
        Step = new JButton(runStepAction);
        Step.setText("");
        Step.setActionCommand("Step");
        Step.setEnabled(false);
        Go = new JButton(runGoAction);
        Go.setText("");
        Go.setActionCommand("Go");
        Go.setEnabled(false);
        Pause = new JButton(runPauseAction);
        Pause.setText("");
        Pause.setActionCommand("Pause");
        Pause.setEnabled(false);
        Resume = new JButton(runResumeAction);
        Resume.setText("");
        Resume.setActionCommand("Change PC");
        Resume.setEnabled(false);
        Reset = new JButton(runResetAction);
        Reset.setText("");
        Reset.setActionCommand("Reset");
        Help= new JButton(helpHelpAction);
        Help.setText("");
        Help.setActionCommand("Help");

        toolBar.add(New);
        toolBar.add(Open);
        toolBar.add(Save);
        toolBar.add(SaveAs);

        toolBar.add(Undo);
        toolBar.add(Redo);
        toolBar.add(Cut);
        toolBar.add(Copy);
        toolBar.add(Paste);
        toolBar.add(new JToolBar.Separator());
        toolBar.add(Assemble);
        toolBar.add(Step);
        toolBar.add(Go);
        toolBar.add(Pause);
        toolBar.add(Resume);
        toolBar.add(Reset);
        toolBar.add(new JToolBar.Separator());
        toolBar.add(Help);
        toolBar.add(new JToolBar.Separator());

        return toolBar;
    }

    /* Stes up the menubar items */
    private JMenuBar setUpMenuBar() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Class cs = this.getClass();
        JMenuBar menuBar = new JMenuBar();
        file=new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        edit = new JMenu("Edit");
        edit.setMnemonic(KeyEvent.VK_E);
        run = new JMenu("Run");
        run.setMnemonic(KeyEvent.VK_R);
        display = new JMenu("Display");
        display.setMnemonic(KeyEvent.VK_D);
        execution = new JMenu("Execution");
        execution.setMnemonic(KeyEvent.VK_X);
        help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);

        fileNew = new JMenuItem(fileNewAction);
        fileNew.setIcon(new ImageIcon(LC.class.getResource("New22.gif")));
        fileInclude = new JMenuItem(fileIncludeAction);
        fileInclude.setIcon(new ImageIcon(LC.class.getResource("MyBlank22.gif")));
        fileInclude.setEnabled(false);
        fileOpen = new JMenuItem(fileOpenAction);
        fileOpen.setIcon(new ImageIcon(LC.class.getResource("Open22.gif")));
        fileClose = new JMenuItem(fileCloseAction);
        fileClose.setIcon(new ImageIcon(LC.class.getResource("MyBlank22.gif")));
        fileClose.setEnabled(false);
        fileSave = new JMenuItem(fileSaveAction);
        fileSave.setIcon(new ImageIcon(LC.class.getResource("Save22.png")));
        fileSave.setEnabled(false);
        fileSaveAs = new JMenuItem(fileSaveAsAction);
        fileSaveAs.setIcon(new ImageIcon(LC.class.getResource("SaveAs22.png")));
        fileSaveAs.setEnabled(false);
        fileExit = new JMenuItem(fileExitAction);
        fileExit.setIcon(new ImageIcon(LC.class.getResource("MyBlank22.gif")));
        file.add(fileNew);
        file.add(fileOpen);
        file.add(fileInclude);
        file.add(fileClose);
        file.addSeparator();
        file.add(fileSave);
        file.add(fileSaveAs);

        file.addSeparator();
        file.add(fileExit);

        editUndo = new JMenuItem(editUndoAction);
        editUndo.setIcon(new ImageIcon(LC.class.getResource("Undo22.png")));
        editUndo.setActionCommand("Undo");
        editRedo = new JMenuItem(editRedoAction);
        editRedo.setIcon(new ImageIcon(LC.class.getResource("Redo22.png")));
        editRedo.setActionCommand("Redo");
        editCut = new JMenuItem(editCutAction);
        editCut.setIcon(new ImageIcon(LC.class.getResource("Cut22.gif")));
        editCut.setActionCommand("Cut");
        editCopy = new JMenuItem(editCopyAction);
        editCopy.setIcon(new ImageIcon(LC.class.getResource("Copy22.png")));
        editCopy.setActionCommand("Copy");
        editPaste = new JMenuItem(editPasteAction);
        editPaste.setIcon(new ImageIcon(LC.class.getResource("Paste22.png")));
        editPaste.setActionCommand("Paste");
        edit.add(editUndo);
        edit.add(editRedo);
        edit.addSeparator();
        edit.add(editCut);
        edit.add(editCopy);
        edit.add(editPaste);
        edit.setEnabled(false);

        runAssemble= new JMenuItem(runAssembleAction);
        runAssemble.setIcon(new ImageIcon(LC.class.getResource("Assemble22.png")));
        runAssemble.setEnabled(false);
        runStep = new JMenuItem(runStepAction);
        runStep.setIcon(new ImageIcon(LC.class.getResource("StepForward22.png")));
        runStep.setEnabled(false);
        runGo = new JMenuItem(runGoAction);
        runGo.setIcon(new ImageIcon(LC.class.getResource("Play22.png")));
        runGo.setEnabled(false);
        runPause = new JMenuItem(runPauseAction);
        runPause.setIcon(new ImageIcon(LC.class.getResource("Pause22.png")));
        runPause.setEnabled(false);
        runResume = new JMenuItem(runResumeAction);
        runResume.setIcon(new ImageIcon(LC.class.getResource("Resume.png")));
        runResume.setEnabled(false);
        runReset = new JMenuItem(runResetAction);
        runReset.setIcon(new ImageIcon(LC.class.getResource("Reset22.png")));

        displayBinary = new JMenuItem(outputDisplayBinaryAction);
        displayBinary.setIcon(new ImageIcon(LC.class.getResource("MyBlank22.gif")));
        displayHex = new JMenuItem(outputDisplayHexAction);
        displayHex.setIcon(new ImageIcon(LC.class.getResource("MyBlank22.gif")));

        executionSlow = new JMenuItem(executionSlowAction);
        executionSlow.setIcon(new ImageIcon(LC.class.getResource("Next22.png")));
        executionMedium = new JMenuItem(executionMediumAction);
        executionMedium.setIcon(new ImageIcon(LC.class.getResource("Next22.png")));
        executionFast = new JMenuItem(executionFastAction);
        executionFast.setIcon(new ImageIcon(LC.class.getResource("Next22.png")));

        run.add(runAssemble);
        run.add(runStep);
        run.add(runGo);
        run.add(runPause);
        run.add(runReset);
        run.add(runResume);
        run.addSeparator();

        display.add(displayBinary);
        display.add(displayHex);
        display.setEnabled(false);

        execution.add(executionSlow);
        execution.add(executionMedium);
        execution.add(executionFast);
        execution.setEnabled(false);

        helpHelp = new JMenuItem(helpHelpAction);
        helpHelp.setIcon(new ImageIcon(LC.class.getResource("NICEsmall.GIF")));
        helpHelp.setActionCommand("Help");
        helpArch = new JMenuItem(helpArchAction);
        helpArch.setIcon(new ImageIcon(LC.class.getResource("Assemble22.png")));
        helpArch.setActionCommand("Arch");
        helpAbout = new JMenuItem(helpAboutAction);
        helpAbout.setIcon(new ImageIcon(LC.class.getResource("Help22.png")));
        helpAbout.setActionCommand("About");
        help.add(helpHelp);
        help.add(helpArch);
        help.addSeparator();
        help.add(helpAbout);

        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(run);
        menuBar.add(display);
        menuBar.add(execution);
        menuBar.add(help);

        return menuBar;
    }

    public void actionPerformed(ActionEvent event) {
    }

    /**
    *  This method Assembles the code from codeArea. Tokenizes the instructions and
    *  passes them one-by-one to AssembleLine class for assemble process.
    */
    public static void assAgain() {
        assembleError = false;
        pc="000000";
        for(int i=0;i<lab.length;i++)
        for(int j=0;j<2;j++)
        lab[i][j]=null;

        instAddress = "00000000";
        assembleCodeArea.setText("");
        // output.setText("");

        String line = null;
        int lineNos= 0;
        writeMessage("Starting assembly.");
        int lineNums = codeEditor.getSourceLineCount();
        BufferedReader bufStringReader = new BufferedReader(new StringReader(codeEditor.codeArea.getText()));
        AssembleLine aLine = new AssembleLine( assembleCodeArea, output);
        try {
            while (lineNums>0) {
                lineNums--;
                lineNos++;
                line = bufStringReader.readLine();
                line = preProcess(line);
                if(line.length()==0) {
                    continue;
                }
                // Now send line for assembly ...
                if (codeFormat==1)
                    instAddress = aLine.processLine(line,instAddress,lab,lineNos);
                else if(codeFormat==0)
                    instAddress = aLine.processBinLine(line,instAddress,lineNos);
                if(error.equals("true")) {
                    error = "false";
                }
            }
            // patch up labels used before they are defined
            // writeMessage("Now to patch labels.");
            aLine.patchUsedLabels();            
        } catch (IOException e) {
            System.out.println(e.toString());
        }
                
        if(!assembleError) {
            writeMessage("Assembly complete - "+lineNos+" source lines.");
            Step.setEnabled(true); runStep.setEnabled(true);
            Go.setEnabled(true); runGo.setEnabled(true);
            Pause.setEnabled(true); runPause.setEnabled(true);
            Resume.setEnabled(true); runResume.setEnabled(true);
            execution.setEnabled(true);
        }
        else {
            Step.setEnabled(false); runStep.setEnabled(false);
            Go.setEnabled(false); runGo.setEnabled(false);
            Pause.setEnabled(false); runPause.setEnabled(false);
            Resume.setEnabled(false); runResume.setEnabled(false);
            execution.setEnabled(false);
        }
    }

    /**
    * This method initializes the machine i.e. regsters, flags,
    * break-points, ports, etc. but does not clear memory
    */
    public static void initialise() {
        moveHighlight(); 
        pc="000000";
        moveHighlight();
        halt=false;
        pause=false;
        BPseen=false;
        regPanel.valTable.setValueAt("x000000",0,1);
        regPanel.valTable.setValueAt("x00000000",0,3);
        for(int i=1;i<9;i++) {
            regPanel.valTable.setValueAt("x00000000",i,1);
            regPanel.valTable.setValueAt("x00000000",i,3);
        }
        for(int i=0;i<5;i++)
            flagPanel.valTable.setValueAt("0",i,1);
        for(int i=0;i<IOPanelSize/2;i++) {
            IOdevicePanel.table.setValueAt("00000000",i,1);
            IOdevicePanel.table.setValueAt("00000000",i,3);
        }    
        for(int i = 0; i < LC.memPanel.memSize; i++)
            LC.memPanel.table.setValueAt(false,i,0);    // Clear all breakpoints.
        output.setText("");
        writeMessage("Initializing processor.");
    }

    /**
    * This method clears memory.
    */
    public static void clearMemory() {
        String x;
        for (int i=0;i<memsize;i++) {
            LC.memPanel.table.setValueAt(false,i,0);
            // no need to refresh memory addresses
            // x = "x"+CustTable.pad(Integer.toHexString(i).toUpperCase(),-6,"0");
            // LC.memPanel.table.setValueAt(x,i,1)
            LC.memPanel.table.setValueAt("00000000",i,2);
        }
    }
    
    /**
    * Handles the amout of delay to be introduced based on speed of execution chosen
    *
    * @param number of miliseconds of delay as long value
    */
    public static void delay(long ms) {
        Date d = new Date();
        Date e;
        long cTime = d.getTime();
        long tTime;
        do {
            e = new Date();
            tTime = e.getTime();
        } while(tTime - cTime <= ms);
        return;
    }

    /**
    *  Form string with source code line numbers.
    *  Resulting string is HTML, for which JLabel does multiline label (it ignores '\n').
    *  The line number list is a JLabel with one line number per line.
    */
    public String getLineNumbersList() {
        StringBuffer lineNumberList = new StringBuffer("<html>");
        int lineCount = this.getSourceLineCount();
        for (int i=1; i<=lineCount;i++) {
            lineNumberList.append(""+i+"<br>");
        }
        lineNumberList.append("</html>");
        return lineNumberList.toString();
    }

    /**
    *  Calculate and return number of lines in source code text.
    *  This is done by counting newline characters then adding one if last line does
    *  not end with newline character.
    */

    public int getSourceLineCount() {
        BufferedReader bufStringReader = new BufferedReader(new StringReader(assembleCodeArea.getText()));
        int lineNums = 0;
        try {
            while (bufStringReader.readLine() != null)
            lineNums++;
        } catch (IOException e) {
        }
        return lineNums;
    }

    /**
    *  Pre-process input line to remove comments, commas, brackets, etc.
    *  param - input line
    *  returns - processed line
    */
    public static String preProcess( String line ) {
        final String PATTERN = "[ ]*";
        int pos = line.indexOf('/');    // drop chars after '/' - inline comments
        if (pos!=-1)
            line = line.substring(0,pos);
        // Drop '[' and ']' used in indirect & indexed addressing
        // since they are only needed for readability.
        line = dropChar( line, '[');
        line = dropChar( line, ']');
        // Drop commas for the same reason.
        line = dropChar( line, ',');
        if (line.matches(PATTERN))
            line = "";
        return line;
    }
    /*
    * in support of the above
    */
    public static String dropChar( String line, char x ) {
        int len = line.length();
        int pos = line.indexOf(x);    
        while (pos!=-1) {
            // drop it
            line = line.substring(0,pos)+" "+line.substring(pos+1,len);
            pos = line.indexOf(x);
        }
        return line;
    }    

    /*
    *  Write message to user.
    *  newline is added here.
    */
    public static void writeMessage( String msg ) {
        output.append(msg+"\n");
        output.setCaretPosition(LC.output.getDocument().getLength());
    }    
}
