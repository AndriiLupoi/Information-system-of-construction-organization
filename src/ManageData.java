import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ManageData extends JDialog {
    private JPanel ManageDataPanel;
    private JTextArea queryArea;
    private JButton executeQueryButton;
    private JTable resultTable;
    private JScrollPane resultScrollPane;
    private JButton projectDetailsButton;

    public ManageData() {
        setContentPane(ManageDataPanel);
        setTitle("Manage Data");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);

        executeQueryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = queryArea.getText();
                executeQuery(query);
            }
        });

        projectDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ProjectDetails();
                dispose();
            }
        });
    }

    private void executeQuery(String query) {
        try (Connection connection = Main.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            // Отримання метаданих результатів запиту
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Створення моделі таблиці для відображення результатів
            DefaultTableModel tableModel = new DefaultTableModel();
            for (int i = 1; i <= columnCount; i++) {
                tableModel.addColumn(metaData.getColumnName(i));
            }

            // Додавання рядків до моделі таблиці
            while (resultSet.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = resultSet.getObject(i);
                }
                tableModel.addRow(row);
            }

            // Встановлення моделі таблиці для відображення
            resultTable.setModel(tableModel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
