
import java.awt.Font;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.filechooser.FileFilter;

/** to handle and editor for reading and writing codeArea files. */
public class CodeEditor extends JScrollPane implements UndoableEditListener {
    /** monospaced font for the code area*/
    public static final Font MONO = new Font("monospaced", Font.PLAIN, 12);

    /** main code area */
    public JTextArea codeArea;
    public JCheckBox writeAssembly, writeBinary;
    public JPanel modeSelection;

    /** file chooser dialog box */
    protected JFileChooser fileChooser;

    /** undo manager for the editor */
    protected UndoManager undo;

    /** file currently being edited */
    protected File currentFile;

    /** whether the codeArea has been changed since last the document is saved */
    public static boolean changed = false;

    public static boolean flag = false;

    private JLabel lineNumbers;
    private static final char newline = '\n';
    private static int count = 0;


    /** initialising the editor */
    public CodeEditor() {
        super();
        codeArea = new JTextArea();
        undo = new UndoManager();

        // setup codeArea area
        codeArea.setFont(MONO);

        /* If source code is modified, will set flag to trigger/request file save. */
        codeArea.getDocument().addDocumentListener(
        new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) {
                LC.fileSave.setEnabled(true);
                LC.Save.setEnabled(true);
                lineNumbers.setText(getLineNumbersList());
                refreshUndoMenuItems();
            }
            public void removeUpdate(DocumentEvent evt) {
                this.insertUpdate(evt);
            }
            public void changedUpdate(DocumentEvent evt) {
                this.insertUpdate(evt);
            }
        }
        );

        lineNumbers = new JLabel();
        lineNumbers.setForeground(Color.RED);
        lineNumbers.setVerticalAlignment(JLabel.TOP);
        lineNumbers.setBackground(Color.YELLOW);
        lineNumbers.setText("");
        lineNumbers.setFont(MONO);
        lineNumbers.setVisible(true);

/* New pseudo-ops .ASM and HEX are used to define input line format
/* Therefore the following code is not required - NJ - 14/10/08

        writeBinary = new JCheckBox("Binary");
        writeBinary.setToolTipText("If checked, will consider code in Binary format.");
        writeBinary.setEnabled(true);    // NJ - 13/10/08
        writeBinary.setVisible(true);
        writeAssembly = new JCheckBox("Assembly");
        writeAssembly.setToolTipText("If checked, will consider code in Assembly format.");
        writeAssembly.setEnabled(true);  // NJ - 13/10/08
        writeAssembly.setVisible(true);
        modeSelection=new JPanel(new GridLayout(1,2));
        modeSelection.add(writeBinary);
        modeSelection.add(writeAssembly);

        // Listener fires when "writeBinary" check box is clicked.
        writeBinary.addItemListener(
        new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if ( writeBinary.isSelected()) {
                    writeAssembly.setEnabled(false);
                    LC.codeFormat=0;
                }
                else {
                    writeAssembly.setEnabled(true);
                }
            }
        } );

        // Listener fires when "writeAssembly" check box is clicked.
        writeAssembly.addItemListener(
        new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if ( writeAssembly.isSelected()) {
                    writeBinary.setEnabled(false);
                    LC.codeFormat=1;
                }
                else {
                    writeBinary.setEnabled(true);
                }
            }
        } );
--------------------------------------------------------------- */

        JPanel source = new JPanel(new BorderLayout());
        source.add(lineNumbers,BorderLayout.WEST);
        source.add(codeArea,BorderLayout.CENTER);
