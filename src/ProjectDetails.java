import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.toedter.calendar.JDateChooser;

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
        queryType.addItem("Будівельні керування та їх керівники");

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
        } else if (selectedQueryType.equals("Будівельні керування та їх керівники")) {
            loadBuildingManagementsForComboBox();
        }
    }

    private void loadBuildingManagementsForComboBox() {
        System.out.println("Loading building managements for combo box."); // Налагодження
        try (Connection connection = Main.getConnection()) {
            if (connection == null) {
                System.err.println("Failed to establish a database connection.");
                return;
            }

            String query = "SELECT name FROM building_management";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.isBeforeFirst()) { // Перевірка, чи є результати
                    System.out.println("No data found for building managements.");
                }

                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    projectComboBox.addItem(name);
                    System.out.println("Added to combo box: " + name); // Налагодження
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Error: " + e.getMessage()); // Налагодження
        }
    }

    // Приклад використання методу для відображення результатів
    private String displayBuildingManagements() {
        String managements = loadBuildingManagements();
        detailsArea.setText(managements);
        return managements;
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

    private void showDateInputDialog(String projectName, String queryType) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));

        // Створюємо календарі для вибору початкової і кінцевої дати
        JDateChooser startDateChooser = new JDateChooser();
        JDateChooser endDateChooser = new JDateChooser();

        panel.add(new JLabel("Дата початку:"));
        panel.add(startDateChooser);
        panel.add(new JLabel("Дата завершення:"));
        panel.add(endDateChooser);

        int option = JOptionPane.showConfirmDialog(this, panel, "Виберіть дати", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            Date startDate = startDateChooser.getDate();
            Date endDate = endDateChooser.getDate();
            loadBrigadeDetails(projectName, startDate, endDate);
        }
    }



    // Завантажуємо деталі проекту в залежності від вибраного типу запиту
    private void loadProjectDetails(String projectName, String queryType) {
        if (queryType.equals("Розклад")) {
            detailsArea.setText(loadSchedule(projectName));
        } else if (queryType.equals("Кошторис")) {
            detailsArea.setText(loadEstimate(projectName));
        } else if (queryType.equals("Будівельні керування та їх керівники")){
            detailsArea.setText(displayBuildingManagements());
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
                estimate.append(" Матеріал: ").append(resultSet.getString("material")).append(",")
                        .append(" Кількість: ").append(resultSet.getInt("quantity")).append(",")
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

    private String loadBuildingManagements() {
        String query = "SELECT bm.name AS management_name, s.name AS site_name, e.name AS leader_name " +
                "FROM building_management bm " +
                "JOIN site s ON bm.id = s.management_id " +
                "JOIN brigade b ON s.id = b.site_id " +
                "JOIN employee e ON b.leader_id = e.id " +
                "WHERE e.position = 'Керівник'";
        StringBuilder result = new StringBuilder("Будівельні керування та їх керівники:\n");
        try (Connection connection = Main.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                result.append("Керування: ").append(resultSet.getString("management_name")).append("\n")
                        .append("Ділянка: ").append(resultSet.getString("site_name")).append("\n")
                        .append("Керівник: ").append(resultSet.getString("leader_name")).append("\n\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result.toString();
    }


}