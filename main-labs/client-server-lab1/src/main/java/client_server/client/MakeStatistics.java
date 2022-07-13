package client_server.client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class MakeStatistics extends JFrame {
    public static ArrayList<String[]> rows;
    protected static DefaultTableModel tableModelS2;

    public MakeStatistics() {
        super("Статистика" + Main.headerOfGroupStatistic);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        double counter = 0;
        if (!Main.groupStatistic) {
            for (int u = 0; u < Main.storage.getAllGroups().size(); u++) {
                for (int w = 0; w < Main.storage.getAllGroups().get(u).products.size(); w++) {
                    counter += Main.storage.getAllGroups().get(u).products.get(w).amount * Main.storage.getAllGroups().get(u).products.get(w).price;
                }
            }
        } else {

            for (int w = 0; w < Main.parameterStatisticGroup.products.size(); w++) {
                counter += Main.parameterStatisticGroup.products.get(w).amount * Main.parameterStatisticGroup.products.get(w).price;
            }
        }

        //add menu
        MenuBar menuBar = new MenuBar();
        PopupMenu menu2 = new PopupMenu("Всього товарів на складі на суму: " + counter);
        menuBar.add(menu2);

        this.setMenuBar(menuBar);

        tableModelS2 = new DefaultTableModel();
        String[] columnsHeader = {"назва", "група товарів", "загальна ціна", "кількість", "ціна"};
        tableModelS2.setColumnIdentifiers(columnsHeader);
        rows = new ArrayList<>();
        rows.add(new String[]{Main.parameterStatisticProduct.name, Main.parameterStatisticGroup.name, String.valueOf((Double) (Main.parameterStatisticProduct.amount * Main.parameterStatisticProduct.price)),
                ((Integer) Main.parameterStatisticProduct.amount).toString(), ((Double) Main.parameterStatisticProduct.price).toString()});
        JTable table = new JTable(tableModelS2);
        table.setEnabled(false);
        JScrollPane currentTableS2 = new JScrollPane(table);

        //!!!! always call this when removing and adding new components
        this.getContentPane().repaint();
        this.getContentPane().revalidate();
        this.getContentPane().add(currentTableS2, BorderLayout.CENTER);
        // Main.getProductTable();

        setSize(550, 300);
        setLocationRelativeTo(null);
        setVisible(true);

    }

}

