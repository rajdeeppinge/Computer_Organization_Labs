
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
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Component;

/**
* TablePanel uses a custom TableModel.
*/
public class TablePanel extends JPanel {
    private boolean DEBUG = false;
    public JTable valTable ;
    public  Object [][]  data= {
    } ; 	    /* store data to be put in memory-table */

    public TablePanel(Object [][]tempData, String [] colNames,int [] edivalTableColumnNos) {
        super(new GridLayout(1,0));
        valTable  = new JTable(new MyTable(tempData,colNames,edivalTableColumnNos));
        valTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        JScrollPane scrollPane = new JScrollPane(valTable);
        add(scrollPane);
    }

    /**
    * method to change the column widths in a table
    *
    * @param JTable table and array with least size values for each column
    */
    public void initColumnSizes(JTable valTable,Object[] longValues) {
        MyTable model = (MyTable)valTable.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;

        TableCellRenderer headerRenderer =
        valTable.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < 3; i++) {
            column = valTable.getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(
                null, column.getHeaderValue(), false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            comp = valTable.getDefaultRenderer(model.getColumnClass(i)).
            getTableCellRendererComponent(
                valTable, longValues[i], false, false, 0, i);
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
    * MyTable creates customized valTable .
    */
    public class MyTable extends AbstractTableModel {
        String[] columnNames;
        int[] edivalTableColNos;

        MyTable(Object[][] tempData, String[] colNames, int [] edivalTableColumnNos) {
            columnNames=colNames;
            data=tempData;
            edivalTableColNos=edivalTableColumnNos;
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
        public boolean isCellEdivalTable(int row, int col) {
            boolean flag=false;
            for(int i=0;i<edivalTableColNos.length;i++) {
                if (col==edivalTableColNos[i])
                    flag = true;
            }
            return flag;
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
}