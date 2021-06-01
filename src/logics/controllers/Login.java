package logics.controllers;

import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.util.Base64;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import logics.*;
import logics.objects.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Login {

    @FXML
    private Button selectAdmin;
    @FXML
    private Button selectSeller;
    @FXML
    private Button selectShopper;
    @FXML
    private AnchorPane windowLogin;
    @FXML
    private PasswordField inputPassword;
    @FXML
    private TextField inputLogin;
    @FXML
    private Button buttonGoin;
    @FXML
    private Button buttonClose;
    @FXML
    private Button buttonRegister;
    @FXML
    private CheckBox CheckRememberMe;
    @FXML
    private Hyperlink remindPassword;
    @FXML
    private Button ButtonAutor;

    @FXML
    void initialize() {
        selectAdmin.setOnAction(actionEvent -> {
            ConfigApp.type_user_session = 2;
            windowLogin.setVisible(true);
        });
        selectSeller.setOnAction(actionEvent -> {
            ConfigApp.type_user_session = 1;
            windowLogin.setVisible(true);
        });
        selectShopper.setOnAction(actionEvent -> {
            ConfigApp.type_user_session = 0;
            windowLogin.setVisible(true);
        });
        buttonGoin.setOnAction(actionEvent -> {
            if(ConfigApp.isStatusDB_Connect){
                Logins();
            }else{
                Main.DisplayNotification("Помилка підключення","До бази даних не встановлено зєднання",null);
            }
        });
        buttonClose.setOnAction(actionEvent -> {
            windowLogin.setVisible(false);
            inputLogin.clear();
            inputPassword.clear();
        });
        buttonRegister.setOnAction(actionEvent -> {
            buttonRegister.getScene().getWindow().hide();
            new MyWindowLoader().load("/view/register.fxml",800,600,false);
        });
        remindPassword.setOnAction(actionEvent -> {
            new MyWindowLoader().load("/view/remind_password.fxml",400,200,true);
        });
        ButtonAutor.setOnAction(actionEvent -> {
            new MyWindowLoader().load("/view/autor.fxml",500,400,true);
        });
    }
    private void Logins(){
        String login_mail = inputLogin.getText().replace('\'','′');
        String password = inputPassword.getText();
        String errorMessage = null;
        if(login_mail.isEmpty()){
            errorMessage = "Заповніть будьласка поле \"Логін або електронна пошта\"!";
            Main.DisplayNotification("Помилка",errorMessage,null);
            return;
        }
        if(password.isEmpty()){
            errorMessage = "Заповніть будьласка поле \"Пароль\"!";
            Main.DisplayNotification("Помилка",errorMessage,null);
            return;
        }
        if(ConfigApp.type_user_session==2){
            LoginUserDB(login_mail,password,true);
        }else {
           LoginUserDB(login_mail,password,false);
        }
    }
    private void LoginUserDB(String login_mail, String password, boolean isAdmin){
        String query = "SELECT * FROM " + ConfigApp.TABLE_USERS + " WHERE " + ConfigApp.NAME
                + " LIKE \'" + login_mail + "\' OR " + ConfigApp.MAIL + " LIKE \'" + login_mail + "\'";
        if(isAdmin){
            query+= " AND "+ConfigApp.TYPE_USER+" = "+2;
        }
        ResultSet result = DataBase.QueryResult(query);
        try {
            result.first();
            String AcceptPassword = result.getString(ConfigApp.PASSWORD);
            byte[] bytes = Base64.getDecoder().decode(AcceptPassword);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                baos.write(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            AcceptPassword = baos.toString();
            if (password.equals(AcceptPassword)) {
                Main.thisUser = new User();
                Main.thisUser.setId(result.getInt(ConfigApp.ID));
                Main.thisUser.setName(result.getString(ConfigApp.NAME));
                Main.thisUser.setMail(result.getString(ConfigApp.MAIL));
                Main.thisUser.setPassword(result.getString(ConfigApp.PASSWORD));
                Main.thisUser.setType_user(result.getInt(ConfigApp.TYPE_USER));
                if(result.getString(ConfigApp.AVATAR)!=null) {
                    JSONParser JsonParser = new JSONParser();
                    JSONObject JsonAvatar = (JSONObject) JsonParser.parse(result.getString(ConfigApp.AVATAR));
                    Main.thisUser.setAvatar(new MyImageLoader().Load((String) JsonAvatar.get("data"),(String) JsonAvatar.get("format")));
                }
                RememberUser();
                DataBase.WriteLogs("Користувач: "+Main.thisUser.getName()+" здійснив вхід!");
                buttonGoin.getScene().getWindow().hide();
                new MyWindowLoader().load("/view/shop.fxml", 800, 600,false);
            } else {
                Main.DisplayNotification("Помилка входу", "Ви ввели не вірний пароль!",null);
                return;
            }
        } catch (Exception throwables) {
            if (isAdmin) {
                Main.DisplayNotification("Помилка входу", "Такого адміністратора не знайдено!", null);
            }else{
                Main.DisplayNotification("Помилка входу", "Можливо ви ввели не вірний логін або пароль!", null);
            }
        }
    }
    private void RememberUser(){
        if(CheckRememberMe.isSelected()){
            String pcName = Main.getComputerName();
            String query = "INSERT INTO "+ConfigApp.TABLE_BOOKED_USERS+"("+ConfigApp.USER_ID+", "+ConfigApp.COMPUTER_NAME
                    + ") VALUES("+Main.thisUser.getId()+", \'"+pcName+"\');";
            DataBase.QueryBoolean(query);
        }
    }
}
