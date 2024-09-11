import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ManageData extends JDialog {
    private JPanel ManageDataPanel;

    public ManageData() {
        setContentPane(ManageDataPanel);
        setTitle("Manage Data");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

}