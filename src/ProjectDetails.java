import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ProjectDetails extends JDialog {
    private JPanel ProjectDetailsPanel;
    private JComboBox<String> projectComboBox;
    private JTextArea scheduleArea;
    private JTextArea estimateArea;
    private JButton loadDetailsButton;
    private JScrollPane scheduleScrollPane;
    private JScrollPane estimateScrollPane;

    public ProjectDetails() {
        setContentPane(ProjectDetailsPanel);
        setTitle("Project Details");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);

        loadProjectNames();

        loadDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedProject = (String) projectComboBox.getSelectedItem();
                if (selectedProject != null) {
                    loadProjectDetails(selectedProject);
                }
            }
        });
    }

    private void loadProjectNames() {
        try (Connection connection = Main.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT name FROM project");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                projectComboBox.addItem(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProjectDetails(String projectName) {
        loadSchedule(projectName);
        loadEstimate(projectName);
    }

    private void loadSchedule(String projectName) {
        String query = "SELECT wt.name AS work_type, s.start_date, s.end_date " +
                "FROM project p " +
                "JOIN schedule s ON p.id = s.project_id " +
                "JOIN work_type wt ON s.work_type_id = wt.id " +
                "WHERE p.name = ?";
        try (Connection connection = Main.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, projectName);
            try (ResultSet resultSet = statement.executeQuery()) {
                StringBuilder schedule = new StringBuilder();
                while (resultSet.next()) {
                    schedule.append(" Work Type: ").append(resultSet.getString("work_type")).append("\n")
                            .append(" Start Date: ").append(resultSet.getDate("start_date")).append("\n")
                            .append(" End Date: ").append(resultSet.getDate("end_date")).append("\n");
                }
                scheduleArea.setText(schedule.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadEstimate(String projectName) {
        String query = "SELECT e.material, e.quantity, e.cost " +
                "FROM project p " +
                "JOIN estimate e ON p.id = e.project_id " +
                "WHERE p.name = ?";
        try (Connection connection = Main.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, projectName);
            try (ResultSet resultSet = statement.executeQuery()) {
                StringBuilder estimate = new StringBuilder();
                while (resultSet.next()) {
                    estimate.append("Material: ").append(resultSet.getString("material")).append("\n")
                            .append("Quantity: ").append(resultSet.getInt("quantity")).append("\n")
                            .append("Cost: ").append(resultSet.getBigDecimal("cost")).append("\n");
                }

                estimateArea.setText("Estimate: "+ estimate.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
