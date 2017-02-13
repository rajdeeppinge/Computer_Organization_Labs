
/**
 *   class FileManager (Helper Class)
 *   This class opens and saves a textfile.
 *
 */
import java.io.*;
import javax.swing.*;

class FileManager  {

//----------------------------------
//    Data Members
//----------------------------------

    /** Constant for empty string */
    private static final String EMPTY_STRING = "";

    /** Generic filepath separator */
    private static String fileSeparator  = System.getProperty("file.separator");

    /** Generic line terminator */
    private static String lineTerminator = System.getProperty("line.separator");

//----------------------------------
//    Constructors
//----------------------------------

    /**
     * Default constructor
     */
    public FileManager( ) {

    }

//----------------------------------
//    Private Methods
//
//          String open (           )
//          String open ( String    )
//
//----------------------------------

    /**
     * Lets the end user select a file with
     * a file dialog. If a file cannot be
     * opened for whatever reason, an empty
     * String is returned.
     *
     * @return content of the opened file
     */
    public String openFile( ) throws FileNotFoundException,
                                 IOException {
        String filename, doc = EMPTY_STRING;

        JFileChooser chooser = new JFileChooser();
        int reply = chooser.showOpenDialog(null);

        if(reply == JFileChooser.APPROVE_OPTION) {

              doc = openFile(chooser.getSelectedFile().getAbsolutePath());
        }

        return doc;
    }

    /**
     * Opens the designated textfile and retuns the
     * whole file content as a single String object.
     * If the file contains nothing, an empty String
     * "" is returned.
     *
     * @param filename name of the textfile to open
     *
     * @return content of the opened file
     */
    public String openFile(String filename)
                throws FileNotFoundException, IOException {

        String         line;
        StringBuffer   document = new StringBuffer(EMPTY_STRING);

        File           inFile     = new File(filename);
        FileReader     fileReader = new FileReader(inFile);
        BufferedReader bufReader  = new BufferedReader(fileReader);

        while (true) {
            line = bufReader.readLine();

            if (line == null) break;

            document.append(line + lineTerminator);
        }

        return document.toString();
    }


    /**
     * Lets the end user select a file with
     * a file dialog. If a file cannot be
     * saved for whatever reason, an exception
     * is thrown.
     *
     * @param data text data to be saved
     *
     * @exception IOException
     */
    public String saveFile(String data) throws IOException {
        String fileName="Untitled", doc = EMPTY_STRING;

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new SourceFileFilter());
        int reply = chooser.showSaveDialog(null);

        if(reply == JFileChooser.APPROVE_OPTION) {

             saveFile(chooser.getSelectedFile().getAbsolutePath(),
                     data);
             fileName=chooser.getSelectedFile().getAbsolutePath();
        }
        return fileName;
    }

    /**
     * Saves the data to the designated textfile.
     * If the file contains nothing, an empty String
     * "" is returned.
     *
     * @param data text data to be saved
     *
     * @exception IOException
     */
    public void saveFile(String filename, String data)
                throws IOException {
        File              outFile       = new File(filename);
        FileOutputStream  outFileStream = new FileOutputStream(outFile);
        PrintWriter       outStream     = new PrintWriter(outFileStream);

        outStream.print(data);

        outStream.close();
    }

}
