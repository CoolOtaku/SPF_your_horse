package logics.controllers;

import java.io.File;
import java.sql.ResultSet;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import logics.ConfigApp;
import logics.DataBase;
import logics.Main;
import logics.MyImageLoader;
import org.json.simple.JSONObject;
import static logics.MyImageLoader.getFormat;

public class Add_Edit_SPF {

    @FXML
    private ImageView Item_Image;
    @FXML
    private TextField ItemName;
    @FXML
    private TextArea ItemDescription;
    @FXML
    private TextField ItemPrice;
    @FXML
    private TextField ItemCount;
    @FXML
    private Button ButtonLookImage;
    @FXML
    private Button ButtonAdd;
    @FXML
    private Button ButtonCancel;

    public static String Sesion = "";
    private JSONObject JsonItemImg = null;

    @FXML
    void initialize() {
        if(Sesion.equals("edit")){
            ButtonAdd.setText("Зберегти");
            Item_Image.setImage(Shop.SelectItem.getImg().getImage());
            ItemName.setText(Shop.SelectItem.getTitle());
            ItemPrice.setText(String.valueOf(Shop.SelectItem.getPrice()));
            String query = "SELECT "+ConfigApp.DESCRIPTION+", "+ConfigApp.COUNT+" FROM "
                    + ConfigApp.TABLE_SPARE_PARTS+" WHERE "+ConfigApp.ID+" = "+Shop.SelectItem.getId();
            ResultSet result = DataBase.QueryResult(query);
            try{
                result.first();
                try {
                    ItemCount.setText(String.valueOf(result.getInt(ConfigApp.COUNT)));
                    ItemDescription.setText(result.getString(ConfigApp.DESCRIPTION));
                }catch(Exception et){et.printStackTrace();}
            }catch (Exception e){e.printStackTrace();}
        }
        ButtonLookImage.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File selectFile = fileChooser.showOpenDialog(null);

            if(selectFile != null){
                String imgData = MyImageLoader.Img_to_Data(selectFile.getAbsolutePath(),getFormat(selectFile));
                Image img = MyImageLoader.Load(imgData,getFormat(selectFile));
                Item_Image.setImage(img);
                JsonItemImg = new JSONObject();
                JsonItemImg.put("data",imgData);
                JsonItemImg.put("format",getFormat(selectFile));
            }else{
                JsonItemImg = null;
            }
        });
        ButtonCancel.setOnAction(actionEvent -> {
            ButtonCancel.getScene().getWindow().hide();
        });
        ButtonAdd.setOnAction(actionEvent -> {
            if(Sesion.equals("add")){
                Add();
            }else{
                Edit();
            }
        });
    }
    private void Add(){
        if(ItemName.getText().isEmpty()){
            Main.DisplayNotification("Помилка при додаванні","Ви не ввели назву продукта!",null);
            return;
        }
        if(ItemPrice.getText().isEmpty()){
            Main.DisplayNotification("Помилка при додаванні","Ви не ввели ціну продукта!",null);
            return;
        }
        if(ItemCount.getText().isEmpty()){
            Main.DisplayNotification("Помилка при додаванні","Ви не ввели кількість продукта!",null);
            return;
        }
        ItemName.setText(ItemName.getText().replace('\'','′'));
        ItemDescription.setText(ItemDescription.getText().replace('\'','′'));
        String query = "INSERT INTO "+ ConfigApp.TABLE_SPARE_PARTS+"("+ConfigApp.TITLE+", "+ConfigApp.PRICE+", "
                +ConfigApp.COUNT+", "+ConfigApp.USER_ID+") VALUES (\'"+ItemName.getText()+"\', "+ItemPrice.getText()
                +", "+ItemCount.getText()+", "+Main.thisUser.getId()+");";
        boolean b = DataBase.QueryBoolean(query);
        if(b){
            DataBase.UpdateStatistics(ConfigApp.J_COUNT_PUBLICATION,Main.thisUser.getId());
            query = "SELECT "+ConfigApp.ID+" FROM "+ConfigApp.TABLE_SPARE_PARTS +" WHERE "+ConfigApp.USER_ID
                    +" = "+Main.thisUser.getId()+" AND "+ConfigApp.TITLE+" LIKE \'"+ItemName.getText()+"\'";
            ResultSet res = DataBase.QueryResult(query);
            try {
                res.last();
                int ProductId = 0;
                ProductId = res.getInt(ConfigApp.ID);
                if(ProductId!=0){
                    if(JsonItemImg!=null){
                        query = "UPDATE "+ConfigApp.TABLE_SPARE_PARTS+" SET "+ConfigApp.IMG+" = \'"+JsonItemImg.toJSONString()
                                +"\' WHERE "+ConfigApp.ID+" = "+ProductId;
                        DataBase.QueryBoolean(query);
                    }
                    if(!ItemDescription.getText().isEmpty()){
                        query = "UPDATE "+ConfigApp.TABLE_SPARE_PARTS+" SET "+ConfigApp.DESCRIPTION+" = \'"+ItemDescription.getText()
                                +"\' WHERE "+ConfigApp.ID+" = "+ProductId;
                        DataBase.QueryBoolean(query);
                    }
                }
            }catch (Exception e){e.printStackTrace();}
            Shop s = (Shop) Main.Context;
            s.DeleteInfoSPF_List();
            s.Load_SPF(null);
            ButtonAdd.getScene().getWindow().hide();
        }else{
            Main.DisplayNotification("Помилка при додаванні","Виникла не відома помилка, можливо ви ввели " +
                    "не вірні дані наприклад ціна та кількісь приймає тільки числові дані",null);
        }
    }
    private void Edit(){
        if(ItemName.getText().isEmpty()){
            Main.DisplayNotification("Помилка при редагуванні","Назву продукта не заповнена!",null);
            return;
        }
        if(ItemPrice.getText().isEmpty()){
            Main.DisplayNotification("Помилка при редагуванні","Ціну продукта не заповнена!",null);
            return;
        }
        if(ItemCount.getText().isEmpty()){
            Main.DisplayNotification("Помилка при редагуванні","Кількість продукта не заповнена!",null);
            return;
        }
        ItemName.setText(ItemName.getText().replace('\'','′'));
        ItemDescription.setText(ItemDescription.getText().replace('\'','′'));
        String query = "UPDATE "+ConfigApp.TABLE_SPARE_PARTS+" SET "+ConfigApp.TITLE+" = \'"+ItemName.getText()+"\', "
                +ConfigApp.PRICE+" = "+ItemPrice.getText()+", "+ConfigApp.COUNT+" = "+ItemCount.getText()
                +" WHERE "+ConfigApp.ID+" = "+Shop.SelectItem.getId();
        boolean b = DataBase.QueryBoolean(query);
        if(b){
            if(JsonItemImg!=null){
                query = "UPDATE "+ConfigApp.TABLE_SPARE_PARTS+" SET "+ConfigApp.IMG+" = \'"+JsonItemImg.toJSONString()
                        +"\' WHERE "+ConfigApp.ID+" = "+Shop.SelectItem.getId();
                DataBase.QueryBoolean(query);
            }
            if(!ItemDescription.getText().isEmpty()){
                query = "UPDATE "+ConfigApp.TABLE_SPARE_PARTS+" SET "+ConfigApp.DESCRIPTION+" = \'"+ItemDescription.getText()
                        +"\' WHERE "+ConfigApp.ID+" = "+Shop.SelectItem.getId();
                DataBase.QueryBoolean(query);
            }
            Shop s = (Shop) Main.Context;
            s.DeleteInfoSPF_List();
            s.Load_SPF(null);
            ButtonAdd.getScene().getWindow().hide();
        }else{
            Main.DisplayNotification("Помилка при редагуванні","Виникла не відома помилка, можливо ви ввели " +
                    "не вірні дані наприклад ціна та кількісь приймає тільки числові дані",null);
        }
    }
}
