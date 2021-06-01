package logics.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import logics.*;
import logics.objects.Logs;
import logics.objects.User;
import logics.objects.Users;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static logics.MyImageLoader.getFormat;

public class AdminPanel {

    @FXML
    private ImageView AdminAvatar;
    @FXML
    private Button ButtonLogs;
    @FXML
    private Button ButtonUsers;
    @FXML
    private Button ButtonAppendUser;
    @FXML
    private Button ButtonDataBase;
    @FXML
    private Button ButtonReturnToMain;
    @FXML
    private Text NameContext;
    @FXML
    private TableView<Logs> TableLogs;
    @FXML
    private TableColumn<Logs, Integer> TL_ColumnId;
    @FXML
    private TableColumn<Logs, String> TL_ColumnText;
    @FXML
    private TableColumn<Logs, String> TL_ColumnDate_time;
    @FXML
    private TableView<Users> TableUsers;
    @FXML
    private TableColumn<Users, Integer> TU_ColumnId;
    @FXML
    private TableColumn<Users, ImageView> TU_ColumnAvatar;
    @FXML
    private TableColumn<Users, String> TU_ColumnName;
    @FXML
    private TableColumn<Users, String> TU_ColumnMail;
    @FXML
    private TableColumn<Users, String> TU_ColumnType;
    @FXML
    private AnchorPane UsersEditContainer;
    @FXML
    private AnchorPane UserAddContainer;
    @FXML
    private ImageView AddImgUser;
    @FXML
    private TextField inputAddUserLogin;
    @FXML
    private Button ButtonSelectAvatar;
    @FXML
    private TextField inputAddUserMail;
    @FXML
    private PasswordField inputAddUserPassword;
    @FXML
    private PasswordField inputAddUserPasswordEnable;
    @FXML
    private CheckBox chekSeller;
    @FXML
    private CheckBox chekShopper;
    @FXML
    private CheckBox chekAdmin;
    @FXML
    private Button ButtonAddUserOk;
    @FXML
    private Button ButtonAddUserCancel;

    ObservableList<Logs> Logs_List = FXCollections.observableArrayList();
    ObservableList<Users> Users_List = FXCollections.observableArrayList();
    Logs SelectLog = null;
    public static Users SelectUsers = null;

    private String PathToAvatar = "";
    private String FormatAvatar = null;

