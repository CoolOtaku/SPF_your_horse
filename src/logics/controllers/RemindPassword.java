package logics.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import logics.ConfigApp;
import logics.DataBase;
import logics.Main;
import logics.objects.User;
import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemindPassword {

    @FXML
    private TextField inputMail;
    @FXML
    private Button ButtonRemind;

    @FXML
    void initialize() {
        ButtonRemind.setOnAction(actionEvent -> {
            RemindPasswordEx();
        });
    }
    private void RemindPasswordEx(){
        String mail = inputMail.getText();
        Pattern pattern = Pattern.compile("^(.+)@(.+)$");
        Matcher matcher = pattern.matcher(mail);
        if(mail.isEmpty()){
            Main.DisplayNotification("Помилка","Ви забули ввести свій адрес електронної пошти!",null);
            return;
        }else if(!matcher.matches()){
            Main.DisplayNotification("Помилка","Електронна пошта заповнена не коректно!",null);
            return;
        }
        String query = "SELECT "+ConfigApp.NAME+", "+ConfigApp.PASSWORD+" FROM "+ConfigApp.TABLE_USERS+" WHERE "
                +ConfigApp.MAIL+" LIKE \'"+mail+"\'";
        ResultSet res = DataBase.QueryResult(query);
        try {
            res.first();
            String Password = res.getString(ConfigApp.PASSWORD);
            byte[] bytes = Base64.getDecoder().decode(Password);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                baos.write(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Password = baos.toString();
            User.SendMailMessage(mail,"Магазин SPF. Нагадування пароля","Ваш пароль до користувача "
                    +res.getString(ConfigApp.NAME)+": "+Password+" Якщо пароль був змінений без вашого відома, " +
                    "рокомендуємо його змінити після входу у магазин!");
            Main.DisplayNotification("Успішно","Вам на електронну адресу було надіслане пісьмо із паролем!",null);
            ButtonRemind.getScene().getWindow().hide();
        }catch (Exception e){
            Main.DisplayNotification("Помилка","Такий користувач не знайдений!",null);
            return;
        }
    }
}
