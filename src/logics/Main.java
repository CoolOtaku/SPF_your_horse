package logics;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import logics.objects.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import javax.swing.*;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Scanner;

public class Main extends Application {

    DataBase dataBase = new DataBase();
    public static Connection dbConnection;
    public static User thisUser;
    public static Object Context;
    public static CustomDialog dialog;
    public static HostServices host_service;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/view/start.fxml"));
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setTitle("SPF you horse");
        Image ico = new Image("/res/icon.png");
        primaryStage.getIcons().add(ico);
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
        host_service = getHostServices();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CheckConfigDB();
                    DBConnect(primaryStage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void DBConnect(Stage primaryStage) throws Exception{
        try {
            dbConnection = dataBase.getDbConnection();
            ConfigApp.isStatusDB_Connect = true;
        }catch (Exception e){
            ConfigApp.isStatusDB_Connect = false;
            Platform.runLater(() -> {
                DisplayNotification("Помилка підключення до бд!","Виникла помилка під час підключення до " +
                        "бази даних. Можливо немає з'єднання з інтернетом або сервером!",null);
            });
            e.printStackTrace();
        }
        Thread.sleep(3000);
        if(ConfigApp.isStatusDB_Connect){
            String pcName = getComputerName();
            String query = "SELECT "+ConfigApp.USER_ID+" FROM "+ ConfigApp.TABLE_BOOKED_USERS+" WHERE "+ConfigApp.COMPUTER_NAME
                    +" LIKE \'"+pcName+"\'";
            ResultSet result = DataBase.QueryResult(query);
            result.last();
            if(result.getRow()!=0){
                try {
                    result.first();
                    int userId = result.getInt(ConfigApp.USER_ID);
                    query = "SELECT * FROM "+ConfigApp.TABLE_USERS+" WHERE "+ConfigApp.ID+" = "+userId;
                    result = DataBase.QueryResult(query);
                    result.first();
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
                    ConfigApp.type_user_session = result.getInt(ConfigApp.TYPE_USER);
                    Platform.runLater(() -> {
                        primaryStage.getScene().getWindow().hide();
                        new MyWindowLoader().load("/view/shop.fxml", 800, 600,false);
                    });
                }catch (Exception e){e.printStackTrace();}
            }else{
                Platform.runLater(() -> {
                    primaryStage.getScene().getWindow().hide();
                    new MyWindowLoader().load("/view/login.fxml", 800, 600,false);
                });
            }
        }
    }

    public static void DisplayNotification(String name, String message, EventHandler actionEvent){
        dialog = new CustomDialog(name, message, actionEvent);
        dialog.openDialog();
    }
    public static String getComputerName(){
        String computername = null;
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                String prioriName = System.getenv("COMPUTERNAME");
                if (prioriName == null) {
                    computername = execReadToString("hostname");
                } else {
                    computername = prioriName;
                }
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac os x")) {
                String prioriName = System.getenv("HOSTNAME");
                if (prioriName == null) {
                    computername = execReadToString("hostname");
                } else {
                    computername = prioriName;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return computername;
    }
    private static String execReadToString(String execCommand) throws IOException {
        try (Scanner s = new Scanner(Runtime.getRuntime().exec(execCommand).getInputStream()).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }
    private void CheckConfigDB() throws Exception{
        String MyDocs = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
        ConfigDB configDB;
        File file = new File(MyDocs+"\\SPF\\config_db.xml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            configDB = new ConfigDB(ConfigApp.dbHost, ConfigApp.dbUser, ConfigApp.dbPassword, ConfigApp.dbName);
            FileOutputStream fos = new FileOutputStream(file);
            XMLEncoder xmlEncoder = new XMLEncoder(fos);
            xmlEncoder.writeObject(configDB);
            xmlEncoder.close();
            fos.close();
        }else{
            FileInputStream fis = new FileInputStream(file);
            XMLDecoder xmlDecoder = new XMLDecoder(fis);
            configDB = (ConfigDB) xmlDecoder.readObject();
            xmlDecoder.close();
            fis.close();
            ConfigApp.dbHost = configDB.dbHost;
            ConfigApp.dbUser = configDB.dbUser;
            ConfigApp.dbPassword = configDB.dbPassword;
            ConfigApp.dbName = configDB.dbName;
        }
    }
    public static class ConfigDB{
        private String dbHost;
        private String dbUser;
        private String dbPassword;
        private String dbName;

        public ConfigDB() {
        }
        public ConfigDB(String dbHost, String dbUser, String dbPassword, String dbName) {
            this.dbHost = dbHost;
            this.dbUser = dbUser;
            this.dbPassword = dbPassword;
            this.dbName = dbName;
        }

        public String getDbHost() {
            return dbHost;
        }
        public void setDbHost(String dbHost) {
            this.dbHost = dbHost;
        }
        public String getDbUser() {
            return dbUser;
        }
        public void setDbUser(String dbUser) {
            this.dbUser = dbUser;
        }
        public String getDbPassword() {
            return dbPassword;
        }
        public void setDbPassword(String dbPassword) {
            this.dbPassword = dbPassword;
        }
        public String getDbName() {
            return dbName;
        }
        public void setDbName(String dbName) {
            this.dbName = dbName;
        }
    }
}
