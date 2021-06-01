package logics.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import logics.*;
import logics.objects.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static logics.MyImageLoader.getFormat;

public class EditUser {

    @FXML
    private ImageView EditImgUser;
    @FXML
    private TextField inputEditLogin;
    @FXML
    private Button ButtonSelectAvatar;
    @FXML
    private TextField inputEditMail;
    @FXML
    private PasswordField inputPassword;
    @FXML
    private PasswordField inputPasswordEnable;
    @FXML
    private Text TextType;
    @FXML
    private CheckBox chekSeller;
    @FXML
    private CheckBox chekShopper;
    @FXML
    private Button ButtonSave;
    @FXML
    private Button ButtonCancel;

    private User EditUser = null;
    private String PathToAvatar = "";
    private String FormatAvatar = null;

    @FXML
    void initialize() {
        User.InsertUserAvatar(EditImgUser);

        if(Main.Context.getClass().getSimpleName().equals("AdminPanel")) {
            int type = -1;
            switch(AdminPanel.SelectUsers.getType()){
                case "Адміністратор": type = 2; break;
                case "Продавець": type = 1; break;
                case "Покупець": type = 0; break;
            }
            EditUser = new User(AdminPanel.SelectUsers.getId(),AdminPanel.SelectUsers.getName(),
                    AdminPanel.SelectUsers.getMail(),null,type,AdminPanel.SelectUsers.getAvatar().getImage());
        }else{
            EditUser = Main.thisUser;
        }

        EditImgUser.setImage(EditUser.getAvatar());
        if(EditUser.getType_user()==2){
            TextType.setVisible(false);
            chekSeller.setVisible(false);
            chekShopper.setVisible(false);
        }else if (EditUser.getType_user()==1){
            chekSeller.setSelected(true);
        }else {
            chekShopper.setSelected(true);
        }

        ButtonSelectAvatar.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File selectFile = fileChooser.showOpenDialog(null);

            if(selectFile != null){
                PathToAvatar = selectFile.getAbsolutePath();
                FormatAvatar = getFormat(selectFile);
                try {
                    EditImgUser.setImage(new Image(selectFile.toURI().toURL().toExternalForm()));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });
        ButtonCancel.setOnAction(actionEvent -> {
            if(Main.Context.getClass().getSimpleName().equals("AdminPanel")){
                AdminPanel ad = (AdminPanel) Main.Context;
                ad.closeEditUser();
            }else {
                ButtonCancel.getScene().getWindow().hide();
            }
        });
        chekSeller.setOnAction(actionEvent -> {
            chekked(0);
        });
        chekShopper.setOnAction(actionEvent -> {
            chekked(1);
        });
        ButtonSave.setOnAction(actionEvent -> {
            String message = EditSave();
            if(!message.isEmpty()){
                Main.DisplayNotification("Помилка зміни даних про акаунт: ",message,null);
            }else{
                if(Main.Context.getClass().getSimpleName().equals("Shop")) {
                    String query = "SELECT * FROM " + ConfigApp.TABLE_USERS + " WHERE " + ConfigApp.ID + " = " + Main.thisUser.getId();
                    ResultSet resultSet = DataBase.QueryResult(query);
                    try {
                        resultSet.first();
                        Main.thisUser.setName(resultSet.getString(ConfigApp.NAME));
                        Main.thisUser.setMail(resultSet.getString(ConfigApp.MAIL));
                        Main.thisUser.setPassword(resultSet.getString(ConfigApp.PASSWORD));
                        Main.thisUser.setType_user(resultSet.getInt(ConfigApp.TYPE_USER));
                        if (resultSet.getString(ConfigApp.AVATAR) != null) {
                            JSONParser JsonParser = new JSONParser();
                            JSONObject JsonAvatar = (JSONObject) JsonParser.parse(resultSet.getString(ConfigApp.AVATAR));
                            Main.thisUser.setAvatar(new MyImageLoader().Load((String) JsonAvatar.get("data"),
                                    (String) JsonAvatar.get("format")));
                        }
                        ConfigApp.type_user_session = resultSet.getInt(ConfigApp.TYPE_USER);

                        Shop s = (Shop) Main.Context;
                        s.UpdateUserInfo();
                        ButtonSave.getScene().getWindow().hide();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    AdminPanel ad = (AdminPanel) Main.Context;
                    ad.closeEditUser();
                }
            }
        });
    }
    private String EditSave(){
        String query;
        boolean b;
        if(!PathToAvatar.isEmpty()){
            String imgData = MyImageLoader.Img_to_Data(PathToAvatar,FormatAvatar);
            JSONObject jsonAvatar = new JSONObject();
            jsonAvatar.put("format",FormatAvatar);
            jsonAvatar.put("data",imgData);
            query = "UPDATE "+ ConfigApp.TABLE_USERS+" SET "+ConfigApp.AVATAR+" = \'"+jsonAvatar.toJSONString()
                    +"\' WHERE "+ConfigApp.ID+" = "+EditUser.getId();
            b = DataBase.QueryBoolean(query);
            if(!b){
                return "Невдалося змінити аватар!";
            }
        }
        if(!inputEditLogin.getText().isEmpty()){
            inputEditLogin.setText(inputEditLogin.getText().replace('\'','′'));
            query = "SELECT "+ConfigApp.ID+" FROM "+ConfigApp.TABLE_USERS+" WHERE "+ConfigApp.NAME+" LIKE \'"+inputEditLogin.getText()+"\'";
            ResultSet resultSet = DataBase.QueryResult(query);
            int resId = 0;
            try {
                resultSet.first();
                resId = resultSet.getInt(ConfigApp.ID);
            }catch (Exception e){e.printStackTrace();}
            if(resId==0){
                query = "UPDATE "+ ConfigApp.TABLE_USERS+" SET "+ConfigApp.NAME+" = \'"+inputEditLogin.getText()
                        +"\' WHERE "+ConfigApp.ID+" = "+EditUser.getId();
                b = DataBase.QueryBoolean(query);
                if(!b){
                    return "Невдалося змінити логін!";
                }
            }else {
                return "Таке ім\'я користувача уже існує!";
            }
        }
        if(!inputEditMail.getText().isEmpty()){
            Pattern pattern = Pattern.compile("^(.+)@(.+)$");
            Matcher matcher = pattern.matcher(inputEditMail.getText());
            if(!matcher.matches()){
                return "Електронна пошта заповнена не коректно!";
            }
            query = "SELECT "+ConfigApp.ID+" FROM "+ConfigApp.TABLE_USERS+" WHERE "+ConfigApp.MAIL+" LIKE \'"+inputEditMail.getText()+"\'";
            ResultSet resultSet = DataBase.QueryResult(query);
            int resId = 0;
            try {
                resultSet.first();
                resId = resultSet.getInt(ConfigApp.ID);
            }catch (Exception e){e.printStackTrace();}
            if(resId==0) {
                query = "UPDATE " + ConfigApp.TABLE_USERS + " SET " + ConfigApp.MAIL + " = \'" + inputEditMail.getText()
                        + "\' WHERE " + ConfigApp.ID + " = " +EditUser.getId();
                b = DataBase.QueryBoolean(query);
                if (!b) {
                    return "Невдалося змінити електронну адресу!";
                }
            }else {
                return "Така електронна адреса уже існує в базі даних!";
            }
        }
        if(!inputPassword.getText().isEmpty()){
            if(inputPassword.getText().equals(inputPasswordEnable.getText())){
                String passwordEncode = Base64.getEncoder().encodeToString(inputPassword.getText().getBytes(StandardCharsets.UTF_8));
                query = "UPDATE "+ ConfigApp.TABLE_USERS+" SET "+ConfigApp.PASSWORD+" = \'"+passwordEncode
                        +"\' WHERE "+ConfigApp.ID+" = "+EditUser.getId();
                b = DataBase.QueryBoolean(query);
                if(!b){
                    return "Невдалося змінити пароль!";
                }
            }else {
                return "Паролі не співпадають!";
            }
        }
        if(EditUser.getType_user()!=2){
            query = "";
            if(chekSeller.isSelected()&&EditUser.getType_user()==0){
                query = "UPDATE "+ ConfigApp.TABLE_USERS+" SET "+ConfigApp.TYPE_USER+" = "+1
                        +" WHERE "+ConfigApp.ID+" = "+EditUser.getId();
            }else if(chekShopper.isSelected()&&EditUser.getType_user()==1){
                query = "UPDATE "+ ConfigApp.TABLE_USERS+" SET "+ConfigApp.TYPE_USER+" = "+0
                        +" WHERE "+ConfigApp.ID+" = "+EditUser.getId();
            }
            if(!query.isEmpty()){
                b = DataBase.QueryBoolean(query);
                if(!b){
                    return "Невдалося змінити головний тип!";
                }
            }
        }
        DataBase.WriteLogs("Користувач: "+EditUser.getName()+" змінив дані профілю!");
        return "";
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
