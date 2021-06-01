package logics.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import java.sql.ResultSet;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import logics.*;
import logics.objects.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class BuyOrdering {

    @FXML
    private Text NameItem;
    @FXML
    private Text PriceItem;
    @FXML
    private Text AllPriceItem;
    @FXML
    private Spinner<Integer> SpinnerCount;
    @FXML
    private Text DescriptionItem;
    @FXML
    private Text NameUserSeller;
    @FXML
    private TextField MailUserSeller;
    @FXML
    private ImageView ImageUserSeller;
    @FXML
    private Text NameUserShopper;
    @FXML
    private TextField MailUserShopper;
    @FXML
    private ImageView ImageUserShopper;
    @FXML
    private Text MaxCount;
    @FXML
    private Button ButtonCancel;
    @FXML
    private Button ButtonDraw_Up;

    private double thisPrice = Shop.SelectItem.getPrice();
    private int user_id = 0;
    private int maxCount = 0;

    @FXML
    void initialize() {
        NameItem.setText(Shop.SelectItem.getTitle());
        PriceItem.setText("Ціна за одиницю: "+Shop.SelectItem.getPrice()+" грн");
        String query = "SELECT "+ ConfigApp.DESCRIPTION+", "+ConfigApp.COUNT+", "+ConfigApp.USER_ID+" FROM "
                + ConfigApp.TABLE_SPARE_PARTS+" WHERE "+ConfigApp.ID+" = "+Shop.SelectItem.getId();
        ResultSet res = DataBase.QueryResult(query);
        try {
            res.first();
            DescriptionItem.setText(res.getString(ConfigApp.DESCRIPTION));

            maxCount = res.getInt(ConfigApp.COUNT);
            SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxCount, 1);
            SpinnerCount.setValueFactory(valueFactory);
            SpinnerCount.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
            AllPriceItem.setText("Загальна ціна: "+Shop.SelectItem.getPrice()*SpinnerCount.getValue());
            MaxCount.setText("Максимальна кількість: "+maxCount);

            user_id = res.getInt(ConfigApp.USER_ID);
            query = "SELECT "+ConfigApp.AVATAR+", "+ConfigApp.NAME+", "+ConfigApp.MAIL+" FROM "+ConfigApp.TABLE_USERS+" WHERE "
                    +ConfigApp.ID+" = "+user_id;
            res = DataBase.QueryResult(query);
            res.first();

            User.InsertUserAvatar(ImageUserSeller);
            User.InsertUserAvatar(ImageUserShopper);
            Image imgUser = null;
            if(res.getString(ConfigApp.AVATAR)!=null){
                JSONParser JsonParser = new JSONParser();
                JSONObject JsonAvatar = (JSONObject) JsonParser.parse(res.getString(ConfigApp.AVATAR));
                imgUser = new MyImageLoader().Load((String) JsonAvatar.get("data"),(String) JsonAvatar.get("format"));
            }else {
                switch(res.getInt(ConfigApp.TYPE_USER)){
                    case 1: imgUser = new Image("/res/seller.png"); break;
                    case 0: imgUser = new Image("/res/shopper.png"); break;
                    default: break;
                }
            }
            ImageUserSeller.setImage(imgUser);
            ImageUserShopper.setImage(Main.thisUser.getAvatar());

            NameUserSeller.setText(res.getString(ConfigApp.NAME));
            MailUserSeller.setText(res.getString(ConfigApp.MAIL));
            NameUserShopper.setText(Main.thisUser.getName());
            MailUserShopper.setText(Main.thisUser.getMail());
            res.close();
        }catch (Exception e){e.printStackTrace();}
        ButtonDraw_Up.setOnAction(actionEvent -> {
            Draw_Up();
        });
        ButtonCancel.setOnAction(actionEvent -> {
            ButtonCancel.getScene().getWindow().hide();
        });
        SpinnerCount.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable,Integer oldValue,Integer newValue) {
                thisPrice = Shop.SelectItem.getPrice()*newValue;
                AllPriceItem.setText("Загальна ціна: "+thisPrice);
            }
        });
    }
    private void Draw_Up(){
        String query = "INSERT INTO "+ ConfigApp.TABLE_ORDERS+"("+ConfigApp.TITLE+", "+ConfigApp.PRICE+", "
                +ConfigApp.COUNT+", "+ConfigApp.USER_ID+", "+ConfigApp.DESCRIPTION+", "+ConfigApp.BUY_USER_ID+", "
                +ConfigApp.PRODUCT_ID+") VALUES (\'"+NameItem.getText()+"\', "+thisPrice+", "+SpinnerCount.getValue()
                +", "+user_id+", \'"+DescriptionItem.getText()+"\', "+Main.thisUser.getId()+", "+Shop.SelectItem.getId()+");";
        boolean b = DataBase.QueryBoolean(query);
        if(b){
            if(maxCount-SpinnerCount.getValue()==0){
                query = "DELETE FROM "+ConfigApp.TABLE_SPARE_PARTS+" WHERE "+ConfigApp.ID+" = "+Shop.SelectItem.getId();
            }else{
                int newCount = maxCount-SpinnerCount.getValue();
                query = "UPDATE "+ConfigApp.TABLE_SPARE_PARTS+" SET "+ConfigApp.COUNT+" = "+newCount
                        +" WHERE "+ConfigApp.ID+" = "+Shop.SelectItem.getId();
            }
            b = DataBase.QueryBoolean(query);
            if (!b){
                Main.DisplayNotification("Помилка при оформленні","Сталася не відома помилка!",null);
                return;
            }
            User.SendMailMessage(MailUserSeller.getText(),"Нове замовлення із магазину SPF your horse","Оформлено " +
                    "нове замовлення на ваш товар: \""+NameItem.getText()+"\" користувачем: "+NameUserShopper.getText()+" в кількості: "+SpinnerCount.getValue()
            +" штук. Перевірте будьласка це замовлення, та звяжіться із цим користувачем за адресою: "+MailUserShopper.getText());
            ButtonDraw_Up.getScene().getWindow().hide();
            Main.DisplayNotification("Замовлення оформлено","Ваше замовлення на товар: "+NameItem.getText()
                    +" оформлено, оцікуйтевідповіді продавця.",null);
            DataBase.UpdateStatistics(ConfigApp.J_COUNT_ORDERS,Main.thisUser.getId());
            DataBase.UpdateStatistics(ConfigApp.J_COUNT_ORDERS,user_id);
        }else{
            Main.DisplayNotification("Помилка при оформленні","Не вдалося оформити замовлення на цей продукт!",null);
        }
    }
}