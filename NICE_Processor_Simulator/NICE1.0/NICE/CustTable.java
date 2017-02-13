
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.TableModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Component;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
* CustTable uses a custom TableModel.
*/
public class CustTable extends JPanel implements TableModelListener {
    public static final int memSize = (int)Math.pow(2,12); 
    /* # of rows in memory-table = memory size NJ - 27/7/08 */

    private boolean DEBUG = false;
    public JTable table ;        // Why two tables? NJ - Oct 2008
    public MyTable myTab ;
                
    public  Object [][]  data= {
    } ;             /* store data to be put in memory-table */

    /* constructor to setup the JPanel */
    public CustTable(Object[][] tempData, String[] colNames, int[] editableColumnNos) {
        
        super(new GridLayout(1,0));

        /* TableCellRenderer to be called before a cell is rendered from the table */
        table = new JTable(myTab = new MyTable(tempData,colNames,editableColumnNos)) {
            public Component prepareRenderer(TableCellRenderer renderer,int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if(myTab.getColumnClass(vColIndex)==boolean.class) {
                    JCheckBox jcheckbox = new JCheckBox();
                    if(LC.memPanel.myTab.isBreakPointSet(rowIndex)) {
                        /* for showing cell with checkbox with BP set */
                        jcheckbox.setSelected(true);
                        // jcheckbox.setBackground(Color.red); NJ - Oct 2008 
                    }
                    else {
                        jcheckbox.setSelected(false);
                        // jcheckbox.setBackground(getBackground()); NJ - Oct 2008 
                    }
                    return jcheckbox;
                }
                return c;
            }

            public void tableChanged(TableModelEvent tablemodelevent) {
                super.tableChanged(tablemodelevent);
            }
        } ;

        table.getModel().addTableModelListener(this);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
    }

    /**
    * implements tableChanged method of TableModelListener.
    * handles change in table
    *
    * @param TableModelEvent e triggering this call
    */
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel)e.getSource();
        Object data = model.getValueAt(row, column);
        // Insert code here as needed - NJ - Oct 2008
    }

    /**
    * method to change the column widths in a table
    *
    * @param JTable table and array with least size values for each column
    */
    public void initColumnSizes(JTable table,Object[] longValues) {
        MyTable model = (MyTable)table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;

        TableCellRenderer headerRenderer =
        table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < 2; i++) {
            column = table.getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(
            null, column.getHeaderValue(),
            false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            comp = table.getDefaultRenderer(model.getColumnClass(i)).
            getTableCellRendererComponent(
            table, longValues[i],
            false, false, 0, i);
            cellWidth = comp.getPreferredSize().width;

            if (DEBUG) {
                System.out.println("Initializing width of column "
                + i + ". "
                + "headerWidth = " + headerWidth
                + "; cellWidth = " + cellWidth);
            }

            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }

    /**
    * MyTable creates customized table .
    */
    public class MyTable extends AbstractTableModel {
        String[] columnNames;
        int[] editableColNos;

        MyTable(Object[][] tempData, String[] colNames, int [] editableColumnNos) {
            columnNames=colNames;
            data=tempData;
            editableColNos=editableColumnNos;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public boolean isBreakPointSet(int i) {
            Object temp = data[i][0];
            if (temp.toString()=="true") return true; 
            else return false;
        }

        /**
        * method to retrieve a data value from a cell in a table
        *
        * @param row number - row ,and column number - col
        */
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /**
        * JTable uses this method to determine the default renderer/editor for each cell.
        * If we didn't implement this method,
        * then the last column would contain text ("true"/"false"),rather than a check box.
        */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /**
        * This method is implemented to make the cells uneditable by the user.
        *
        * @param row number - row ,and column number - col
        */
        public boolean isCellEditable(int row, int col) {
            boolean flag=false;
            for(int i=0;i<editableColNos.length;i++) {
                if (col==editableColNos[i])
                flag = true;
            }
            return flag;
        }

        public String setBreakPoint(int i) {
            if(i < 0 || i >= 0x1000) {            // Only 2^12 bytes memory - Oct 2008
                return "Error: Invalid address or label";
            }
            else {
                fireTableCellUpdated(i, -1);
                return (new StringBuilder()).append("Breakpoint set at ").append(Long.toHexString(i)).toString();
            }
        }

        /**
        * This method is implemented insert data in table in a cell specified
        *
        * @param Object value to be inserted,row number - row ,and column number - col
        */
        public void setValueAt(Object value, int row, int col) {
            if (DEBUG) {
                System.out.println("Setting value at " + row + "," + col
                + " to " + value
                + " (an instance of "
                + value.getClass().getName() + ")");
            }

            data[row][col] = value;
            fireTableCellUpdated(row, col);

            if (DEBUG) {
                System.out.println("New value of data:");
                printDebugData();
            }
        }

        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i=0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j=0; j < numCols; j++)
                System.out.print("  " + data[i][j]);
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }

    /**
    * This method is is used to pad a string to make it of certain fixed length.
    *
    * @param string to be padded - str, length of to be padded string - padlen, and string
    * with which padding has to be done - pad
    */
    public static  String pad(Object str, int padlen, String pad) {
        String padding = new String();
        int len = Math.abs(padlen) - str.toString().length();
        if (len < 1)
            return str.toString();
        for (int i = 0 ; i < len ; ++i)
            padding = padding + pad;
        return (padlen < 0 ? padding + str : str + padding);
    }
}