package mywallet;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class UserprofileController {
    private int userId;
    @FXML
    private Label userid;
    @FXML
    private TextField currentbalance;
    @FXML
    private TextField moneyamount;
    @FXML
    private TextField transcash;
    @FXML
    private TextField transnum;
    @FXML
    private Button send;
    @FXML
    private Button out;
    @FXML
    private Button add;

    public void initializeUser(int id, String email, double balance) {
        userId = id;
        userid.setText(email);
        currentbalance.setText(String.valueOf(balance));
    }

    @FXML
    private void add() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            if (conn == null) {
                showAlert("Error", "Database connection failed.");
                return;
            }

            double amount = Double.parseDouble(moneyamount.getText());
            if (amount <= 0) {
                showAlert("Error", "Enter a valid amount greater than 0.");
                return;
            }

            // Update balance in database
            String query = "UPDATE users SET balance = balance + ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Update balance on UI
                currentbalance.setText(String.valueOf(Double.parseDouble(currentbalance.getText()) + amount));
                showAlert("Success", "Money added successfully.");
            } else {
                showAlert("Error", "Failed to add money. Please try again.");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid amount. Please enter a numeric value.");
        } catch (SQLException e) {
            showAlert("Error", "An error occurred while adding money: " + e.getMessage());
        }
    }

    @FXML
    private void send() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            if (conn == null) {
                showAlert("Error", "Database connection failed.");
                return;
            }

            double amount = Double.parseDouble(transcash.getText());
            String transactionId = transnum.getText();

            if (amount <= 0 || transactionId.isEmpty()) {
                showAlert("Error", "Please enter a valid amount and recipient ID.");
                return;
            }

            // Check if recipient exists
            String checkRecipientQuery = "SELECT id FROM users WHERE email = ?";
            PreparedStatement checkRecipientStmt = conn.prepareStatement(checkRecipientQuery);
            checkRecipientStmt.setString(1, transactionId);
            ResultSet rs = checkRecipientStmt.executeQuery();

            if (rs.next()) {
                int recipientId = rs.getInt("id");

                // Deduct money from sender
                String deductQuery = "UPDATE users SET balance = balance - ? WHERE id = ? AND balance >= ?";
                PreparedStatement deductStmt = conn.prepareStatement(deductQuery);
                deductStmt.setDouble(1, amount);
                deductStmt.setInt(2, userId);
                deductStmt.setDouble(3, amount);
                int rowsAffected = deductStmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Add money to recipient
                    String addQuery = "UPDATE users SET balance = balance + ? WHERE id = ?";
                    PreparedStatement addStmt = conn.prepareStatement(addQuery);
                    addStmt.setDouble(1, amount);
                    addStmt.setInt(2, recipientId);
                    addStmt.executeUpdate();

                    // Update sender's balance on UI
                    currentbalance.setText(String.valueOf(Double.parseDouble(currentbalance.getText()) - amount));
                    showAlert("Success", "Money sent successfully.");
                } else {
                    showAlert("Error", "Insufficient balance or failed to send money.");
                }
            } else {
                showAlert("Error", "Recipient not found.");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid amount. Please enter a numeric value.");
        } catch (SQLException e) {
            showAlert("Error", "An error occurred while sending money: " + e.getMessage());
        }
    }

   @FXML
private void logout() {
    try {
        // Close the current stage (user profile window)
        userid.getScene().getWindow().hide();

        // Close the database connection (call the closeConnection method from DatabaseHelper)
        DatabaseHelper.closeConnection(DatabaseHelper.getConnection());

        // Load the login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml")); // Make sure Login.fxml is correct
        Parent root = loader.load();

        // Create a new stage for the login screen
        Stage stage = new Stage();
        stage.setTitle("MyWallet - Login");
        stage.setScene(new Scene(root));
        stage.show();

    } catch (Exception e) {
        showAlert("Error", "An error occurred while logging out: " + e.getMessage());
    }
}

private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setContentText(message);
    alert.show();
}




    }

    
  