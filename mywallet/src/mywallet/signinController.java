package mywallet;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.Parent;
import javafx.scene.control.Button;

public class signinController {
    @FXML
    private TextField email;
    @FXML
    private TextField password;
    @FXML
    private TextField confrimpassword;
    @FXML
    private Button singupbtn;

    @FXML
    private void singup(ActionEvent event) {
        // Validate inputs
        String userEmail = email.getText().trim();
        String userPassword = password.getText().trim();
        String confirmPassword = confrimpassword.getText().trim();

        if (userEmail.isEmpty() || userPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Sign Up Failed", "All fields are required.");
            return;
        }

        if (!userPassword.equals(confirmPassword)) {
            showAlert("Sign Up Failed", "Passwords do not match.");
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            // Check if email already exists
            String checkQuery = "SELECT * FROM users WHERE email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, userEmail);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                showAlert("Sign Up Failed", "Email already exists.");
                return;
            }

            // Insert new user
            String query = "INSERT INTO users (email, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, userEmail);
            stmt.setString(2, userPassword);
            stmt.executeUpdate();

            showAlert2("Sign Up Successful", "Sign Up Successful! Now you can log in.");

            // Redirect to login page
            Stage stage = (Stage) email.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("login.fxml"))));

        } catch (SQLException e) {
            showAlert("Database Error", "Error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }

    private void showAlert2(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    private void login(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
