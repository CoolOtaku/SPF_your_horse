package logics;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MyWindowLoader {

    public void load(String path, int x, int y, boolean initModal){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(path));
            loader.load();

            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setMinWidth(x);
            stage.setMinHeight(y);
            Scene s = new Scene(root,x,y);
            s.getStylesheets().add((getClass().getResource("/res/WindowStyle.css")).toExternalForm());
            stage.setScene(s);
            stage.setTitle("SPF you horse");
            Image ico = new Image("/res/icon.png");
            stage.getIcons().add(ico);
            if(initModal){
                stage.initModality(Modality.APPLICATION_MODAL);
            }
            stage.show();
            stage.toFront();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
