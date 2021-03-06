package org.ui.views.list.orders;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;

/**
 * The ButtonColumn class provides a renderer and an editor that looks like a
 * JButton. The renderer and editor will then be used for a specified column
 * in the table. The TableModel will contain the String to be displayed on
 * the button.
 * <p>
 * The button can be invoked by a mouse click or by pressing the space bar
 * when the cell has focus. Optionally a mnemonic can be set to invoke the
 * button. When the button is invoked the provided Action is invoked. The
 * source of the Action will be the table. The action command will contain
 * the model row number of the button that was clicked.
 */
public class ButtonColumn extends AbstractCellEditor
        implements TableCellRenderer, TableCellEditor, ActionListener, MouseListener {
    private static final int MAX_WIDTH = 40;
    private static String TEXT = "X";
    private JTable table;
    private Action action;
    private int mnemonic;
    private Border originalBorder;
    private Border focusBorder;
    private JButton renderButton;
    private JButton editButton;
    private Object editorValue;

    /**
     * Create the ButtonColumn to be used as a renderer and editor. The
     * renderer and editor will automatically be installed on the TableColumn
     * of the specified column.
     *
     * @param table  the table containing the button renderer/editor
     * @param action the Action to be invoked when the button is invoked
     * @param column the column to which the button renderer/editor is added
     */
    public ButtonColumn(JTable table, Action action, int column) {
        this.table = table;
        this.action = action;

        renderButton = new JButton();
        editButton = new JButton();
        editButton.setFocusPainted(false);
        editButton.addActionListener(this);
        originalBorder = editButton.getBorder();

        Dimension dim = new Dimension(MAX_WIDTH, MAX_WIDTH);
        renderButton.setMaximumSize(dim);
        editButton.setMaximumSize(dim);
        renderButton.setPreferredSize(dim);
        editButton.setPreferredSize(dim);
        editButton.setMinimumSize(dim);
        renderButton.setMinimumSize(dim);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(column).setMaxWidth(MAX_WIDTH); // new
        columnModel.getColumn(column).setCellRenderer(this);
        columnModel.getColumn(column).setCellEditor(this);
    }

    public void resize() {
        Dimension dim = new Dimension(MAX_WIDTH, MAX_WIDTH);
        renderButton.setMaximumSize(dim);
        editButton.setMaximumSize(dim);
        renderButton.setPreferredSize(dim);
        editButton.setPreferredSize(dim);
        editButton.setMinimumSize(dim);
        renderButton.setMinimumSize(dim);
        editButton.setSize(dim);
        renderButton.setSize(dim);
    }

    /**
     * Get foreground color of the button when the cell has focus
     *
     * @return the foreground color
     */
    public Border getFocusBorder() {
        return focusBorder;
    }

    /**
     * The foreground color of the button when the cell has focus
     *
     * @param focusBorder the foreground color
     */
    public void setFocusBorder(Border focusBorder) {
        this.focusBorder = focusBorder;
        editButton.setBorder(focusBorder);
    }

    public int getMnemonic() {
        return mnemonic;
    }

    /**
     * The mnemonic to activate the button when the cell has focus
     *
     * @param mnemonic the mnemonic
     */
    public void setMnemonic(int mnemonic) {
        this.mnemonic = mnemonic;
        renderButton.setMnemonic(mnemonic);
        editButton.setMnemonic(mnemonic);
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column) {
        if (value == null) {
            editButton.setText("");
            editButton.setIcon(null);
        } else if (value instanceof Icon) {
            editButton.setText("");
            editButton.setIcon((Icon) value);
        } else {
            editButton.setText(TEXT);
            editButton.setIcon(null);
        }

        this.editorValue = value;
        return editButton;
    }

    @Override
    public Object getCellEditorValue() {
        return editorValue;
    }

    //
//  Implement TableCellRenderer interface
//
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            renderButton.setForeground(table.getSelectionForeground());
            renderButton.setBackground(table.getSelectionBackground());
        } else {
            renderButton.setForeground(table.getForeground());
            renderButton.setBackground(UIManager.getColor("Button.background"));
        }

        if (hasFocus) {
            renderButton.setBorder(focusBorder);
        } else {
            renderButton.setBorder(originalBorder);
        }

//		renderButton.setText( (value == null) ? "" : value.toString() );
        if (value == null) {
            renderButton.setText("");
            renderButton.setIcon(null);
        } else if (value instanceof Icon) {
            renderButton.setText("");
            renderButton.setIcon((Icon) value);
        } else {
            renderButton.setText(TEXT);
            renderButton.setIcon(null);
        }

        return renderButton;
    }

    /*
     *	The button has been pressed. Stop editing and invoke the custom Action
	 */
    public void actionPerformed(ActionEvent e) {
        Robot r = null;
        try {
            r = new Robot();
        } catch (AWTException e1) {
            e1.printStackTrace();
        }
        int row = table.convertRowIndexToModel(table.getEditingRow());
        fireEditingCanceled();
        //  Invoke the Action
        ActionEvent event = new ActionEvent(
                table,
                ActionEvent.ACTION_PERFORMED,
                "" + row);
        action.actionPerformed(event);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Robot r = null;
        try {
            r = new Robot();
        } catch (AWTException e1) {
            e1.printStackTrace();
        }
        r.keyPress(KeyEvent.VK_ESCAPE);
        r.keyRelease(KeyEvent.VK_ESCAPE);
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}