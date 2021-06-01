package logics.controllers;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import logics.*;
import logics.objects.User;
import org.json.simple.JSONObject;

import static logics.MyImageLoader.getFormat;

public class Register {

    @FXML
    private TextField inputLogin;
    @FXML
    private TextField inputMail;
    @FXML
    private PasswordField inputPassword;
    @FXML
    private PasswordField inputPasswordEnable;
    @FXML
    private Button ButtonAvatar;
    @FXML
    private ImageView ImagAvatar;
    @FXML
    private CheckBox chekSeller;
    @FXML
    private CheckBox chekShopper;
    @FXML
    private Button buttonClose;
    @FXML
    private Button buttonRegister;

    private String PathToAvatar = "";
    private String FormatAvatar = null;

    @FXML
    void initialize() {
        chekSeller.setOnAction(actionEvent -> {
            chekked(0);
        });
        chekShopper.setOnAction(actionEvent -> {
            chekked(1);
        });
        ButtonAvatar.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File selectFile = fileChooser.showOpenDialog(null);

            if(selectFile != null){
                PathToAvatar = selectFile.getAbsolutePath();
                FormatAvatar = getFormat(selectFile);
                try {
                    ImagAvatar.setImage(new Image(selectFile.toURI().toURL().toExternalForm()));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });
        buttonRegister.setOnAction(actionEvent -> {
            if(ConfigApp.isStatusDB_Connect){
                Register();
            }else{
                Main.DisplayNotification("Помилка підключення","До бази даних не встановлено зєднання",null);
            }
        });
        buttonClose.setOnAction(actionEvent -> {
            buttonClose.getScene().getWindow().hide();
            new MyWindowLoader().load("/view/login.fxml",800,600,false);
        });
    }
    private void Register(){
        String login = inputLogin.getText().replace('\'','′');
        String mail = inputMail.getText();
        String password = inputPassword.getText();
        String passwordEnable = inputPasswordEnable.getText();
        String errorMessage = null;
        if(login.isEmpty()){
            errorMessage = "Заповніть будьласка поле \"Логін\"!";
            Main.DisplayNotification("Помилка",errorMessage,null);
            return;
        }
        if(mail.isEmpty()){
            errorMessage = "Заповніть будьласка поле \"Електронна пошта\"!";
            Main.DisplayNotification("Помилка",errorMessage,null);
            return;
        }
        Pattern pattern = Pattern.compile("^(.+)@(.+)$");
        Matcher matcher = pattern.matcher(mail);
        if(!matcher.matches()){
            errorMessage = "Електронна пошта заповнена не коректно!";
            Main.DisplayNotification("Помилка",errorMessage,null);
            return;
        }
        if(password.isEmpty() && passwordEnable.isEmpty()){
            errorMessage = "Будьласка створіть пароль та підтвердіть його!";
            Main.DisplayNotification("Помилка",errorMessage,null);
            return;
        }
        if(!password.equals(passwordEnable)){
            errorMessage = "Пароль та його підтвердження не збігаються!";
            Main.DisplayNotification("Помилка",errorMessage,null);
            return;
        }
        int resID = 0;
        String query = "SELECT "+ConfigApp.ID+" FROM "+ConfigApp.TABLE_USERS+" WHERE "+ConfigApp.NAME
                +" LIKE \'"+login+"\' OR "+ConfigApp.MAIL+" LIKE \'"+mail+"\'";
        ResultSet result = DataBase.QueryResult(query);
        try {
            result.first();
            resID = result.getInt(ConfigApp.ID);
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
        if(resID == 0) {
            int type_user = 0;
            if(chekSeller.isSelected()){
                type_user = 1;
            }else if(chekShopper.isSelected()){
                type_user = 0;
            }
            String passwordEncode = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
            query = "INSERT INTO " + ConfigApp.TABLE_USERS + "(" + ConfigApp.NAME + ", " + ConfigApp.MAIL
                    + ", " + ConfigApp.PASSWORD + ", " + ConfigApp.TYPE_USER
                    + ") VALUES(\'" + login + "\', \'" + mail + "\', \'" + passwordEncode + "\', "+ type_user + ");";
            boolean res = DataBase.QueryBoolean(query);
            if(res){
                System.out.println("Успішно зареєстровано!");
                query = "SELECT "+ConfigApp.ID+" FROM "+ConfigApp.TABLE_USERS+" WHERE "+ConfigApp.NAME
                        +" LIKE \'"+login+"\'";
                result = DataBase.QueryResult(query);
                try {
                    result.first();
                    resID = result.getInt(ConfigApp.ID);
                    Image i = null;
                    if(!PathToAvatar.isEmpty()){
                        String imgData = MyImageLoader.Img_to_Data(PathToAvatar,FormatAvatar);
                        JSONObject jsonAvatar = new JSONObject();
                        jsonAvatar.put("format",FormatAvatar);
                        jsonAvatar.put("data",imgData);
                        query = "UPDATE "+ConfigApp.TABLE_USERS+" SET "+ConfigApp.AVATAR+" = \'"+jsonAvatar.toJSONString()+"\' WHERE "
                                +ConfigApp.ID+" = "+resID;
                        DataBase.QueryBoolean(query);
                        i = MyImageLoader.Load((String) jsonAvatar.get("data"),FormatAvatar);
                    }
                    DataBase.CreateStatistics(resID);
                    Main.thisUser = new User(resID,login,mail,passwordEncode,type_user,i);
                    ConfigApp.type_user_session = type_user;
                    DataBase.WriteLogs("Користувач: "+Main.thisUser.getName()+" зареєструвався!");
                    buttonRegister.getScene().getWindow().hide();
                    new MyWindowLoader().load("/view/shop.fxml",800,600,false);
                    return;
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }else{
            Main.DisplayNotification("Помилка реєстрації","Такий користувач уже існує в базі даних!\n" +
                    "Логін користувача або електронна пошта не може повторюватися!",null);
            inputLogin.clear();
            inputMail.clear();
            inputPassword.clear();
            inputPasswordEnable.clear();
            PathToAvatar = "";
            FormatAvatar = null;
        }
    }
    private void chekked(int i){
        if(i == 0){
            chekSeller.setSelected(true);
            chekShopper.setSelected(false);
        }else{
            chekSeller.setSelected(false);
            chekShopper.setSelected(true);
        }
    }

}
