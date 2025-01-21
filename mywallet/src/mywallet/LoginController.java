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
import javafx.scene.Parent;

public class LoginController {
    @FXML
    private TextField email;
    @FXML
    private TextField pass;

    @FXML
    private void login(ActionEvent event) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT * FROM users WHERE email = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email.getText());
            stmt.setString(2, pass.getText());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Stage stage = (Stage) email.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("userprofile.fxml"));
                stage.setScene(new Scene(loader.load()));

                UserprofileController controller = loader.getController();
                controller.initializeUser(rs.getInt("id"), rs.getString("email"), rs.getDouble("balance"));
            } else {
                showAlert("Login Failed", "Invalid email or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   @FXML
private void singup(ActionEvent event) {
    try {
            // Load the signin.fxml file
            Parent root = FXMLLoader.load(getClass().getResource("signin.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
}


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}
