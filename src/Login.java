import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.Properties;
import javax.mail.Session;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Login extends JDialog {
    private JPanel LoginPanel;
    private JTextField loginField;
    private JTextField passwordField;
    private JButton buttonLogin;
    private JButton forgotPasswordButton;
    private JLabel loginLabel;
    private JLabel passwordLabel;

    public Login() {
        setContentPane(LoginPanel);
        setTitle("Вхід");
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
                if (user != null && "owner".equals(user.getPossition())) {
                    JOptionPane.showMessageDialog(Login.this, "Вхід успішний!");
                    new ProjectDetails();
                    dispose();
                } else if (user != null && "meneger".equals(user.getPossition())) {
                    JOptionPane.showMessageDialog(Login.this, "Вхід успішний!");
                    new ManageData();
                    dispose();
                    
                } else {
                    JOptionPane.showMessageDialog(Login.this, "Помилка входу!");
                }
            }
        });

        forgotPasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String loginUser = loginField.getText();
                Connection connection = null;
                String email = null;
                try {
                    connection = Main.getConnection();
                    String query = "SELECT email FROM `keys` WHERE login = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, loginUser);
                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        email = resultSet.getString("email");
                        if (sendNewPassword(loginUser, email)) {
                            JOptionPane.showMessageDialog(Login.this, "Новий пароль надіслано на вашу електронну пошту.");
                        } else {
                            JOptionPane.showMessageDialog(Login.this, "Помилка при скиданні паролю.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(Login.this, "Логін не знайдено.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    Main.closeConnection(connection);
                }
            }
        });

    }

    // Метод для перевірки логіну
    public User checkLogin(String loginUser, String passwordUser) {
        try (Connection connection = Main.getConnection()) {
            String query = "SELECT * FROM `keys` WHERE login = ? AND password = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, loginUser);
                statement.setString(2, passwordUser);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String login = resultSet.getString("login");
                        String password = resultSet.getString("password");
                        String position = resultSet.getString("position");
                        String email = resultSet.getString("email");
                        return new User(id, login, password, position, email);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    final String from = "lupoy098@gmail.com";
    final String username = "lupoy098@gmail.com";
    final String password = "hrtu pwlp seah cdxx";




    public void sendEmail(String to, String subject, String body) {
        final String from = "lupoy098@gmail.com";
        final String username = "lupoy098@gmail.com";
        final String password = "hrtu pwlp seah cdxx";

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", "smtp.gmail.com");
        properties.setProperty("mail.smtp.port", "587");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            System.out.println("Повідомлення відправлено успішно...");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }



    public boolean updatePassword(String loginUser, String newPassword) {
        Connection connection = null;
        try {
            connection = Main.getConnection();
            String query = "UPDATE `keys` SET password = ? WHERE login = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, newPassword);
            statement.setString(2, loginUser);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Main.closeConnection(connection);
        }
        return false;
    }

    public boolean sendNewPassword(String loginUser, String email) {
        String newPassword = PasswordUtil.generateRandomPassword();
        if (updatePassword(loginUser, newPassword)) {
            String subject = "Ваш новий пароль";
            String messageBody = "Ваш новий пароль: " + newPassword;
            sendEmail(email, subject, messageBody);
            return true;
        }
        return false;
    }


}
