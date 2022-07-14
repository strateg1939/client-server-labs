package client_server.client;

import client_server.exceptions.ClientDataException;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

public class Main extends JFrame {
    protected static JFrame main;
    protected static Storage storage;

    private static JButton add;
    private static JButton remove;
    private static JButton changeGroupsProducts;
    private static JButton search;
    private static JButton statistics;

    private static JScrollPane currentTable;

    protected static Group parameterGroup;
    protected static Product parameterProduct;
    protected static Group parameterStatisticGroup;
    protected static Product parameterStatisticProduct;
    protected static String helper = "";

    protected static Boolean flag;
    protected static Boolean flag2;
    protected static int i1;
    protected static int j1;
    protected static int i2;
    protected static int j2;

    protected static ToSearch searching;
    protected static Double amountCounter = 0.0;
    protected static String headerOfGroupStatistic = "";
    protected static Boolean groupStatistic = false;

    public Main() {
        super("Склад");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        //add menu
        MenuBar menuBar = new MenuBar();
        PopupMenu menu = new PopupMenu("Опції");
        menuBar.add(menu);
        menuBar.setFont(new Font("sans-serif", Font.BOLD, 12));
        this.setMenuBar(menuBar);
        //save option
        //buttons
        add = new JButton("Додати");
        remove = new JButton("Видалити");
        changeGroupsProducts = new JButton("");
        search = new JButton("Пошук");
        statistics = new JButton("Статистика");
        JPanel buttons = new JPanel();
        buttons.add(add);
        buttons.add(remove);
        buttons.add(changeGroupsProducts);
        buttons.add(search);
        buttons.add(statistics);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        setSize(550, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * create a table with all groups
     * change actions on create/delete/change buttons
     */
    public static void getAllGroups() {
        main.setTitle("Склад");
        if (currentTable != null) main.remove(currentTable);
        removeActionListeners();
        DefaultTableModel tableModel = new DefaultTableModel();
        String[] columnsHeader = {"назва", "опис"};
        tableModel.setColumnIdentifiers(columnsHeader);
        for (Group group : storage.getAllGroups()) {
            tableModel.addRow(new String[]{group.name, group.description});
        }
        //listen for changes in table
        tableModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                TableModel model = (TableModel) e.getSource();
                model.removeTableModelListener(this);

                int row = e.getFirstRow();
                //check it is not create/delete
                if (e.getType() != TableModelEvent.UPDATE) {
                    model.addTableModelListener(this);
                    return;
                }

                int column = e.getColumn();
                String data = (String) model.getValueAt(row, column);

                boolean sameName = false;
                for (int i = 0; i < storage.getAllGroups().size(); i++) {
                    if (data.equals(storage.getAllGroups().get(i).name)) {
                        sameName = true;
                        break;
                    }
                }
                Group group = storage.getAllGroups().get(row);
                String nameOfGroup = group.name;
                if (column == 0) {
                    if (!sameName) {
                        try {
                            storage.updateGroup(group);
                            group.name = data;
                        } catch (ClientDataException clientDataException) {
                            JOptionPane.showMessageDialog(main, clientDataException.getMessage());
                        }
                    } else {
                        JOptionPane.showMessageDialog(main, "Таке ім'я групи товарів вже існує!");
                        model.setValueAt(nameOfGroup, row, column);
                    }
                } else {
                    try {
                        storage.updateGroup(group);
                        group.description = data;
                    } catch (ClientDataException clientDataException) {
                        JOptionPane.showMessageDialog(main, clientDataException.getMessage());
                    }
                }
                model.addTableModelListener(this);
            }
        });
        JTable table = new JTable(tableModel);
        //add new group
        add.addActionListener(e -> {

            JPanel addPanel = new JPanel();
            JTextField nameField = new JTextField(10);
            JTextField descriptionField = new JTextField(20);
            addPanel.add(new JLabel("Ім'я"));
            addPanel.add(nameField);
            addPanel.add(new JLabel("Опис"));
            addPanel.add(descriptionField);
            int result = JOptionPane.showConfirmDialog(main, addPanel,
                    "Нова група", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                Group group = new Group(nameField.getText(), descriptionField.getText());
                boolean sameName = false;
                for (int i = 0; i < storage.getAllGroups().size(); i++) {
                    if (group.name.equals(storage.getAllGroups().get(i).name)) {
                        sameName = true;
                        break;
                    }
                }

                if (!sameName) {
                    try {
                        storage.addGroup(group);
                        tableModel.addRow(new String[]{
                                group.name,
                                group.description});
                    } catch (ClientDataException clientDataException) {
                        JOptionPane.showMessageDialog(main, clientDataException.getMessage());
                    }

                } else {
                    JOptionPane.showMessageDialog(main, "Таке ім'я групи товарів вже існує!");
                }

            }
        });
        //remove
        remove.addActionListener(e -> {
            int idx = table.getSelectedRow();
            if (idx < 0) return;
            try {
                storage.deleteGroup(storage.getAllGroups().get(idx), idx);
                tableModel.removeRow(idx);
            } catch (ClientDataException clientDataException) {
                JOptionPane.showMessageDialog(main, clientDataException.getMessage());
            }
        });
        //go to the selected groups products

