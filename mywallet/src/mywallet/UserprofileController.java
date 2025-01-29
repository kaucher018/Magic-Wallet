package mywallet;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.scene.control.ChoiceDialog;

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
    @FXML
    private Button viewTransactions;
    @FXML
    private Button setLimit;
    @FXML
    private Button addBeneficiary;
    @FXML
    private Button viewBeneficiaries;

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

            // Check daily spending limit
            String limitQuery = "SELECT daily_limit FROM spending_limits WHERE user_id = ?";
            PreparedStatement limitStmt = conn.prepareStatement(limitQuery);
            limitStmt.setInt(1, userId);
            ResultSet limitRs = limitStmt.executeQuery();

            if (limitRs.next()) {
                double dailyLimit = limitRs.getDouble("daily_limit");
                if (amount > dailyLimit) {
                    showAlert("Error", "Transaction failed: Amount exceeds your daily limit.");
                    return;
                }
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

                    // Save transaction record
                    String transactionQuery = "INSERT INTO transactions (user_id, amount, transaction_id) VALUES (?, ?, ?)";
                    PreparedStatement transactionStmt = conn.prepareStatement(transactionQuery);
                    transactionStmt.setInt(1, userId);
                    transactionStmt.setDouble(2, amount);
                    transactionStmt.setString(3, transactionId);
                    transactionStmt.executeUpdate();

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
    private void viewTransactionHistory() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT transaction_id, amount FROM transactions WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder("Transaction History:\n");
            while (rs.next()) {
                sb.append("Transaction ID: ").append(rs.getString("transaction_id"))
                        .append("|| Amount: ").append(rs.getDouble("amount")).append("\n");
            }

            showAlert("Transaction History", sb.toString());
        } catch (SQLException e) {
            showAlert("Error", "Failed to load transactions: " + e.getMessage());
        }
    }
    

    @FXML
    private void setSpendingLimit() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Set Spending Limit");
        dialog.setHeaderText("Enter your daily spending limit:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(input -> {
            try (Connection conn = DatabaseHelper.getConnection()) {
                double limit = Double.parseDouble(input);
                if (limit <= 0) {
                    showAlert("Error", "Spending limit must be greater than 0.");
                    return;
                }

                String query = "INSERT INTO spending_limits (user_id, daily_limit) VALUES (?, ?) " +
                               "ON CONFLICT(user_id) DO UPDATE SET daily_limit = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
                stmt.setDouble(2, limit);
                stmt.setDouble(3, limit);
                stmt.executeUpdate();

                showAlert("Success", "Spending limit set successfully.");
            } catch (SQLException | NumberFormatException e) {
                showAlert("Error", "Invalid input: " + e.getMessage());
            }
        });
    }

    @FXML
    private void logout() {
        try {
            userid.getScene().getWindow().hide();
            DatabaseHelper.closeConnection(DatabaseHelper.getConnection());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
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

    @FXML
    private void addBeneficiary(ActionEvent event) {
         TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Add Beneficiary");
    dialog.setHeaderText("Enter Beneficiary Email:");
    Optional<String> result = dialog.showAndWait();

    result.ifPresent(email -> {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "INSERT INTO beneficiaries (user_id, beneficiary_email) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setString(2, email);
            stmt.executeUpdate();

            showAlert("Success", "Beneficiary added successfully.");
        } catch (SQLException e) {
            showAlert("Error", "Could not add beneficiary: " + e.getMessage());
        }
    });
    }
   @FXML
private void viewBeneficiaries() {
    try (Connection conn = DatabaseHelper.getConnection()) {
        if (conn == null) {
            showAlert("Error", "Database connection failed.");
            return;
        }

        // Fetch all beneficiaries for the logged-in user
        String query = "SELECT beneficiary_email FROM beneficiaries WHERE user_id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();

        // Store beneficiaries in a list
        List<String> beneficiaryList = new ArrayList<>();
        while (rs.next()) {
            beneficiaryList.add(rs.getString("beneficiary_email"));
        }

        if (beneficiaryList.isEmpty()) {
            showAlert("No Beneficiaries", "You have no saved beneficiaries.");
            return;
        }

        // Show a choice dialog for selection
        ChoiceDialog<String> dialog = new ChoiceDialog<>(beneficiaryList.get(0), beneficiaryList);
        dialog.setTitle("Select Beneficiary");
        dialog.setHeaderText("Choose a beneficiary to send money:");
        dialog.setContentText("Select a beneficiary:");

        Optional<String> result = dialog.showAndWait();

        // If user selects a beneficiary, set it in the transaction account field
        result.ifPresent(selectedBeneficiary -> transnum.setText(selectedBeneficiary));

    } catch (SQLException e) {
        showAlert("Error", "Failed to retrieve beneficiaries: " + e.getMessage());
    }
}


}
