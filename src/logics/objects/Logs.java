package logics.objects;

import logics.ConfigApp;
import logics.DataBase;
import logics.Main;

public class Logs {
    private int id;
    private String text;
    private String date_time;

    public Logs(int id, String text, String date_time) {
        this.id = id;
        this.text = text;
        this.date_time = date_time;
    }
    public Logs() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public static void DeleteLog(int id){
        String query = "DELETE FROM "+ ConfigApp.TABLE_LOGS+" WHERE "+ConfigApp.ID+" = "+id;
        boolean b = DataBase.QueryBoolean(query);
        if(b){
            Main.DisplayNotification("Видалення","Видалення пройшло успішно!",null);
        }else{
            Main.DisplayNotification("Не вдале видалення","Видалення перервано по не зрозумілих причинах!",null);
        }
    }
}
