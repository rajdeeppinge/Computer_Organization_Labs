import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
* to filter only .asm and .bin source files for listing in JFileChooser
*/

class SourceFileFilter extends FileFilter {
    private static final String ASM  = "asm";
    private static final String BIN  = "bin";
    private static final char   DOT   = '.';

    /* accepts only directories and files with .asm and .bin extension only */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        if (extension(f).equalsIgnoreCase(ASM)) {
            return true;
        } else if (extension(f).equalsIgnoreCase(BIN)) {
            return true;
        }
        else {
            return false;
        }
    }

    /* returns description of the filtered files */
    public String getDescription( ) {
        return "source files (*.asm,*.bin)";
    }

    /* extracts the extension from the filename
    * @param the file
    * return String of file extension
    */
    private String extension(File f) {
        String filename = f.getName();
        int    loc      = filename.lastIndexOf(DOT);

        if (loc > 0 && loc < filename.length() - 1) {
            return filename.substring(loc+1);
        } else {
            return "";
        }
    }
}