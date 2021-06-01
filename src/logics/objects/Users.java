package logics.objects;

import javafx.scene.image.ImageView;
import logics.ConfigApp;
import logics.DataBase;
import logics.Main;

public class Users {
    private int id;
    private ImageView avatar;
    private String name;
    private String mail;
    private String type;

    public Users(int id, ImageView avatar, String name, String mail, String type) {
        this.id = id;
        this.avatar = avatar;
        this.name = name;
        this.mail = mail;
        this.type = type;
    }
    public Users() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ImageView getAvatar() {
        return avatar;
    }

    public void setAvatar(ImageView avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static void DeleteUser(int id){
        String query = "DELETE FROM "+ ConfigApp.TABLE_USERS+" WHERE "+ConfigApp.ID+" = "+id;
        boolean b = DataBase.QueryBoolean(query);
        if(b){
            Main.DisplayNotification("Видалення","Видалення пройшло успішно!",null);
            query = "DELETE FROM "+ ConfigApp.TABLE_BOOKED_USERS+" WHERE "+ConfigApp.USER_ID+" = "+id;
            DataBase.QueryBoolean(query);
        }else{
            Main.DisplayNotification("Не вдале видалення","Видалення перервано по не зрозумілих причинах!",null);
        }
    }

}