        //Пошук
        search.addActionListener(e -> {
            JPanel addPanel = new JPanel();
            JTextField nameField = new JTextField(10);
            addPanel.add(new JLabel("Ім'я"));
            addPanel.add(nameField);

            int result = JOptionPane.showConfirmDialog(main, addPanel,
                    "Пошук товару", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                flag = false;
                for (int i = 0; i < storage.getAllGroups().size(); i++) {
                    for (int j = 0; j < storage.getAllGroups().get(i).products.size(); j++) {
                        // if (nameField.getText().contains(s.AllGroups.get(i).products.get(j).getName())){
                        boolean flag2 = true;
                        for (int u = 0; u < nameField.getText().length() && u < storage.getAllGroups().get(i).products.get(j).getName().length(); u++) {
                            if (!(nameField.getText().charAt(u) == (storage.getAllGroups().get(i).products.get(j).getName().charAt(u))))
                                flag2 = false;
                        }
                        if (flag2) {
                            //System.out.println(s.AllGroups.get(i).products.get(j).toString());
                            parameterGroup = storage.getAllGroups().get(i);
                            parameterProduct = storage.getAllGroups().get(i).products.get(j);
                            helper = storage.getAllGroups().get(i).products.get(j).getName();
                            i1 = i;
                            j1 = j;
                            if (!Main.flag) {
                                searching = new ToSearch();
                            } else {
                                if (Main.parameterProduct.name.contains(Main.helper)) {
                                    ToSearch.tableModel.addRow(new String[]{Main.parameterProduct.name, Main.parameterGroup.name, Main.parameterProduct.description, Main.parameterProduct.producer,
                                            ((Integer) Main.parameterProduct.amount).toString(), ((Double) Main.parameterProduct.price).toString()});
                                }
                            }
                            flag = true;
                        }

                    }
                }
                if (!flag) JOptionPane.showMessageDialog(main, "Нічого не знайдено!");

            }
        });

        statistics.addActionListener(e -> {
            flag2 = false;
            for (int i = 0; i < storage.getAllGroups().size(); i++) {
                for (int j = 0; j < storage.getAllGroups().get(i).products.size(); j++) {
                    parameterStatisticGroup = storage.getAllGroups().get(i);
                    parameterStatisticProduct = storage.getAllGroups().get(i).products.get(j);
                    i2 = i;
                    j2 = j;

                    if (!flag2) {
                        new MakeStatistics();
                        flag2 = true;
                    } else {
                        MakeStatistics.rows.add(new String[]{
                                Main.parameterStatisticProduct.name,
                                Main.parameterStatisticGroup.name,
                                String.valueOf((Double) (Main.parameterStatisticProduct.amount * Main.parameterStatisticProduct.price)),
                                ((Integer) Main.parameterStatisticProduct.amount).toString(),
                                ((Double) Main.parameterStatisticProduct.price).toString()});
                    }
                }
            }
            ArrayList<String[]> rows2 = new ArrayList<>();
            ArrayList<Double> amount = new ArrayList<>();
            amountCounter = 0.0;
            for (int j = 0; j < MakeStatistics.rows.size(); j++) {
                amount.add(Double.valueOf(MakeStatistics.rows.get(j)[2]));
                amountCounter += amount.get(j);
            }

            amount.sort((x, y) -> (x > y) ? 1 : -1);

            for (Double aDouble : amount) {
                for (int j = 0; j < MakeStatistics.rows.size(); j++) {
                    if (aDouble >= (Double.parseDouble(MakeStatistics.rows.get(j)[2]))) {
                        rows2.add(0, MakeStatistics.rows.get(j));
                        MakeStatistics.rows.remove(j);
                    }
                }

            }

            for (String[] strings : rows2) {
                MakeStatistics.tableModelS2.addRow(strings);
            }


        });


        changeGroupsProducts.setText("Перейти до товарів");
        changeGroupsProducts.addActionListener(e -> {
            int idx = table.getSelectedRow();
            if (idx < 0) return;
            Group selectedGroup = storage.getAllGroups().get(idx);
            try {
                storage.loadProducts(selectedGroup);
                getProductTable(selectedGroup);
            } catch (ClientDataException clientDataException) {
                JOptionPane.showMessageDialog(main, clientDataException.getMessage());
            }
        });


