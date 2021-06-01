package logics.controllers;

import java.sql.ResultSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import logics.*;
import logics.objects.Spare_parts;
import logics.objects.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Shop {

    @FXML
    private ImageView ImageUser;
    @FXML
    private Text NameView;
    @FXML
    private TextField MailUser;
    @FXML
    private Text TypeUser;
    @FXML
    private TableView<Spare_parts> ListData;
    @FXML
    private TableColumn<Spare_parts, Integer> ColumnId;
    @FXML
    private TableColumn<Spare_parts, ImageView> ColumnImage;
    @FXML
    private TableColumn<Spare_parts, String> ColumnName;
    @FXML
    private TableColumn<Spare_parts, Double> ColumnPrice;
    @FXML
    private TableColumn<Spare_parts, Button> ColumnActions;
    @FXML
    private AnchorPane LoadBur;
    @FXML
    private ProgressBar LoadProgres;
    @FXML
    private Button ButtonExit;
    @FXML
    private Button ButtonEditUser;
    @FXML
    private Button ButtonAddSPF;
    @FXML
    private Button ButtonAdminPanel;
    @FXML
    private TextField SearchText;
    @FXML
    private Button SearchButton;
    @FXML
    private Button ButtonOrders;
    @FXML
    private Button ButtonStatistics;
    @FXML
    private Button FilterButton;
    @FXML
    private Text CountSPF;

    public static Spare_parts SelectItem = null;
    ObservableList<Spare_parts> Spare_parts_List = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        LoadBur.setVisible(true);
        Main.Context = Shop.this;
        UpdateUserInfo();

        ColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        ColumnImage.setCellValueFactory(new PropertyValueFactory<>("img"));
        ColumnName.setCellValueFactory(new PropertyValueFactory<>("title"));
        ColumnPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        Callback<TableColumn<Spare_parts, Button>, TableCell<Spare_parts, Button>> cellFoctory = (TableColumn<Spare_parts, Button> param) -> {
            final TableCell<Spare_parts, Button> cell = new TableCell<Spare_parts, Button>() {
                @Override
                public void updateItem(Button item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        ImageView c = new ImageView(new Image("/res/configure.png"));
                        c.setFitHeight(48);
                        c.setFitWidth(48);
                        Button configure = new Button();
                        configure.setPrefSize(60,60);
                        configure.setMaxSize(60,60);
                        configure.setStyle("-fx-background-color: #6B8799; -fx-background-radius: 10;");
                        configure.setGraphic(c);
                        configure.setCursor(Cursor.HAND);
                        configure.setOnAction(actionEvent -> {
                            SelectItem = ListData.getSelectionModel().getSelectedItem();
                            ClickConfigure();
                        });
                        setGraphic(configure);
                        setText(null);
                    }
                }
            };
            return cell;
        };
        ColumnActions.setCellFactory(cellFoctory);

        Load_SPF(null);
        CheckOrders();

        ListData.setRowFactory( tv -> {
            TableRow<Spare_parts> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    SelectItem = row.getItem();
                    new MyWindowLoader().load("/view/product_page.fxml", 600, 400,true);
                }
            });
            row.setOnMouseMoved(event -> {
                ListData.getSelectionModel().select(row.getItem());
            });
            return row ;
        });
        ButtonExit.setOnAction(actionEvent -> {
            String pcName = Main.getComputerName();
            String query = "SELECT "+ConfigApp.ID+" FROM "+ ConfigApp.TABLE_BOOKED_USERS+" WHERE "+ConfigApp.COMPUTER_NAME
                    +" LIKE \'"+pcName+"\'";
            ResultSet r = DataBase.QueryResult(query);
            int id = 0;
            try{
                r.first();
                id=r.getInt(ConfigApp.ID);
            }catch (Exception e){}
            if(id==0){
                ButtonExit.getScene().getWindow().hide();
                new MyWindowLoader().load("/view/login.fxml", 800, 600,false);
            }else{
                query = "DELETE FROM "+ConfigApp.TABLE_BOOKED_USERS+" WHERE "+ConfigApp.ID+" = "+id;
                DataBase.QueryBoolean(query);
                ButtonExit.getScene().getWindow().hide();
                new MyWindowLoader().load("/view/login.fxml", 800, 600,false);
            }
        });
        ButtonEditUser.setOnAction(actionEvent -> {
            new MyWindowLoader().load("/view/edit_user.fxml", 600, 400,true);
        });
        ButtonAddSPF.setOnAction(actionEvent -> {
            Add_Edit_SPF.Sesion="add";
            addedit_SPF();
        });
        SearchButton.setOnAction(actionEvent -> {
            String search = SearchText.getText();
            if(search.isEmpty()){
                DeleteInfoSPF_List();
                Load_SPF(null);
            }else{
                String where = "WHERE "+ConfigApp.TITLE+" LIKE \'%"+search+"%\' OR "+ConfigApp.DESCRIPTION+" LIKE\'%"
                        +search+"%\'";
                DeleteInfoSPF_List();
                Load_SPF(where);
            }
        });
        ButtonOrders.setOnAction(actionEvent -> {
            if(Main.thisUser.getType_user()!=2) {
                new MyWindowLoader().load("/view/orders.fxml", 800, 500, true);
            }else{
                Main.DisplayNotification("Системна помилка","Адміністратори магазину не можуть приймати замовлення!",null);
            }
        });
        ButtonAdminPanel.setOnAction(actionEvent -> {
            ButtonAdminPanel.getScene().getWindow().hide();
            new MyWindowLoader().load("/view/admin_panel.fxml", 800, 500, false);
        });
        ButtonStatistics.setOnAction(actionEvent -> {
            new MyWindowLoader().load("/view/statistics.fxml", 600, 300, true);
        });
        FilterButton.setOnAction(actionEvent -> {
            Filter();
        });
    }
    public void Load_SPF(String where){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String query = "SELECT "+ConfigApp.ID+", "+ConfigApp.IMG+", "+ConfigApp.TITLE+", "+ConfigApp.PRICE
                        +" FROM "+ ConfigApp.TABLE_SPARE_PARTS;
                if(where!=null){query+=" "+where;}
                ResultSet result = DataBase.QueryResult(query);
                try {
                    result.last();
                    int indexProgress = 0;
                    int rowCount = result.getRow();
                    result.first();
                    do{
                        indexProgress++;
                        ImageView imgItem = null;
                        try {
                            JSONParser JsonParser = new JSONParser();
                            JSONObject JsonImg = (JSONObject) JsonParser.parse(result.getString(ConfigApp.IMG));
                            Image imgLoadItem = new MyImageLoader().Load((String) JsonImg.get("data"),(String) JsonImg.get("format"));
                            imgItem = new ImageView(imgLoadItem);
                        }catch(Exception et){ imgItem = new ImageView(new Image("/res/none_image.png"));}
                        imgItem.setFitHeight(140);
                        imgItem.setFitWidth(140);

                        Spare_parts item = new Spare_parts(result.getInt(ConfigApp.ID),imgItem,
                                result.getString(ConfigApp.TITLE),result.getDouble(ConfigApp.PRICE));
                        Spare_parts_List.add(item);
                        double progress = 0;
                        try {
                            progress = indexProgress/rowCount;
                        }catch (ArithmeticException e){
                            progress=1;
                        }
                        LoadProgres.setProgress(progress);
                    }while (result.next());
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    ListData.setItems(Spare_parts_List);
                    ListData.refresh();
                    CountSPF.setText("Кількість знайденої продукції: "+Spare_parts_List.size());
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LoadBur.setVisible(false);
                }
            }
        }).start();
    }
    public void DeleteInfoSPF_List(){
        Spare_parts_List.removeAll(Spare_parts_List);
        LoadBur.setVisible(true);
    }
    private void ClickConfigure(){
        int user_id = -1;
        String query = "SELECT "+ConfigApp.USER_ID+" FROM "+ConfigApp.TABLE_SPARE_PARTS+" WHERE "
                +ConfigApp.ID+" = "+SelectItem.getId();
        ResultSet r = DataBase.QueryResult(query);
        try {
            r.first();
            user_id = r.getInt(ConfigApp.USER_ID);
        }catch (Exception e){e.printStackTrace();}
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #6B8799; -fx-background-radius: 10;");
        MenuItem buy = new MenuItem("Купити");
        buy.setStyle("-fx-text-fill: white;");
        MenuItem edit = new MenuItem("Редагувати");
        edit.setStyle("-fx-text-fill: white;");
        MenuItem delete = new MenuItem("Видалити");
        delete.setStyle("-fx-text-fill: white;");

        if(Main.thisUser.getId()!=user_id&&Main.thisUser.getType_user()!=2) {
            contextMenu.getItems().add(buy);
        }
        if(Main.thisUser.getType_user()==2||Main.thisUser.getId()==user_id){
            contextMenu.getItems().add(edit);
            contextMenu.getItems().add(delete);
        }
        buy.setOnAction(actionEvent -> {
            new MyWindowLoader().load("/view/buy_ordering.fxml", 600, 400,true);
        });
        edit.setOnAction(actionEvent -> {
            Add_Edit_SPF.Sesion="edit";
            addedit_SPF();
        });
        delete.setOnAction(actionEvent -> {
            EventHandler handler = new EventHandler<Event>(){
                @Override
                public void handle(Event event) {
                    Main.dialog.closeDialog();
                    Spare_parts.DeleteSPF(Shop.SelectItem);
                    DeleteInfoSPF_List();
                    Load_SPF(null);
                }
            };
            Main.DisplayNotification("Видалення елемента","Ви дійсно хочете видалити цей продукт?",handler);
        });
        contextMenu.show(ListData.getScene().getWindow());
    }
    public void UpdateUserInfo(){
        User.InsertUserAvatar(ImageUser);
        if(Main.thisUser.getAvatar()==null||Main.thisUser.getAvatar().isError()){
            switch(Main.thisUser.getType_user()){
                case 2: Main.thisUser.setAvatar(new Image("/res/admin.png")); break;
                case 1: Main.thisUser.setAvatar(new Image("/res/seller.png")); break;
                case 0: Main.thisUser.setAvatar(new Image("/res/shopper.png")); break;
            }
        }
        ImageUser.setImage(Main.thisUser.getAvatar());
        NameView.setText(Main.thisUser.getName());
        MailUser.setText(Main.thisUser.getMail());
        switch(Main.thisUser.getType_user()){
            case 2: TypeUser.setText("Адміністратор"); ButtonAdminPanel.setDisable(false); break;
            case 1: TypeUser.setText("Продавець"); break;
            case 0: TypeUser.setText("Покупець"); break;
        }
    }
    public static void addedit_SPF(){
        if (Main.thisUser.getType_user()!=2) {
            new MyWindowLoader().load("/view/add_edit_spf.fxml", 600, 400, true);
        }else if(Add_Edit_SPF.Sesion.equals("edit")){
            new MyWindowLoader().load("/view/add_edit_spf.fxml", 600, 400, true);
        }else{
            Main.DisplayNotification("Системна помилка","Адміністратор магазину не може добавляти продукцію!",null);
        }
    }
    private void CheckOrders(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String query = "SELECT "+ConfigApp.ID+" FROM "+ConfigApp.TABLE_ORDERS+" WHERE "+ConfigApp.NOTE+" = 0 AND "
                        +ConfigApp.USER_ID+" = "+Main.thisUser.getId();
                ResultSet r = DataBase.QueryResult(query);
                try {
                    r.first();
                    int id = 0;
                    id = r.getInt(ConfigApp.ID);
                    if(id!=0){
                        ImageView c = new ImageView(new Image("/res/new_icon.png"));
                        c.setFitHeight(30);
                        c.setFitWidth(30);
                        ButtonOrders.setGraphic(c);
                    }
                }catch (Exception e){}
            }
        }).start();
    }
    private void Filter(){
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #6B8799; -fx-background-radius: 10;");
        MenuItem select_by_me = new MenuItem("Знайти тільки свої");
        select_by_me.setStyle("-fx-text-fill: white;");
        MenuItem select_not_by_me = new MenuItem("Знайти тільки чужі");
        select_not_by_me.setStyle("-fx-text-fill: white;");

        contextMenu.getItems().addAll(select_by_me, select_not_by_me);

        select_by_me.setOnAction(actionEvent -> {
            DeleteInfoSPF_List();
            Load_SPF("WHERE "+ConfigApp.USER_ID+" = "+Main.thisUser.getId());
        });
        select_not_by_me.setOnAction(actionEvent -> {
            DeleteInfoSPF_List();
            Load_SPF("WHERE "+ConfigApp.USER_ID+" != "+Main.thisUser.getId());
        });

        contextMenu.show(ListData.getScene().getWindow());
    }
}