package logics;

import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

public class MyImageLoader {
    public static Image Load(String data,String format){
        try {
            byte[] resByteArray = Base64.getDecoder().decode(data);
            Image img = new Image(new ByteArrayInputStream(resByteArray));
            return img;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String Img_to_Data(String path, String format){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
            BufferedImage image = ImageIO.read(new File(path));
            ImageIO.write(image, format, baos);
            baos.flush();
            String data = Base64.getEncoder().encodeToString(baos.toByteArray());
            baos.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getFormat(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
}
