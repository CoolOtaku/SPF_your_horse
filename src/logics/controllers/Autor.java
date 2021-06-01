package logics.controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import logics.Main;

public class Autor {

    @FXML
    private ImageView DevAvatar;
    @FXML
    private Button ButtonInstagram;
    @FXML
    private Button ButtonFacebook;
    @FXML
    private Button ButtonTelegram;
    @FXML
    private Button ButtonGit;

    private Image AutorImg;
    private Image DevImg;

    @FXML
    void initialize() {
        AutorImg = new Image("/res/autor_icon.png");
        DevImg = new Image("/res/developer1.jpg");
        DevAvatar.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(DevAvatar.getImage().equals(AutorImg)){
                    DevAvatar.setImage(DevImg);
                }else {
                    DevAvatar.setImage(AutorImg);
                }
                event.consume();
            }
        });
        ButtonInstagram.setOnAction(actionEvent -> {
            Main.host_service.showDocument("https://www.instagram.com/coll_otaku/");
        });
        ButtonFacebook.setOnAction(actionEvent -> {
            Main.host_service.showDocument("https://www.facebook.com/profile.php?id=100008579443704");
        });
        ButtonTelegram.setOnAction(actionEvent -> {
            Main.host_service.showDocument("https://t.me/Coll_Otaku");
        });
        ButtonGit.setOnAction(actionEvent -> {
            Main.host_service.showDocument("https://github.com/CoolOtaku");
        });

    }
}
