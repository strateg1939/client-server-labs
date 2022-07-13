package client_server.client;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;


public class ToSearch extends JFrame {
    protected static DefaultTableModel tableModel;

    public ToSearch() {

        super("Результати пошуку");
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        //add menu
        MenuBar menuBar = new MenuBar();
        PopupMenu menu = new PopupMenu("Опції");
        menuBar.add(menu);
        this.setMenuBar(menuBar);

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && column != 1;
            }
        };

        String[] columnsHeader = {"назва", "група", "опис", "виробник", "кількість", "ціна"};
        tableModel.setColumnIdentifiers(columnsHeader);

        if (!Main.flag) {
            if (Main.parameterProduct.name.equals(Main.helper)) {
                tableModel.addRow(new String[]{Main.parameterProduct.name,
                        Main.parameterGroup.name,
                        Main.parameterProduct.description,
                        Main.parameterProduct.producer,
                        ((Integer) Main.parameterProduct.amount).toString(),
                        ((Double) Main.parameterProduct.price).toString()});
            }
        }


        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                //check it is not create/delete
                if (e.getType() != TableModelEvent.UPDATE) return;
                int column = e.getColumn();

                TableModel model = (TableModel) e.getSource();
                String data = (String) model.getValueAt(row, column);


                // we search group which cell we are editing right now
                Group thisGroupCounter = null;
                for (int i = 0; i < Main.storage.getAllGroups().size(); i++) {
                    if (tableModel.getValueAt(row, 1).equals(Main.storage.getAllGroups().get(i).name)) {
                        thisGroupCounter = Main.storage.getAllGroups().get(i);
                        break;
                    }
                }
                // we search product which cell we are editing right now by name of the product
                Product thisProductCounter = null;
                for (int i = 0; i < thisGroupCounter.products.size(); i++) {
                    if (tableModel.getValueAt(row, 0).equals(thisGroupCounter.products.get(i).name)) {
                        thisProductCounter = thisGroupCounter.products.get(i);
                        break;
                    }
                }

                model.removeTableModelListener(this);

                switch (column) {
                    //column 0 and 1 are not editable
                    case 2 -> thisProductCounter.description = data;
                    case 3 -> thisProductCounter.producer = data;
                    case 4 -> {
                        Integer value = Main.getInteger(Main.main, data);
                        if (value == null) model.setValueAt(Integer.toString(thisProductCounter.amount), row, column);
                        else thisProductCounter.amount = value;
                    }
                    case 5 -> {
                        Double value = Main.getDouble(Main.main, data);
                        if (value == null) model.setValueAt(Double.toString(thisProductCounter.price), row, column);
                        else thisProductCounter.price = value;
                    }
                }
                model.addTableModelListener(this);
            }
        });


        JTable table = new JTable(tableModel);
        JScrollPane currentTable = new JScrollPane(table);

        // this.getContentPane().add(currentTable, BorderLayout.CENTER);
        //!!!! always call this when removing and adding new components
        this.getContentPane().repaint();
        this.getContentPane().revalidate();
        this.getContentPane().add(currentTable, BorderLayout.CENTER);
        // Main.getProductTable();


        setSize(550, 300);
        setLocationRelativeTo(null);
        setVisible(true);

    }


}

