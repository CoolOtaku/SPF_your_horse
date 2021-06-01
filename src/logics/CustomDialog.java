package logics;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class CustomDialog extends Stage {

    private static final Interpolator EXP_IN = new Interpolator() {
        @Override
        protected double curve(double t) {
            return (t == 1.0) ? 1.0 : 1 - Math.pow(2.0, -10 * t);
        }
    };

    private static final Interpolator EXP_OUT = new Interpolator() {
        @Override
        protected double curve(double t) {
            return (t == 0.0) ? 0.0 : Math.pow(2.0, 10 * (t - 1));
        }
    };

    private ScaleTransition scale1 = new ScaleTransition();
    private ScaleTransition scale2 = new ScaleTransition();

    private SequentialTransition anim = new SequentialTransition(scale1, scale2);

    public CustomDialog(String header, String content, EventHandler actionEvent) {
        Pane root = new Pane();

        scale1.setFromX(0.01);
        scale1.setFromY(0.01);
        scale1.setToY(1.0);
        scale1.setDuration(Duration.seconds(0.33));
        scale1.setInterpolator(EXP_IN);
        scale1.setNode(root);

        scale2.setFromX(0.01);
        scale2.setToX(1.0);
        scale2.setDuration(Duration.seconds(0.33));
        scale2.setInterpolator(EXP_OUT);
        scale2.setNode(root);

        initStyle(StageStyle.TRANSPARENT);
        initModality(Modality.APPLICATION_MODAL);

        Rectangle bg = new Rectangle(500, 250, Color.BLACK);

        Text headerText = new Text(header);
        headerText.setFont(Font.font(20));
        headerText.setFill(Color.WHITE);
        headerText.setWrappingWidth(450);

        Text contentText = new Text(content);
        contentText.setFont(Font.font(14));
        contentText.setFill(Color.WHITE);
        contentText.setWrappingWidth(450);

        VBox box = new VBox(10,
                headerText,
                new Separator(Orientation.HORIZONTAL),
                contentText
        );
        box.setPadding(new Insets(15));

        Button btc = new Button("Закрити");
        btc.setTranslateX(bg.getWidth() - 150);
        btc.setTranslateY(bg.getHeight() - 50);
        btc.setStyle("-fx-background-color: #6B8799; -fx-text-fill: white;");
        btc.setOnAction(e -> closeDialog());

        Button btn = new Button("OK");
        btn.setTranslateX(bg.getWidth() - 50);
        btn.setTranslateY(bg.getHeight() - 50);
        btn.setStyle("-fx-background-color: #6B8799; -fx-text-fill: white;");
        if(actionEvent==null) {
            btn.setOnAction(e -> closeDialog());
        }else{
            btc.setText("Скасувати");
            btn.setText("Так");
            btn.setOnAction(actionEvent);
        }

        root.getChildren().addAll(bg, box, btn, btc);

        setScene(new Scene(root, null));
    }

    public void openDialog() {
        show();
        anim.play();
    }

    public void closeDialog() {
        anim.setOnFinished(e -> close());
        anim.setAutoReverse(true);
        anim.setCycleCount(2);
        anim.playFrom(Duration.seconds(0.66));
    }
}
