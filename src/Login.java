import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login extends JDialog {
    private JPanel LoginPanel;
    private JTextField loginField;
    private JTextField passwordField;
    private JButton buttonLogin;
    private JButton registerButton;
    private JLabel loginLabel;
    private JLabel passwordLabel;

    public Login() {
        setContentPane(LoginPanel);
        setTitle("Login");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(450, 300);
        setLocationRelativeTo(null);
        setVisible(true);

        buttonLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String loginUser = loginField.getText();
                String passwordUser = passwordField.getText();
                User user = checkLogin(loginUser, passwordUser);
                if (user != null) {
                    JOptionPane.showMessageDialog(Login.this, "Login successful!");
                    new ManageData();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(Login.this, "Login failed!");
                }
            }
        });

//        registerButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                new Register(); // Переходимо до форми реєстрації
//            }
//        });
    }

    public User checkLogin(String loginUser, String passwordUser) {
        Connection connection = null;
        User user = null;
        try {
            connection = Main.getConnection();
            String query = "SELECT * FROM `keys` WHERE login = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, loginUser);
            statement.setString(2, passwordUser);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String login = resultSet.getString("login");
                String password = resultSet.getString("password");
                String position = resultSet.getString("position");
                user = new User(id, login, password, position);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Main.closeConnection(connection);
        }
        return user;
    }
}
