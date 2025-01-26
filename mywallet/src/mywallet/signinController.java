package mywallet;

import java.awt.Image;
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
        if (!password.getText().equals(confrimpassword.getText())) {
            showAlert("Sign Up Failed", "Passwords do not match.");
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "INSERT INTO users (email, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email.getText());
            stmt.setString(2, password.getText());
            stmt.executeUpdate();
             showAlert2("Sign Up Successfull", "Sign Up Successfull,Now you can login!.");

            Stage stage = (Stage) email.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("login.fxml"))));
        } catch (Exception e) {
            showAlert("Sign Up Failed", "Email already exists.");
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
            // Load the login.fxml file
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    }

