package dbms_project.Main;

import com.almasb.fxgl.app.PrimaryStageWindow;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/login.fxml"));
        stage.setResizable(false);
        Scene scene = new Scene(fxmlLoader.load(), 450, 550);
        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/Images/icon.png"))
        );
        stage.setTitle("DBMS PROJECT");
        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}