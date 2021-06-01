package logics.objects;

import logics.ConfigApp;
import logics.DataBase;
import logics.Main;
import java.sql.ResultSet;

public class Order {

    private int id;
    private String name;
    private double price;
    private int count;
    private String seller;
    private String shopper;

    public Order(int id, String name, double price, int count, String seller, String shopper) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.count = count;
        this.seller = seller;
        this.shopper = shopper;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getShopper() {
        return shopper;
    }

    public void setShopper(String shopper) {
        this.shopper = shopper;
    }

    public static void DeleteOrder(Order obj){
        String query = "DELETE FROM "+ ConfigApp.TABLE_ORDERS+" WHERE "+ConfigApp.ID+" = "+obj.getId();
        boolean b = DataBase.QueryBoolean(query);
        if(b){
            Main.DisplayNotification("Видалення","Видалення пройшло успішно!",null);
            DataBase.UpdateStatistics(ConfigApp.J_COUNT_DELETE_ORDERS,Main.thisUser.getId());
        }else{
            Main.DisplayNotification("Не вдале видалення","Видалення перервано по не зрозумілих причинах!",null);
        }
    }
    public static void ReturnCountOrder(Order obj){
        String query = "SELECT "+ConfigApp.PRODUCT_ID+" FROM "+ConfigApp.TABLE_ORDERS+" WHERE "+ConfigApp.ID+" = "+obj.getId();
        ResultSet res = DataBase.QueryResult(query);
        try {
            res.first();
            int id = res.getInt(ConfigApp.PRODUCT_ID);
            query = "SELECT "+ConfigApp.COUNT+" FROM "+ConfigApp.TABLE_SPARE_PARTS+" WHERE "+ConfigApp.ID+" = "+id;
            res = DataBase.QueryResult(query);
            res.first();
            int count = res.getInt(ConfigApp.COUNT);
            count+=obj.getCount();
            query = "UPDATE "+ConfigApp.TABLE_SPARE_PARTS+" SET "+ConfigApp.COUNT+" = "+count
                    +" WHERE "+ConfigApp.ID+" = "+id;
            DataBase.QueryBoolean(query);
        }catch (Exception e){e.printStackTrace();}
    }
    public static void Note(int user_id){
        String query = "UPDATE "+ConfigApp.TABLE_ORDERS+" SET "+ConfigApp.NOTE+" = "+1
                +" WHERE "+ConfigApp.USER_ID+" = "+user_id+" AND "+ConfigApp.NOTE+" = 0";
        DataBase.QueryBoolean(query);
    }
    public static void AddToBOUGHTS(Order obj){
        String query = "SELECT "+ConfigApp.USER_ID+", "+ConfigApp.BUY_USER_ID+", "+ConfigApp.DESCRIPTION+", "+ConfigApp.PRODUCT_ID
                +" FROM "+ConfigApp.TABLE_ORDERS+" WHERE "+ConfigApp.ID+" = "+obj.getId();
        ResultSet res = DataBase.QueryResult(query);
        try {
            res.first();
            int product_id = res.getInt(ConfigApp.PRODUCT_ID);
            int user_id = res.getInt(ConfigApp.USER_ID);
            int buy_user_id = res.getInt(ConfigApp.BUY_USER_ID);
            String description = res.getString(ConfigApp.DESCRIPTION);
            DeleteOrder(obj);
            query = "INSERT INTO "+ ConfigApp.TABLE_BOUGHTS+"("+ConfigApp.TITLE+", "+ConfigApp.PRICE+", "
                    +ConfigApp.COUNT+", "+ConfigApp.USER_ID+", "+ConfigApp.DESCRIPTION+", "+ConfigApp.BUY_USER_ID+", "
                    +ConfigApp.PRODUCT_ID+") VALUES (\'"+obj.getName()+"\', "+obj.getPrice() +", "+obj.getCount()
                    +", "+user_id+", \'"+description+"\', "+buy_user_id+", "+product_id+");";
            boolean b = DataBase.QueryBoolean(query);
            if(b){
                Main.DisplayNotification("Виконане замовлення","Замовлення позначене як виконане!",null);
                DataBase.UpdateStatistics(ConfigApp.J_COUNT_BOUGHTS,user_id);
                DataBase.UpdateStatistics(ConfigApp.J_COUNT_BOUGHTS,buy_user_id);
            }
        }catch (Exception e){e.printStackTrace();}
    }

}
