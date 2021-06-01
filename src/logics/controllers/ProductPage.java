package logics.controllers;

import java.sql.ResultSet;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import logics.*;
import logics.objects.Reviews;
import logics.objects.Spare_parts;
import logics.objects.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ProductPage {

    @FXML
    private ImageView ImageItem;
    @FXML
    private Text NameItem;
    @FXML
    private Text DescriptionItem;
    @FXML
    private Text PriceItem;
    @FXML
    private Text CountItem;
    @FXML
    private Text PUserName;
    @FXML
    private ImageView PUserImage;
    @FXML
    private TableView<Reviews> ListReviews;
    @FXML
    private TableColumn<Reviews, ImageView> ColumnAvatar;
    @FXML
    private TableColumn<Reviews, String> ColumnName;
    @FXML
    private TableColumn<Reviews, String> ColumnText;
    @FXML
    private Button ButtonBuy;
    @FXML
    private Button ButtonEdit;
    @FXML
    private Button ButtonDelete;
    @FXML
    private Button ButtonAddReviews;
    @FXML
    private AnchorPane ContainerSendReviews;
    @FXML
    private Button ButtonReviewsCancel;
    @FXML
    private TextArea SendReviewsText;
    @FXML
    private Button ButtonSendReviews;
    @FXML
    private TextArea MailUser;

    ObservableList<Reviews> Reviews_List = FXCollections.observableArrayList();
    Map<Integer, String> list_tmp_users = new HashMap<>();

    @FXML
    void initialize() {
        Spare_parts thisItem = new Spare_parts(Shop.SelectItem.getId(),Shop.SelectItem.getImg(),Shop.SelectItem.getTitle(),
                Shop.SelectItem.getPrice());
        LoadReviews(thisItem.getId());
        String query = "SELECT "+ConfigApp.DESCRIPTION+", "+ConfigApp.COUNT+", "+ConfigApp.USER_ID+" FROM "
                + ConfigApp.TABLE_SPARE_PARTS+" WHERE "+ConfigApp.ID+" = "+thisItem.getId();
        ResultSet result = DataBase.QueryResult(query);
        try {
            result.first();
            try {
                ImageItem.setImage(thisItem.getImg().getImage());
            }catch(Exception et){}
            NameItem.setText(thisItem.getTitle());
            try {
                DescriptionItem.setText(result.getString(ConfigApp.DESCRIPTION));
            }catch(Exception et){}
            PriceItem.setText("Ціна: "+thisItem.getPrice()+" грн");
            CountItem.setText("Кількість: "+result.getString(ConfigApp.COUNT));

            int IdUser = result.getInt(ConfigApp.USER_ID);
            if(Main.thisUser.getId()==IdUser||Main.thisUser.getType_user()==2){
                ButtonBuy.setDisable(true);
                ButtonAddReviews.setDisable(true);

            }
            if(Main.thisUser.getType_user()!=2&&Main.thisUser.getId()!=IdUser){
                ButtonEdit.setDisable(true);
                ButtonDelete.setDisable(true);
            }
            query = "SELECT "+ConfigApp.AVATAR+", "+ConfigApp.NAME+", "+ConfigApp.TYPE_USER+", "+ConfigApp.MAIL
                    +" FROM "+ConfigApp.TABLE_USERS+" WHERE "+ConfigApp.ID+" = "+IdUser;
            result = DataBase.QueryResult(query);
            result.first();
            User.InsertUserAvatar(PUserImage);
            Image imgUser = null;
            if(result.getString(ConfigApp.AVATAR)!=null){
                JSONParser JsonParser = new JSONParser();
                JSONObject JsonAvatar = (JSONObject) JsonParser.parse(result.getString(ConfigApp.AVATAR));
                imgUser = new MyImageLoader().Load((String) JsonAvatar.get("data"),(String) JsonAvatar.get("format"));
            }else {
                switch(result.getInt(ConfigApp.TYPE_USER)){
                    case 1: imgUser = new Image("/res/seller.png"); break;
                    case 0: imgUser = new Image("/res/shopper.png"); break;
                }
            }
            PUserImage.setImage(imgUser);
            PUserName.setText(result.getString(ConfigApp.NAME));
            MailUser.setText(result.getString(ConfigApp.MAIL));
        }catch (Exception e){e.printStackTrace();}

        ButtonDelete.setOnAction(actionEvent -> {
            EventHandler handler = new EventHandler<Event>(){
                @Override
                public void handle(Event event) {
                    Main.dialog.closeDialog();
                    Spare_parts.DeleteSPF(thisItem);
                    Shop s = (Shop) Main.Context;
                    s.DeleteInfoSPF_List();
                    s.Load_SPF(null);
                    ButtonDelete.getScene().getWindow().hide();
                }
            };
            Main.DisplayNotification("Видалення елемента","Ви дійсно хочете видалити цей продукт?",handler);
        });
        ButtonBuy.setOnAction(actionEvent -> {
            new MyWindowLoader().load("/view/buy_ordering.fxml", 600, 400,true);
        });
        ButtonEdit.setOnAction(actionEvent -> {
            Add_Edit_SPF.Sesion="edit";
            Shop.addedit_SPF();
        });
        ButtonReviewsCancel.setOnAction(actionEvent -> {
            ContainerSendReviews.setVisible(false);
        });
        ButtonAddReviews.setOnAction(actionEvent -> {
            ContainerSendReviews.setVisible(true);
        });
        ButtonSendReviews.setOnAction(actionEvent -> {
            SendReviews(thisItem);
        });
    }

    private void LoadReviews(int id){
        ColumnAvatar.setCellValueFactory(new PropertyValueFactory<>("avatar"));
        ColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        ColumnText.setCellValueFactory(new PropertyValueFactory<>("text"));
        new Thread(new Runnable() {
            @Override
            public void run() {
                String query = "SELECT * FROM "+ConfigApp.TABLE_REVIEWS+" WHERE "+ConfigApp.PRODUCT_ID+" = "+id;
                ResultSet res = DataBase.QueryResult(query);
                try {
                    res.first();
                    do {
                        ImageView imgItem = null;
                        String name = null;
                        int user_id = res.getInt(ConfigApp.USER_ID);
                        if(list_tmp_users.get(user_id)==null) {
                            query = "SELECT "+ConfigApp.AVATAR+", " + ConfigApp.NAME + " FROM " + ConfigApp.TABLE_USERS
                                    +" WHERE "+ConfigApp.ID+" = "+user_id;
                            ResultSet resultSet = DataBase.QueryResult(query);
                            resultSet.first();
                            try {
                                JSONParser JsonParser = new JSONParser();
                                JSONObject JsonImg = (JSONObject) JsonParser.parse(resultSet.getString(ConfigApp.AVATAR));
                                Image imgLoadItem = new MyImageLoader().Load((String) JsonImg.get("data"),(String) JsonImg.get("format"));
                                imgItem = new ImageView(imgLoadItem);
                            }catch(Exception et){ imgItem = new ImageView(new Image("/res/none_image.png"));}

                            name = resultSet.getString(ConfigApp.NAME);
                            list_tmp_users.put(user_id,name);
                        }else{
                            Reviews r = new Reviews(null,"","");
                            name = list_tmp_users.get(user_id);
                            int index = 0;
                            while(r.getName()!=name){
                                r = Reviews_List.get(index);
                                index++;
                            }
                            Image imgLoadItem = r.getAvatar().getImage();
                            imgItem = new ImageView(imgLoadItem);
                        }
                        imgItem.setFitHeight(60);
                        imgItem.setFitWidth(60);
                        String text = res.getString(ConfigApp.TEXT);
                        Reviews_List.add(new Reviews(imgItem,name,text));
                    }while(res.next());
                    ListReviews.setItems(Reviews_List);
                }catch (Exception e){}
            }
        }).start();
    }
    private void SendReviews(Spare_parts thisItem){
        String textReviews = SendReviewsText.getText();
        if(!textReviews.isEmpty()){
            String query = "INSERT INTO "+ ConfigApp.TABLE_REVIEWS+"("+ConfigApp.USER_ID+", "+ConfigApp.TEXT+", "
                    +ConfigApp.PRODUCT_ID+") VALUES ("+Main.thisUser.getId()+", \'"+textReviews+"\', "+thisItem.getId()+");";
            boolean b = DataBase.QueryBoolean(query);
            if(b){
                DataBase.UpdateStatistics(ConfigApp.J_COUNT_REVIEWS,Main.thisUser.getId());
                Reviews_List.clear();
                list_tmp_users.clear();
                ListReviews.getItems().remove(0,ListReviews.getItems().size());
                SendReviewsText.clear();
                ContainerSendReviews.setVisible(false);
                LoadReviews(thisItem.getId());
            }else{
                Main.DisplayNotification("Помилка при надсиланні","Виникла не зрозуміла помилка, вибачте (",null);
            }
        }else{
            Main.DisplayNotification("Помилка при надсиланні","Ви забули написати текст для відгука, будьте удажні \uD83D\uDE0A",null);
        }
    }

}
