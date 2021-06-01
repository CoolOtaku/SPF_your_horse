package logics;

import com.mysql.jdbc.Driver;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class DataBase {
    public Connection getDbConnection () throws ClassNotFoundException, SQLException{
        String connectionString = "jdbc:mysql://"+ConfigApp.dbHost+":3306/"+ConfigApp.dbName+"?useUnicode=true&characterEncoding=UTF-8";
        Driver driver = new Driver();
        DriverManager.registerDriver(driver);
        return DriverManager.getConnection(connectionString,ConfigApp.dbUser,ConfigApp.dbPassword);
    }
    public static boolean QueryBoolean(String query){
        try {
            PreparedStatement preparedStatement = Main.dbConnection.prepareStatement(query);
            int rows = 0;
            rows = preparedStatement.executeUpdate();
            boolean res = false;
            if(rows!=0){
                res = true;
            }else {
                res = false;
            }
            return res;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public static ResultSet QueryResult(String query){
        try {
            ResultSet res = null;
            PreparedStatement preparedStatement = Main.dbConnection.prepareStatement(query,
                    ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
            res = preparedStatement.executeQuery();
            return res;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static void WriteLogs(String message){
        try {
            LocalDate lD = LocalDate.now();
            LocalTime lT = LocalTime.now();
            String date = lD.getYear()+"-"+lD.getMonthValue()+"-"+lD.getDayOfMonth()+" "+lT.getHour()
                    +":"+ lT.getMinute()+":"+lT.getSecond();
            String query = "INSERT INTO "+ConfigApp.TABLE_LOGS+"("+ConfigApp.TEXT+", "+ConfigApp.DATE+") VALUES (\'"
                    +message+"\', \'"+date+"\');";
            PreparedStatement preparedStatement = Main.dbConnection.prepareStatement(query);
            preparedStatement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void CreateStatistics(int user_id){
        try {
            JSONObject statistic = new JSONObject();
            statistic.put(ConfigApp.J_COUNT_PUBLICATION, 0);
            statistic.put(ConfigApp.J_COUNT_REVIEWS, 0);
            statistic.put(ConfigApp.J_COUNT_ORDERS, 0);
            statistic.put(ConfigApp.J_COUNT_BOUGHTS, 0);
            statistic.put(ConfigApp.J_COUNT_DELETE_ORDERS, 0);
            statistic.put(ConfigApp.J_COUNT_DELETE_PUBLICATION, 0);

            String query = "INSERT INTO " + ConfigApp.TABLE_STATISTICS + "(" + ConfigApp.USER_ID + ", " + ConfigApp.STATISTICS_DATA + ") VALUES ("
                    + user_id + ", \'" + statistic.toJSONString() + "\');";
            PreparedStatement preparedStatement = Main.dbConnection.prepareStatement(query);
            preparedStatement.executeUpdate();
        }catch (Exception e){ e.printStackTrace();}
    }
    public static void UpdateStatistics(String from, int user_id){
        try {
            String query = "SELECT * FROM " + ConfigApp.TABLE_STATISTICS + " WHERE " + ConfigApp.USER_ID + " = " + user_id;
            ResultSet res = DataBase.QueryResult(query);
            res.first();
            JSONParser JsonParser = new JSONParser();
            JSONObject JsonData = (JSONObject) JsonParser.parse(res.getString(ConfigApp.STATISTICS_DATA));
            String data = String.valueOf(JsonData.get(from));
            int data_count = Integer.parseInt(data);
            data_count++;
            JsonData.replace(from,data_count);

            query = "UPDATE "+ConfigApp.TABLE_STATISTICS+" SET "+ConfigApp.STATISTICS_DATA+" = \'"+JsonData.toJSONString()+"\' WHERE "
                    +ConfigApp.USER_ID+" = "+user_id;
            PreparedStatement preparedStatement = Main.dbConnection.prepareStatement(query);
            preparedStatement.executeUpdate();
        }catch (Exception e){ e.printStackTrace();}
    }
}
