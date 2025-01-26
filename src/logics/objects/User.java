package logics.objects;

import javafx.scene.SnapshotParameters;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class User {
    private int id;
    private String name;
    private String mail;
    private String password;
    private int type_user;
    private Image avatar;

    public User(int id, String name, String mail, String password, int type_user, Image avatar) {
        this.id = id;
        this.name = name;
        this.mail = mail;
        this.password = password;
        this.type_user = type_user;
        this.avatar = avatar;
    }
    public User(){

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

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getType_user() {
        return type_user;
    }

    public void setType_user(int type_user) {
        this.type_user = type_user;
    }

    public Image getAvatar() {
        return avatar;
    }

    public void setAvatar(Image avatar) {
        this.avatar = avatar;
    }

    public static void InsertUserAvatar(ImageView imgView){
        Rectangle clip = new Rectangle(0,0,
                imgView.getFitWidth(), imgView.getFitHeight()
        );
        clip.setArcWidth(50);
        clip.setArcHeight(50);
        imgView.setClip(clip);

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        WritableImage image = imgView.snapshot(parameters, null);
        imgView.setClip(null);
        imgView.setEffect(new DropShadow(50, Color.BLACK));

        imgView.setImage(image);

    }
    public static void SendMailMessage(String to, String subject, String text){
        final String from ="[PROJECT_MAIL]";
        final  String password ="[PROJECT_PASSWORD_TO_MAIL]";

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.debug", "true");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from,password);
                    }
                });

        session.setDebug(true);
        try {
            Transport transport = session.getTransport();
            InternetAddress addressFrom = new InternetAddress(from);

            MimeMessage message = new MimeMessage(session);
            message.setHeader("Content-Type", "text/plain; charset=UTF-8");
            message.setSender(addressFrom);
            message.setSubject(subject, "utf-8");
            message.setContent(text, "text/plain; charset=UTF-8");
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            transport.connect();
            Transport.send(message);
            transport.close();
        }catch (Exception e){e.printStackTrace();}
    }
}