    @FXML
    void initialize() {
        Main.Context = AdminPanel.this;
        User.InsertUserAvatar(AdminAvatar);
        AdminAvatar.setImage(Main.thisUser.getAvatar());
        NameContext.setText("Логи");
        TL_ColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TL_ColumnText.setCellValueFactory(new PropertyValueFactory<>("text"));
        TL_ColumnDate_time.setCellValueFactory(new PropertyValueFactory<>("date_time"));

        TU_ColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TU_ColumnAvatar.setCellValueFactory(new PropertyValueFactory<>("avatar"));
        TU_ColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        TU_ColumnMail.setCellValueFactory(new PropertyValueFactory<>("mail"));
        TU_ColumnType.setCellValueFactory(new PropertyValueFactory<>("type"));
        get_Logs();

        TableLogs.setRowFactory( tv -> {
            TableRow<Logs> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    SelectLog = row.getItem();
                    ActionLogs();
                }
            });
            row.setOnMouseMoved(event -> {
                TableLogs.getSelectionModel().select(row.getItem());
            });
            return row ;
        });
        TableUsers.setRowFactory( tv -> {
            TableRow<Users> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    SelectUsers = row.getItem();
                    ActionUsers();
                }
            });
            row.setOnMouseMoved(event -> {
                TableUsers.getSelectionModel().select(row.getItem());
            });
            return row ;
        });
        ButtonLogs.setOnAction(actionEvent -> {
            if(!TableLogs.isVisible()) {
                TableUsers.setVisible(false);
                UsersEditContainer.setVisible(false);
                UserAddContainer.setVisible(false);
                NameContext.setText("Загрузка...");
                get_Logs();
                Users_List.removeAll(Logs_List);
                TableUsers.refresh();
            }
        });
        ButtonUsers.setOnAction(actionEvent -> {
            if(!TableUsers.isVisible()) {
                TableLogs.setVisible(false);
                UsersEditContainer.setVisible(false);
                UserAddContainer.setVisible(false);
                NameContext.setText("Загрузка...");
                get_Users();
                Logs_List.removeAll(Logs_List);
                TableLogs.refresh();
            }
        });
        ButtonAppendUser.setOnAction(actionEvent -> {
            if(!UserAddContainer.isVisible()) {
                TableLogs.setVisible(false);
                TableUsers.setVisible(false);
                UsersEditContainer.setVisible(false);
                UserAddContainer.setVisible(true);
                NameContext.setText("Додавання користувача");
            }
        });
        ButtonDataBase.setOnAction(actionEvent -> {
            Main.host_service.showDocument("https://"+ConfigApp.dbHost+"/phpmyadmin/index.php?db=&table=&token=2a75704e375950702e2475464c6b3367&lang=uk");
        });
        ButtonReturnToMain.setOnAction(actionEvent -> {
            ButtonReturnToMain.getScene().getWindow().hide();
            new MyWindowLoader().load("/view/shop.fxml", 800, 600,false);
        });

        chekSeller.setOnAction(actionEvent -> {
            chekked(0);
        });
        chekShopper.setOnAction(actionEvent -> {
            chekked(1);
        });
        chekAdmin.setOnAction(actionEvent -> {
            chekked(2);
        });
        ButtonSelectAvatar.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File selectFile = fileChooser.showOpenDialog(null);

            if(selectFile != null){
                PathToAvatar = selectFile.getAbsolutePath();
                FormatAvatar = getFormat(selectFile);
                try {
                    AddImgUser.setImage(new Image(selectFile.toURI().toURL().toExternalForm()));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });
        ButtonAddUserCancel.setOnAction(actionEvent -> {
            clearAddUser();
            NameContext.setText("Загрузка...");
            get_Logs();
        });
        ButtonAddUserOk.setOnAction(actionEvent -> {
            AddUser();
            NameContext.setText("Загрузка...");
            get_Logs();
        });
    }
    private void get_Logs(){
        Logs_List.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String query = "SELECT * FROM "+ ConfigApp.TABLE_LOGS;
                ResultSet result = DataBase.QueryResult(query);
                try {
                    result.first();
                    do{
                        Logs log = new Logs(result.getInt(ConfigApp.ID),result.getString(ConfigApp.TEXT),
                                result.getString(ConfigApp.DATE));
                        Logs_List.add(log);
                    }while (result.next());
                    TableLogs.setItems(Logs_List);
                    TableLogs.refresh();
                    NameContext.setText("Логи");
                    TableLogs.setVisible(true);
                }catch (Exception e){}
            }
        }).start();
    }
    private void get_Users(){
        Users_List.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String query = "SELECT * FROM "+ ConfigApp.TABLE_USERS;
                ResultSet result = DataBase.QueryResult(query);
                try {
                    result.first();
                    do{
                        ImageView imgItem = null;
                        try {
                            JSONParser JsonParser = new JSONParser();
                            JSONObject JsonImg = (JSONObject) JsonParser.parse(result.getString(ConfigApp.AVATAR));
                            Image imgLoadItem = new MyImageLoader().Load((String) JsonImg.get("data"),(String) JsonImg.get("format"));
                            imgItem = new ImageView(imgLoadItem);
                        }catch(Exception et){ imgItem = new ImageView(new Image("/res/none_image.png"));}
                        imgItem.setFitHeight(110);
                        imgItem.setFitWidth(110);
                        String type = "";
                        switch(result.getInt(ConfigApp.TYPE_USER)){
                            case 2: type = "Адміністратор"; break;
                            case 1: type = "Продавець"; break;
                            case 0: type = "Покупець"; break;
                        }

                        Users users = new Users(result.getInt(ConfigApp.ID),imgItem,result.getString(ConfigApp.NAME),
                                result.getString(ConfigApp.MAIL),type);
                        Users_List.add(users);
                    }while (result.next());
                    TableUsers.setItems(Users_List);
                    TableUsers.refresh();
                    NameContext.setText("Користувачі");
                    TableUsers.setVisible(true);
                }catch (Exception e){}
            }
        }).start();
    }
    private void get_Edit_User(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/edit_user.fxml"));
            UsersEditContainer.getChildren().setAll((Node) loader.load());
            NameContext.setText("Редагування користувача");
            UsersEditContainer.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closeEditUser(){
        if(UsersEditContainer.isVisible()) {
            UsersEditContainer.setVisible(false);
            TableLogs.setVisible(false);
            NameContext.setText("Загрузка...");
            get_Users();
        }
    }

    private void ActionLogs(){
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #6B8799; -fx-background-radius: 10;");
        MenuItem copy = new MenuItem("Копіювати текст");
        copy.setStyle("-fx-text-fill: white;");
        MenuItem delete = new MenuItem("Видалити");
        delete.setStyle("-fx-text-fill: white;");

        contextMenu.getItems().addAll(copy, delete);

        copy.setOnAction(actionEvent -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(SelectLog.getText());
            clipboard.setContent(content);
        });
        delete.setOnAction(actionEvent -> {
            EventHandler handler = new EventHandler<Event>(){
                @Override
                public void handle(Event event) {
                    Main.dialog.closeDialog();
                    Logs.DeleteLog(SelectLog.getId());
                    Logs_List.removeAll(Logs_List);
                    get_Logs();
                }
            };
            Main.DisplayNotification("Видалення елемента","Ви дійсно хочете видалити цей лог?",handler);
        });
        contextMenu.show(TableLogs.getScene().getWindow());
    }
    private void ActionUsers(){
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #6B8799; -fx-background-radius: 10;");
        MenuItem edit = new MenuItem("Редагувати");
        edit.setStyle("-fx-text-fill: white;");
        MenuItem delete = new MenuItem("Видалити");
        delete.setStyle("-fx-text-fill: white;");

        contextMenu.getItems().addAll(edit, delete);

        edit.setOnAction(actionEvent -> {
            TableLogs.setVisible(false);
            TableUsers.setVisible(false);
            NameContext.setText("Загрузка...");
            get_Edit_User();
        });
        delete.setOnAction(actionEvent -> {

        });

        contextMenu.show(TableUsers.getScene().getWindow());
    }
    private void AddUser(){
        String login = inputAddUserLogin.getText().replace('\'','′');
        String mail = inputAddUserMail.getText();
        String password = inputAddUserPassword.getText();
        String passwordEnable = inputAddUserPasswordEnable.getText();
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
            if(chekAdmin.isSelected()){
                type_user = 2;
            }else if(chekShopper.isSelected()){
                type_user = 0;
            }else if(chekSeller.isSelected()){
                type_user = 1;
            }else {
                Main.DisplayNotification("Помилка при додаванні користувача","Ви забули вказати тип користувача!",null);
                return;
            }
            String passwordEncode = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
            query = "INSERT INTO " + ConfigApp.TABLE_USERS + "(" + ConfigApp.NAME + ", " + ConfigApp.MAIL
                    + ", " + ConfigApp.PASSWORD + ", " + ConfigApp.TYPE_USER
                    + ") VALUES(\'" + login + "\', \'" + mail + "\', \'" + passwordEncode + "\', "+ type_user + ");";
            boolean res = DataBase.QueryBoolean(query);
            if(res){
                DataBase.WriteLogs("Користувач: "+Main.thisUser.getName()+" добавив користувача: "+login);
                Main.DisplayNotification("Користувач успішно добавлений","Користувач успішно добавлений під логіном: " +
                        ""+login+" і його електронна пошта: "+mail+", якщо пошти не існує або вона не правильна рокомендуємо її поправити!",null);

                query = "SELECT "+ConfigApp.ID+" FROM "+ConfigApp.TABLE_USERS+" WHERE "+ConfigApp.NAME
                        +" LIKE \'"+login+"\'";
                result = DataBase.QueryResult(query);
                try {
                    result.first();
                    resID = result.getInt(ConfigApp.ID);
                    DataBase.CreateStatistics(resID);
                }catch (Exception e){ e.printStackTrace();}
            }
        }else{
            Main.DisplayNotification("Помилка при додаванні користувача","Такий користувач уже існує в базі даних!\n" +
                    "Логін користувача або електронна пошта не може повторюватися!",null);
        }
        clearAddUser();
    }
    private void clearAddUser(){
        UserAddContainer.setVisible(false);
        AddImgUser.setImage(new Image("/res/none_image.png"));
        inputAddUserLogin.clear();
        inputAddUserMail.clear();
        inputAddUserPassword.clear();
        inputAddUserPasswordEnable.clear();
        PathToAvatar = "";
        FormatAvatar = null;
    }
    private void chekked(int i){
        if(i == 0){
            chekSeller.setSelected(true);
            chekShopper.setSelected(false);
            chekAdmin.setSelected(false);
        }else if(i==1){
            chekSeller.setSelected(false);
            chekShopper.setSelected(true);
            chekAdmin.setSelected(false);
        }else{
            chekSeller.setSelected(false);
            chekShopper.setSelected(false);
            chekAdmin.setSelected(true);
        }
    }

}
