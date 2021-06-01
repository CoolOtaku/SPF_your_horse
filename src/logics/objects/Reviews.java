package logics.objects;

import javafx.scene.image.ImageView;

public class Reviews {
    private ImageView avatar;
    private String name;
    private String text;

    public Reviews(ImageView avatar, String name, String text) {
        this.avatar = avatar;
        this.name = name;
        this.text = text;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
