package mywallet;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class UserprofileController {

    @FXML private TextField userid;
    @FXML private TextField currentbalance;
    @FXML private TextField moneyamount;
    private int userId;

    public void setUser(String email, double balance, int id) {
        userid.setText(email);
        currentbalance.setText(String.format("%.2f$", balance));
        this.userId = id;
    }

    public void add() {
        try {
            double amount = Double.parseDouble(moneyamount.getText());

            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:mywallet.db")) {
                String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setDouble(1, amount);
                stmt.setInt(2, userId);
                stmt.executeUpdate();

                currentbalance.setText(String.format("%.2f$", Double.parseDouble(currentbalance.getText().replace("$", "")) + amount));
                moneyamount.clear();
                showAlert("Success", "Money added successfully.");
            }
        } catch (Exception e) {
            showAlert("Error", "Invalid amount.");
            e.printStackTrace();
        }
    }

    public void logout() {
        userid.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}