//      source.add(modeSelection,BorderLayout.SOUTH);

        JScrollPane editAreaScrollPane = new JScrollPane(source,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        editAreaScrollPane.getVerticalScrollBar().setUnitIncrement(
        codeArea.getFontMetrics(this.codeArea.getFont()).getHeight());
        this.add(editAreaScrollPane);

        setViewportView(editAreaScrollPane);

        // provide support for undo and redo
        addUndoableEditListener(this);

        // setup file chooser dialog box
        fileChooser = new JFileChooser(System.getProperty("user.dir"));
        fileChooser.addChoosableFileFilter(new SourceFileFilter());
    }


    /** starts from scratch with a blank document */
    public void newFile() {
        currentFile = null;

        LC.codeEditor.codeArea.setEditable(true);
        LC.codeEditor.codeArea.setBackground(Color.white);
        LC.codeEditor.codeArea.setText("");
        LC.assembleCodeArea.setText("");

//      writeAssembly.setEnabled(true);
//      writeAssembly.setSelected(true);
//      writeBinary.setEnabled(false);

        LC.showBinary.setEnabled(true);
        LC.showHex.setEnabled(false);
        LC.showBinary.setSelected(true);

        LC.fileSave.setEnabled(true);
        LC.Save.setEnabled(true);
        LC.fileSaveAs.setEnabled(true);
        LC.SaveAs.setEnabled(true);
        LC.fileClose.setEnabled(true);
        LC.runAssemble.setEnabled(true);
        LC.Assemble.setEnabled(true);
        LC.fileInclude.setEnabled(true);
        LC.edit.setEnabled(true);
        LC.display.setEnabled(true);
        LC.Cut.setEnabled(true);
        LC.Copy.setEnabled(true);
        LC.Paste.setEnabled(true);

        LC.codeFormat=1;
        LC.format=0;

        undo.discardAllEdits();
        changed = false;
    }

    /** opens the given file */
    public void openFile(String filename) throws IOException {
        File file = (filename == null ? null : new File(filename));
        openFile(file);
    }

    /** opens the given file */
    public void openFile(File file) throws IOException {
        String fileText;
        if (file == null) fileText = "";
        else {
            int len = (int) file.length();
            byte[] bytes = new byte[len];
            FileInputStream in = new FileInputStream(file);
            in.read(bytes);
            in.close();
            fileText = new String(bytes);
        }
        currentFile = file;
        LC.codeEditor.codeArea.setEditable(true);
        LC.codeEditor.codeArea.setBackground(Color.white);
        LC.showBinary.setEnabled(true);
        LC.showHex.setEnabled(false);
        LC.showBinary.setSelected(true);

        LC.fileSave.setEnabled(true);
        LC.Save.setEnabled(true);
        LC.fileSaveAs.setEnabled(true);
        LC.SaveAs.setEnabled(true);
        LC.fileClose.setEnabled(true);
        LC.runAssemble.setEnabled(true);
        LC.Assemble.setEnabled(true);
        LC.fileInclude.setEnabled(true);
        LC.edit.setEnabled(true);
        LC.display.setEnabled(true);
        LC.Cut.setEnabled(true);
        LC.Copy.setEnabled(true);
        LC.Paste.setEnabled(true);

        LC.format=0;

        String fileName = file.getName();
        
/* currently not using the extension in any way - NJ - 14/10/08 
        String fileExtn = 
            fileName.substring(fileName.indexOf((int)'.')+1,fileName.length());

            if(fileExtn.equals("bin")) {
            // writeAssembly.setEnabled(false);
            // writeBinary.setEnabled(false);
            LC.codeFormat=0;
        }
        else if(fileExtn.equals("asm")) {
            // writeAssembly.setEnabled(false);
            // writeBinary.setEnabled(false);
            LC.codeFormat=1;
        }
        else {
            // writeAssembly.setEnabled(true);
            // writeBinary.setEnabled(true);
        }
--------------------------------------------------------------- */

        setText(fileText);
        changed = false;
    }

    /** saves the given file */
    public void saveFile(String filename) throws IOException {
        File file = (filename == null ? null : new File(filename));
        saveFile(file);
    }

    /** saves the given file */
    public void saveFile(File file) throws IOException {
        byte[] bytes = getText().getBytes();
        FileOutputStream out = new FileOutputStream(file);
        out.write(bytes);
        out.close();
        currentFile = file;
        changed = false;
    }

    /** pops up a dialog box for the user to select a file to open */
    public boolean openDialog() {
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            // user has canceled request
            return false;
        }
        try {
            openFile(fileChooser.getSelectedFile());
        }
        catch (IOException exc) {
            exc.printStackTrace();
            return false;
        }
        return true;
    }

    /** pops up a dialog box for the user to select a file to save */
    public boolean saveDialog() {
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            // user has canceled request
            return false;
        }
        try {
            saveFile(fileChooser.getSelectedFile());
            /* 
            * Accept any filename - NJ - October 2008
            * Recall writeAssembly / writeBinay are not used
            *
            String fileName = fileChooser.getSelectedFile().getName();
            String fileExtn = fileName.substring(fileName.indexOf((int)'.')+1,fileName.length());
            if(fileExtn.equals("bin")) {
                writeAssembly.setEnabled(false);
                writeBinary.setEnabled(false);
                LC.codeFormat=0;
            }
            else if(fileExtn.equals("asm")) {
                writeAssembly.setEnabled(false);
                writeBinary.setEnabled(false);
                LC.codeFormat=1;
            }
            else {
                writeAssembly.setEnabled(true);
                writeBinary.setEnabled(true);
            }
            */
        }
        catch (IOException exc) {
            exc.printStackTrace();
            return false;
        }
        return true;
    }

    /** saves the file under its current name */
    public boolean saveFile() {
        boolean success = false;
        if (currentFile == null) success = saveDialog();
        else {
            try {
                saveFile(currentFile);
                success = true;
            }
            catch (IOException exc) {
                // display error box
                JOptionPane.showMessageDialog(this, "Could not save the file.",
                "codeArea Editor", JOptionPane.ERROR_MESSAGE);
            }
        }
        return success;
    }


    public void closeFile() {
        flag=true;
        changed = false;
        // writeAssembly.setEnabled(false);
        // writeBinary.setEnabled(false);
        LC.showBinary.setEnabled(false);
        LC.showHex.setEnabled(false);

        LC.fileSaveAs.setEnabled(false);
        LC.fileClose.setEnabled(false);
        LC.runAssemble.setEnabled(false);
        LC.runStep.setEnabled(false);
        LC.runGo.setEnabled(false);
        LC.runPause.setEnabled(false);
        LC.runResume.setEnabled(false);
        LC.fileInclude.setEnabled(false);
        LC.edit.setEnabled(false);
        LC.display.setEnabled(false);
        LC.execution.setEnabled(false);

        LC.Cut.setEnabled(false);
        LC.Copy.setEnabled(false);
        LC.Paste.setEnabled(false);
        LC.SaveAs.setEnabled(false);


        LC.codeFormat=1;
        LC.format=0;
    }


    /** undoes the last edit */
    public void undo() throws CannotUndoException {
        undo.undo();
        changed = true;
    }

    /** redoes the last undone edit */
    public void redo() throws CannotRedoException {
        undo.redo();
        changed = true;
    }

    /** cuts the selected codeArea to the clipboard */
    public void cut() {
        codeArea.cut();
        changed = true;
    }

    /** copies the selected codeArea to the clipboard */
    public void copy() {
        codeArea.copy();
    }

    /** pastes the clipboard into the codeArea document */
    public void paste() {
        codeArea.paste();
        changed = true;
    }

    /** returns a string containing the codeArea of the document */
    public String getText() {
        return codeArea.getText();
    }

    /** sets the codeArea of this document to the current string */
    public void setText(String codeArea) {
        this.codeArea.setText(codeArea);
    }

    /** returns the filename being edited */
    public String getFilename() {
        return (currentFile == null ? null : currentFile.getPath());
    }

    /** returns the file being edited */
    public File getFile() {
        return currentFile;
    }

    /** returns whether an undo command is possible */
    public boolean canUndo() {
        return undo.canUndo();
    }

    /** returns whether a redo command is possible */
    public boolean canRedo() {
        return undo.canRedo();
    }

    /** returns the name of the undo command */
    public String getUndoName() {
        return undo.getUndoPresentationName();
    }

    /** returns the name of the redo command */
    public String getRedoName() {
        return undo.getRedoPresentationName();
    }

    /** returns whether the document has changed since the last save */
    public boolean hasChanged() {
        return changed;
    }

    /** handle undoable edits */
    public void undoableEditHappened(UndoableEditEvent e) {
        if (!e.getEdit().isSignificant()) return;
        undo.addEdit(e.getEdit());
        if(!flag)
        changed = true;
        else    changed = false;
    }

    /** add an undoable edit listener */
    public void addUndoableEditListener(UndoableEditListener l) {
        codeArea.getDocument().addUndoableEditListener(l);
    }

    /** remove an undoable edit listener */
    public void removeUndoableEditListener(UndoableEditListener l) {
        codeArea.getDocument().removeUndoableEditListener(l);
    }

    /**
    * Given byte stream position in text being edited, calculate its column and line
    * number coordinates.
    *
    * @param stream position of character
    * @return position Its column and line number coordinate as a Point
    */
    public Point convertStreamPositionToLineColumn(int position) {
        String textStream = codeArea.getText();
        int line = 1;
        int column = 1;
        for (int i=0; i<position; i++) {
            if (textStream.charAt(i) == newline) {
                line++;
                column=1;
            }
            else {
                column++;
            }
        }
        return new Point(column,line);
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
        BufferedReader bufStringReader = new BufferedReader(new StringReader(codeArea.getText()));
        int lineNums = 0;
        try {
            while (bufStringReader.readLine() != null)
            lineNums++;
        } catch (IOException e) {
        }
        return lineNums;
    }


    /**
    *  Adds the source code line by line.
    *
    *  @param s A line of source code.
    **/
    public void append(String s) {
        this.codeArea.append(s);
        this.codeArea.setCaretPosition(0);
    }

    /**
    * Get source code text
    *
    * @return Sting containing source code
    */
    public String getSource() {
        return codeArea.getText();
    }

    private void refreshUndoMenuItems() {
        LC.editUndo.setEnabled(LC.codeEditor.canUndo());
        LC.Undo.setEnabled(LC.codeEditor.canUndo());
        LC.editUndo.setText(LC.codeEditor.getUndoName());
        LC.editRedo.setEnabled(LC.codeEditor.canRedo());
        LC.Redo.setEnabled(LC.codeEditor.canRedo());
        LC.editRedo.setText(LC.codeEditor.getRedoName());
    }
}