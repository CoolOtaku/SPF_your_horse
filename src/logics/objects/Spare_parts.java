package logics.objects;

import javafx.scene.image.ImageView;
import logics.ConfigApp;
import logics.DataBase;
import logics.Main;

public class Spare_parts {
    private int id;
    private ImageView img;
    private String title;
    private double price;

    public Spare_parts(int id, ImageView img, String title, double price) {
        this.id = id;
        this.img = img;
        this.title = title;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ImageView getImg() {
        return img;
    }

    public void setImg(ImageView img) {
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public static void DeleteSPF(Spare_parts spf){
        String query = "DELETE FROM "+ ConfigApp.TABLE_SPARE_PARTS+" WHERE "+ConfigApp.ID+" = "+spf.getId();
        boolean b = DataBase.QueryBoolean(query);
        if(b){
            Main.DisplayNotification("Видалення","Видалення пройшло успішно!",null);
            DataBase.UpdateStatistics(ConfigApp.J_COUNT_DELETE_PUBLICATION,Main.thisUser.getId());
        }else{
            Main.DisplayNotification("Не вдале видалення","Видалення перервано по не зрозумілих причинах!",null);
        }
    }
}
