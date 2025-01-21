package mywallet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Mywallet extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseHelper.initializeDatabase();
        primaryStage.setTitle("MyWallet");
        primaryStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("login.fxml"))));
        primaryStage.show();
    }

   

    public static void main(String[] args) {
        launch(args);
    }
}
