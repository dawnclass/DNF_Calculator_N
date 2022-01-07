package org.dnf_calc_n.ui;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.*;
import java.util.HashMap;

public class Common {

    public static HashMap<String, Font> loadFont(){
        HashMap<String, Font> fontMap = new HashMap<>();
        fontMap.put("small", new Font("맑은 고딕", Font.PLAIN, 10));
        fontMap.put("normal", new Font("맑은 고딕", Font.PLAIN, 12));
        fontMap.put("bold", new Font("맑은 고딕", Font.BOLD, 14));
        fontMap.put("large", new Font("맑은 고딕", Font.BOLD, 18));
        fontMap.put("huge", new Font("맑은 고딕", Font.BOLD, 24));
        return fontMap;
    }

    public static JSONObject loadJsonObject(String fileStr){
        try{
            var parser = new JSONParser();
            var reader = new BufferedReader(new FileReader(fileStr));
            return (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e){
            e.printStackTrace();
            return null;
        }
    }

    public static ImageIcon changeBright(
            JComponent component, ImageIcon im, double brightness
    ){
        try{
            Image image = im.getImage();
            ImageFilter filter = new RGBImageFilter() {
                @Override
                public int filterRGB(int x, int y, int rgb) {
                    return ((rgb & 0xFF000000) ^
                            (int)(((rgb & 0xFF0000) >> 16) * brightness) << 16 ^
                            (int)(((rgb & 0xFF00) >> 8) * brightness) << 8 ^
                            (int)(((rgb & 0xFF)) * brightness));
                }
            };
            ImageProducer producer = new FilteredImageSource(image.getSource(), filter);
            Image resultImage = component.createImage(producer);
            return new ImageIcon(resultImage);
        }catch (NullPointerException e){
            return im;
        }
    }

    public static void saveCacheData(String file, String key, String value){
        try{
            var parser = new JSONParser();
            var reader = new BufferedReader(new FileReader("cache/"+file+".json"));
            var json = (JSONObject) parser.parse(reader);
            json.put(key, value);
            var writer = new BufferedWriter(new FileWriter("cache/"+file+".json"));
            writer.write(json.toJSONString());
            writer.flush();
            writer.close();
        }catch (IOException | ParseException e){
            e.printStackTrace();
        }
    }

}
