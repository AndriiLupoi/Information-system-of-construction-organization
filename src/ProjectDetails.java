import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProjectDetails extends JDialog {
    private JPanel ProjectDetailsPanel;
    private JComboBox<String> queryType; // Комбо бокс для вибору типу запиту
    private JComboBox<String> projectComboBox; // Комбо бокс для проектів
    private JTextArea detailsArea; // Текстова область для виведення даних
    private JButton loadDetailsButton;

    public ProjectDetails() {
        setContentPane(ProjectDetailsPanel);
        setTitle("Деталі проекту");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);

        // Додаємо варіанти в queryType (типи запитів)
        queryType.addItem("Розклад");
        queryType.addItem("Кошторис");
        queryType.addItem("Огляд робіт бригад");

        // Слухач для зміни типу проектів у залежності від вибраного типу запиту
        queryType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateProjectComboBox();
            }
        });

        // Натискання кнопки для завантаження деталей
        loadDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedProject = (String) projectComboBox.getSelectedItem();
                String selectedQueryType = (String) queryType.getSelectedItem();
                if (selectedProject != null && selectedQueryType != null) {
                    if (selectedQueryType.equals("Огляд робіт бригад")) {
                        showDateInputDialog(selectedProject, selectedQueryType);
                    } else {
                        loadProjectDetails(selectedProject, selectedQueryType);
                    }
                }
            }
        });

        // Спочатку завантажуємо проекти для першого типу запиту
        updateProjectComboBox();
    }

    // Метод для оновлення projectComboBox на основі вибраного типу запиту
    private void updateProjectComboBox() {
        projectComboBox.removeAllItems(); // Очищаємо комбо бокс
        String selectedQueryType = (String) queryType.getSelectedItem();
        if (selectedQueryType.equals("Розклад")) {
            loadProjectNamesForSchedule();
        } else if (selectedQueryType.equals("Кошторис")) {
            loadProjectNamesForEstimate();
        } else if (selectedQueryType.equals("Огляд робіт бригад")){
            loadWorkTypes();
        }
    }

    // Завантажуємо імена проектів для розкладу
    private void loadProjectNamesForSchedule() {
        try (Connection connection = Main.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT name FROM project")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                projectComboBox.addItem(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Завантажуємо типи робіт для комбобоксу
    private void loadWorkTypes() {
        try (Connection connection = Main.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT name FROM work_type")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                projectComboBox.addItem(resultSet.getString("name")); // Додаємо тип роботи в JComboBox
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Завантажуємо імена проектів для кошторису
    private void loadProjectNamesForEstimate() {
        try (Connection connection = Main.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT name FROM project")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                projectComboBox.addItem(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Метод для відображення діалогового вікна для введення часу
    private void showDateInputDialog(String projectName, String queryType) {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

        // Додаємо текстові поля для вводу початкової і кінцевої дати
        panel.add(new JLabel("Дата початку (формат dd-MM-yyyy):"));
        JTextField startDateField = new JTextField(20);
        panel.add(startDateField);

        panel.add(new JLabel("Дата завершення (формат dd-MM-yyyy):"));
        JTextField endDateField = new JTextField(20);
        panel.add(endDateField);

        int option = JOptionPane.showConfirmDialog(this, panel, "Введіть дати", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String startDateText = startDateField.getText().trim();
            String endDateText = endDateField.getText().trim();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date startDate = sdf.parse(startDateText);
                Date endDate = sdf.parse(endDateText);
                loadBrigadeDetails(projectName, startDate, endDate);
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Невірний формат дати. Будь ласка, використовуйте формат dd-MM-yyyy.", "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // Завантажуємо деталі проекту в залежності від вибраного типу запиту
    private void loadProjectDetails(String projectName, String queryType) {
        if (queryType.equals("Розклад")) {
            detailsArea.setText(loadSchedule(projectName));
        } else if (queryType.equals("Кошторис")) {
            detailsArea.setText(loadEstimate(projectName));
        }
    }

    private String loadSchedule(String projectName) {
        String query = "SELECT wt.name AS work_type, s.start_date, s.end_date " +
                "FROM project p " +
                "JOIN schedule s ON p.id = s.project_id " +
                "JOIN work_type wt ON s.work_type_id = wt.id " +
                "WHERE p.name = ?";
        StringBuilder schedule = new StringBuilder(" Розклад:\n");
        try (Connection connection = Main.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, projectName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                schedule.append(" Тип роботи: ").append(resultSet.getString("work_type")).append("\n")
                        .append(" Дата початку: ").append(resultSet.getDate("start_date")).append("\n")
                        .append(" Дата закінчення: ").append(resultSet.getDate("end_date")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedule.toString();
    }

    private String loadEstimate(String projectName) {
        String query = "SELECT e.material, e.quantity, e.cost " +
                "FROM project p " +
                "JOIN estimate e ON p.id = e.project_id " +
                "WHERE p.name = ?";
        StringBuilder estimate = new StringBuilder(" Кошторис:\n");
        try (Connection connection = Main.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, projectName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                estimate.append(" Матеріал: ").append(resultSet.getString("material")).append("\n")
                        .append(" Кількість: ").append(resultSet.getInt("quantity")).append("\n")
                        .append(" Вартість: ").append(resultSet.getBigDecimal("cost")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return estimate.toString();
    }

    private void loadBrigadeDetails(String workType, Date startDate, Date endDate) {
        String query = "SELECT b.name AS brigade_name, p.name AS project_name, s.start_date, s.end_date, wt.name AS work_type, site.name AS site_name " +
                "FROM brigade b " +
                "JOIN project p ON b.site_id = p.site_id " +
                "JOIN schedule s ON p.id = s.project_id " +
                "JOIN work_type wt ON s.work_type_id = wt.id " +
                "JOIN site site ON p.site_id = site.id " +
                "WHERE wt.name = ? AND s.start_date >= ? AND s.end_date <= ?";

        try (Connection connection = Main.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, workType);
            statement.setDate(2, new java.sql.Date(startDate.getTime()));
            statement.setDate(3, new java.sql.Date(endDate.getTime()));
            ResultSet resultSet = statement.executeQuery();

            StringBuilder details = new StringBuilder();
            while (resultSet.next()) {
                details.append("Бригада: ").append(resultSet.getString("brigade_name")).append("\n")
                        .append("Проект: ").append(resultSet.getString("project_name")).append("\n")
                        .append("Тип робіт: ").append(resultSet.getString("work_type")).append("\n")
                        .append("Дата початку: ").append(resultSet.getDate("start_date")).append("\n")
                        .append("Дата завершення: ").append(resultSet.getDate("end_date")).append("\n")
                        .append("Об'єкт: ").append(resultSet.getString("site_name")).append("\n\n");
            }
            detailsArea.setText(details.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
