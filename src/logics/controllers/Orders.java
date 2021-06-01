package logics.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.util.Callback;
import logics.ConfigApp;
import logics.DataBase;
import logics.Main;
import logics.objects.Order;
import java.sql.ResultSet;
import java.util.Scanner;

public class Orders {

    @FXML
    private TableView<Order> ListOrders;
    @FXML
    private TableColumn<Order, Integer> ColumnID;
    @FXML
    private TableColumn<Order, String> ColumnName;
    @FXML
    private TableColumn<Order, Double> ColumnPrice;
    @FXML
    private TableColumn<Order, Integer> ColumnCount;
    @FXML
    private TableColumn<Order, String> ColumnSeller;
    @FXML
    private TableColumn<Order, String> ColumnShopper;
    @FXML
    private TableColumn<Order, Button> ColumnActions;
    @FXML
    private Text NameView;

    private Order SelectItem = null;
    ObservableList<Order> Order_List = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        NameView.setText(Main.thisUser.getName());
        ColumnID.setCellValueFactory(new PropertyValueFactory<>("id"));
        ColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        ColumnPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        ColumnCount.setCellValueFactory(new PropertyValueFactory<>("count"));
        ColumnSeller.setCellValueFactory(new PropertyValueFactory<>("seller"));
        ColumnShopper.setCellValueFactory(new PropertyValueFactory<>("shopper"));
        Callback<TableColumn<Order, Button>, TableCell<Order, Button>> cellFoctory = (TableColumn<Order, Button> param) -> {
            final TableCell<Order, Button> cell = new TableCell<Order, Button>() {
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
                            SelectItem = ListOrders.getSelectionModel().getSelectedItem();
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

        Load_Orders();
        Order.Note(Main.thisUser.getId());

        ListOrders.setRowFactory( tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    SelectItem = row.getItem();
                    ClickConfigure();
                }
            });
            row.setOnMouseMoved(event -> {
                ListOrders.getSelectionModel().select(row.getItem());
            });
            return row ;
        });
    }

    private void ClickConfigure(){
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #6B8799; -fx-background-radius: 10;");
        MenuItem note_executed = new MenuItem("Позначити як виконаний");
        note_executed.setStyle("-fx-text-fill: white;");
        MenuItem to_contact = new MenuItem("Зв'язатися");
        to_contact.setStyle("-fx-text-fill: white;");
        MenuItem delete = new MenuItem("Видалити");
        delete.setStyle("-fx-text-fill: white;");

        contextMenu.getItems().addAll(note_executed, to_contact, delete);

        note_executed.setOnAction(actionEvent -> {
            EventHandler handler = new EventHandler<Event>(){
                @Override
                public void handle(Event event) {
                    Main.dialog.closeDialog();
                    Order.AddToBOUGHTS(SelectItem);
                    Order_List.removeAll(Order_List);
                    Load_Orders();
                }
            };
            Main.DisplayNotification("Позначити як виконане","Ви дійсно виконали цей заказ?",handler);
        });
        to_contact.setOnAction(actionEvent -> {
            Scanner scanner1 = new Scanner(SelectItem.getShopper());
            Scanner scanner2 = new Scanner(SelectItem.getSeller());
            scanner1.nextLine();
            scanner2.nextLine();
            String mail1 = scanner1.nextLine();
            String mail2 = scanner2.nextLine();
            if(mail1.equals(Main.thisUser.getMail())) {
                Main.host_service.showDocument("mailto:"+mail2);
            }else{
                Main.host_service.showDocument("mailto:"+mail1);
            }
        });
        delete.setOnAction(actionEvent -> {
            EventHandler handler = new EventHandler<Event>(){
                @Override
                public void handle(Event event) {
                    Main.dialog.closeDialog();
                    Order.ReturnCountOrder(SelectItem);
                    Order.DeleteOrder(SelectItem);
                    Order_List.removeAll(Order_List);
                    Load_Orders();
                }
            };
            Main.DisplayNotification("Видалення заказу","Ви дійсно хочете видалити цей заказ?",handler);
        });
        contextMenu.show(ListOrders.getScene().getWindow());
    }
    private void Load_Orders(){
        String query = "SELECT "+ ConfigApp.ID+", "+ConfigApp.TITLE+", "+ConfigApp.PRICE+", "+ConfigApp.COUNT+", "
                +ConfigApp.USER_ID+", "+ConfigApp.BUY_USER_ID+" FROM "+ConfigApp.TABLE_ORDERS+" WHERE "+ConfigApp.USER_ID
                +" = "+Main.thisUser.getId()+" OR "+ConfigApp.BUY_USER_ID+" = "+Main.thisUser.getId();
        ResultSet result = DataBase.QueryResult(query);
        try {
            result.first();
            do{
                ResultSet res1 = DataBase.QueryResult("SELECT "+ConfigApp.NAME+", "+ConfigApp.MAIL+" FROM "+ConfigApp.TABLE_USERS
                        +" WHERE "+ConfigApp.ID+" = "+result.getInt(ConfigApp.USER_ID));
                res1.first();
                ResultSet res2 = DataBase.QueryResult("SELECT "+ConfigApp.NAME+", "+ConfigApp.MAIL+" FROM "+ConfigApp.TABLE_USERS
                        +" WHERE "+ConfigApp.ID+" = "+result.getInt(ConfigApp.BUY_USER_ID));
                res2.first();
                Order_List.add(new Order(result.getInt(ConfigApp.ID),result.getString(ConfigApp.TITLE),result.getDouble(ConfigApp.PRICE),
                        result.getInt(ConfigApp.COUNT),res1.getString(ConfigApp.NAME)+"\n"+res1.getString(ConfigApp.MAIL),
                        res2.getString(ConfigApp.NAME)+"\n"+res2.getString(ConfigApp.MAIL)));
            }while (result.next());
            ListOrders.setItems(Order_List);
            ListOrders.refresh();
        }catch (Exception e){}
    }
}
