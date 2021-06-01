package logics.controllers;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import logics.ConfigApp;
import logics.DataBase;
import logics.Main;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.sql.ResultSet;

public class Statistics {

    @FXML
    private Text NameView;
    @FXML
    private Text StatisticsText1;
    @FXML
    private Text StatisticsText2;
    @FXML
    private Text StatisticsText3;
    @FXML
    private Text StatisticsText4;
    @FXML
    private Text StatisticsText5;
    @FXML
    private Text StatisticsText6;

    @FXML
    void initialize() {
        NameView.setText(Main.thisUser.getName());
        String query = "SELECT * FROM "+ ConfigApp.TABLE_STATISTICS+" WHERE "+ConfigApp.USER_ID+" = "+Main.thisUser.getId();
        ResultSet res = DataBase.QueryResult(query);
        try {
            res.first();
            JSONParser JsonParser = new JSONParser();
            JSONObject JsonData = (JSONObject) JsonParser.parse(res.getString(ConfigApp.STATISTICS_DATA));

            StatisticsText1.setText("Кількість опублікованих товарів: "+JsonData.get(ConfigApp.J_COUNT_PUBLICATION));
            StatisticsText2.setText("Кількість написаних відгуків на товари: "+JsonData.get(ConfigApp.J_COUNT_REVIEWS));
            StatisticsText3.setText("Кількість всього замовлень звязаних з вами: "+JsonData.get(ConfigApp.J_COUNT_ORDERS));
            StatisticsText4.setText("Кількість виконаних цих замовлень: "+JsonData.get(ConfigApp.J_COUNT_BOUGHTS));
            StatisticsText5.setText("Кількість видалених вами замовлень: "+JsonData.get(ConfigApp.J_COUNT_DELETE_ORDERS));
            StatisticsText6.setText("Кількість видалених вами товарів: "+JsonData.get(ConfigApp.J_COUNT_DELETE_PUBLICATION));
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }
}