        currentTable = new JScrollPane(table);
        main.getContentPane().add(currentTable, BorderLayout.CENTER);
        //!!!! always call this when removing and adding new components
        main.getContentPane().repaint();
        main.getContentPane().revalidate();
    }

    /**
     * delete all listeners
     */
    private static void removeActionListeners() {
        for (ActionListener a :
                add.getActionListeners()) {
            add.removeActionListener(a);
        }
        for (ActionListener a :
                remove.getActionListeners()) {
            remove.removeActionListener(a);
        }
        for (ActionListener a :
                changeGroupsProducts.getActionListeners()) {
            changeGroupsProducts.removeActionListener(a);
        }
        for (ActionListener a :
                search.getActionListeners()) {
            search.removeActionListener(a);
        }
        for (ActionListener a :
                statistics.getActionListeners()) {
            statistics.removeActionListener(a);
        }
    }

    /**
     * adds selected group`s table
     * change actions on create/delete/change buttons
     *
     * @param group
     */

    private static void getProductTable(Group group) {
        main.setTitle("Товари з групи " + group.name);
        removeActionListeners();
        if (currentTable != null) main.remove(currentTable);
        DefaultTableModel tableModel = new DefaultTableModel();
        String[] columnsHeader = {"назва", "опис", "виробник", "кількість", "ціна"};
        tableModel.setColumnIdentifiers(columnsHeader);
        for (Product product :
                group.products) {
            tableModel.addRow(new String[]{product.name, product.description, product.producer, ((Integer) product.amount).toString(), ((Double) product.price).toString()});
        }
        //change products when table data is edited
        tableModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                TableModel model = (TableModel) e.getSource();
                model.removeTableModelListener(this);
                int row = e.getFirstRow();
                //check it is not create/delete
                if (e.getType() != TableModelEvent.UPDATE) {
                    model.addTableModelListener(this);
                    return;
                }
                int column = e.getColumn();
                String data = (String) model.getValueAt(row, column);
                Product product = group.products.get(row);
                switch (column) {

                    case 0 -> {
                        boolean sameName = false;
                        for (int i = 0; i < storage.getAllGroups().size(); i++) {
                            for (int j = 0; j < storage.getAllGroups().get(i).products.size(); j++) {
                                if (data.equals(storage.getAllGroups().get(i).products.get(j).getName())) {
                                    sameName = true;
                                    break;
                                }
                            }
                        }
                        if (!sameName) {
                            product.name = data;
                        } else {
                            JOptionPane.showMessageDialog(main, "Таке ім'я товару вже існує!");
                            model.setValueAt(product.name, row, column);
                        }
                    }
                    case 1 -> product.description = data;
                    case 2 -> product.producer = data;
                    case 3 -> {
                        Integer value = getInteger(main, data);
                        if (value == null) model.setValueAt(Integer.toString(product.amount), row, column);
                        else product.amount = value;
                    }
                    case 4 -> {
                        Double value = getDouble(main, data);
                        if (value == null) model.setValueAt(Double.toString(product.price), row, column);
                        else product.price = value;
                    }
                }
                try {
                    storage.updateProduct(product);
                } catch (ClientDataException clientDataException) {
                    JOptionPane.showMessageDialog(main, clientDataException.getMessage());
                }
                model.addTableModelListener(this);
            }
        });
        JTable table = new JTable(tableModel);
        //add new product
        add.addActionListener(e -> {
            JPanel addPanel = new JPanel();
            JTextField nameField = new JTextField(10);
            JTextField descriptionField = new JTextField(20);
            JTextField producerField = new JTextField(10);
            JTextField priceField = new JTextField("0.0", 10);
            JTextField amountField = new JTextField("0", 5);
            addPanel.add(new JLabel("Ім'я"));
            addPanel.add(nameField);
            addPanel.add(new JLabel("Опис"));
            addPanel.add(descriptionField);
            addPanel.add(new JLabel("Виробник"));
            addPanel.add(producerField);
            addPanel.add(new JLabel("Ціна за одиницю"));
            addPanel.add(priceField);
            addPanel.add(new JLabel("Кількість на складі"));
            addPanel.add(amountField);
            int result = JOptionPane.showConfirmDialog(main, addPanel,
                    "Новий продукт", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                Double price = getDouble(main, priceField.getText());
                Integer amount = getInteger(main, amountField.getText());
                if (price == null || amount == null) return;
                Product product = new Product(nameField.getText(), descriptionField.getText(), producerField.getText(), amount, price, group);
                boolean sameName = false;
                for (int i = 0; i < storage.getAllGroups().size(); i++) {
                    for (int j = 0; j < storage.getAllGroups().get(i).products.size(); j++) {
                        if (product.name.equals(storage.getAllGroups().get(i).products.get(j).getName())) {
                            sameName = true;
                            break;
                        }
                    }
                }
                if (!sameName) {
                    try {
                        storage.addProduct(product, group);
                        tableModel.addRow(new String[]{
                                product.name,
                                product.description,
                                product.producer,
                                amount.toString(),
                                price.toString()});
                    } catch (ClientDataException clientDataException) {
                        JOptionPane.showMessageDialog(main, clientDataException.getMessage());
                    }

                } else {
                    JOptionPane.showMessageDialog(main, "Таке ім'я товару вже існує!");
                }
            }
        });
        //remove
        remove.addActionListener(e -> {
            int idx = table.getSelectedRow();
            if (idx < 0) return;
            Product product = group.products.get(idx);
            try {
                storage.deleteProduct(product, group, idx);
                tableModel.removeRow(idx);
            } catch (ClientDataException clientDataException) {
                JOptionPane.showMessageDialog(main, clientDataException.getMessage());
            }

        });
        changeGroupsProducts.setText("Повернутися");
        changeGroupsProducts.addActionListener(e -> getAllGroups());

        //group statistics
        statistics.addActionListener(e -> {
            flag2 = false;
            headerOfGroupStatistic = (" групи " + group.name);
            for (int j = 0; j < group.products.size(); j++) {
                parameterStatisticGroup = group;
                parameterStatisticProduct = group.products.get(j);
                i2 = 1;
                j2 = j;
                groupStatistic = true;

                if (!flag2) {
                    new MakeStatistics();
                    flag2 = true;
                    headerOfGroupStatistic = "";
                } else {
                    MakeStatistics.rows.add(new String[]{
                            Main.parameterStatisticProduct.name,
                            Main.parameterStatisticGroup.name,
                            String.valueOf((Double) (Main.parameterStatisticProduct.amount * Main.parameterStatisticProduct.price)),
                            ((Integer) Main.parameterStatisticProduct.amount).toString(),
                            ((Double) Main.parameterStatisticProduct.price).toString()});
                }
            }

            ArrayList<String[]> rows2 = new ArrayList<>();
            ArrayList<Double> amount = new ArrayList<>();
            for (int j = 0; j < MakeStatistics.rows.size(); j++) {
                amount.add(Double.valueOf(MakeStatistics.rows.get(j)[2]));
            }
            amount.sort((x, y) -> (x > y) ? 1 : -1);
            //System.out.println(amount.toString());
            for (Double aDouble : amount) {
                for (int j = 0; j < MakeStatistics.rows.size(); j++) {
                    if (aDouble >= (Double.parseDouble(MakeStatistics.rows.get(j)[2]))) {
                        rows2.add(0, MakeStatistics.rows.get(j));
                        MakeStatistics.rows.remove(j);
                    }
                }

            }

            for (String[] strings : rows2) {
                MakeStatistics.tableModelS2.addRow(strings);
            }
        });

        currentTable = new JScrollPane(table);
        main.getContentPane().add(currentTable, BorderLayout.CENTER);
        //!!!! always call this when removing and adding new components
        main.getContentPane().repaint();
        main.getContentPane().revalidate();
    }

    public static void main(String[] args) {
        main = new Main();
        storage = new Storage();
        try {
            storage.loadGroups();
        } catch (ClientDataException e) {
            JOptionPane.showMessageDialog(main, e.getMessage());
        }
        getAllGroups();
        /*
        if (statusCode != -1) {
            getAllGroups();

            main.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    int answer = JOptionPane.showConfirmDialog(main, "Чи бажаєте зберегти?");
                    if (answer == JOptionPane.OK_CANCEL_OPTION || answer == JOptionPane.CLOSED_OPTION) return;
                    if (answer == JOptionPane.OK_OPTION) {
                        storage.saveGroups();
                    } else if (statusCode != 1) {
                        File file = new File(Storage.groupFileName);
                        file.delete();
                    }
                    main.setVisible(false);
                    main.dispose();
                }
            });
        } else {
            main.setVisible(false);
            main.dispose();
        }
        /*
         */
    }

    /**
     * gets integer from input, shows error message if it is wrong
     *
     * @param parent parentComponent
     * @param input  string
     * @return int or null
     */
    public static Integer getInteger(Component parent, String input) {
        int integer;
        try {
            integer = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parent, "Неправильно введене ціле число", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (integer < 0) {
            JOptionPane.showMessageDialog(parent, "Число не менше 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return integer;
    }

    /**
     * gets double from input, shows error message if it is wrong
     *
     * @param parent parentComponent
     * @param input  string
     * @return double or null
     */
    public static Double getDouble(Component parent, String input) {
        double number;
        try {
            number = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parent, "Неправильно введене дробове число", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (number < 0) {
            JOptionPane.showMessageDialog(parent, "Число не менше 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return number;
    }


}

