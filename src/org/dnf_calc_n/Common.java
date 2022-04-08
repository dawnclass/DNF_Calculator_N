package org.dnf_calc_n;

import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType;
import org.json.simple.JSONArray;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Common {

    public HashMap<String, Font> loadFont(){
        HashMap<String, Font> fontMap = new HashMap<>();
        fontMap.put("very_small", new Font("맑은 고딕", Font.PLAIN, 8));
        fontMap.put("more_small", new Font("맑은 고딕", Font.PLAIN, 9));
        fontMap.put("small", new Font("맑은 고딕", Font.PLAIN, 10));
        fontMap.put("small_bold", new Font("맑은 고딕", Font.BOLD, 10));
        fontMap.put("normal", new Font("맑은 고딕", Font.PLAIN, 12));
        fontMap.put("normal_bold", new Font("맑은 고딕", Font.BOLD, 12));
        fontMap.put("bold", new Font("맑은 고딕", Font.BOLD, 14));
        fontMap.put("large", new Font("맑은 고딕", Font.BOLD, 18));
        fontMap.put("huge", new Font("맑은 고딕", Font.BOLD, 24));
        return fontMap;
    }

    public JSONObject loadJsonObject(String fileStr){
        try{
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(new BufferedReader(new FileReader(fileStr)));
        } catch (IOException | ParseException | NullPointerException e){
            e.printStackTrace();
            return null;
        }
    }

    public int changeBright(int color, double brightness){
        int r = color / (256*256);
        int g = (color / 256)%256;
        int b = color % 256;
        //System.out.println("전 r="+r+" g="+g+" b="+b);

        r = (int)(r * brightness);
        g = (int)(g * brightness);
        b = (int)(b * brightness);
        //System.out.println("후 r="+r+" g="+g+" b="+b);

        return r*256*256 + g * 256 + b;
    }

    public ImageIcon changeBright(
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

    public void deleteEquipmentCacheData(String value, boolean isReset){
        try{
            JSONParser parser = new JSONParser();
            BufferedReader reader = new BufferedReader(new FileReader("cache/selected.json"));
            JSONObject json = (JSONObject) parser.parse(reader);
            JSONArray equipmentArray;
            if(isReset){
                equipmentArray = new JSONArray();
                json.put("equipments", equipmentArray);
            }else{
                try{
                    equipmentArray = (JSONArray) json.get("equipments");
                    equipmentArray.remove(value);
                    json.put("equipments", equipmentArray);
                }catch (Exception ignored){

                }
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter("cache/selected.json"));
            writer.write(json.toJSONString());
            writer.flush();
            writer.close();
        }catch (IOException | ParseException | NullPointerException e){
            e.printStackTrace();
        }
    }

    public void saveJson(String filePath, JSONObject json){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(json.toJSONString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCacheData(String file, String key, Object value){
        try{
            JSONParser parser = new JSONParser();
            BufferedReader reader = new BufferedReader(new FileReader("cache/"+file+".json"));
            JSONObject json = (JSONObject) parser.parse(reader);
            if(key.equals("equipments")){
                JSONArray equipmentArray;
                try{
                    equipmentArray = (JSONArray) json.get("equipments");
                    if(!equipmentArray.contains(value)){
                        equipmentArray.add(value);
                    }
                }catch (NullPointerException | ClassCastException e){
                    equipmentArray = new JSONArray();
                    equipmentArray.add(value);
                }
                json.put("equipments", equipmentArray);
            }else{
                json.put(key, value);
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter("cache/"+file+".json"));
            writer.write(json.toJSONString());
            writer.flush();
            writer.close();
        }catch (IOException | ParseException | NullPointerException e){
            e.printStackTrace();
        }
    }

    public void writeCSVFile(String jobName, Double[] tranArray){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss-SSS");
        String time = now.format(formatter);
        File csv = new File(jobName+" "+time+".csv");
        BufferedWriter bw = null;
        try{
            bw = new BufferedWriter(new FileWriter(csv, true));

            for (int i=0;i<tranArray.length;i++){
                String aData = "";
                aData = i + "," + tranArray[i];
                bw.write(aData);
                bw.newLine();
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try{
                if(bw!=null){
                    bw.flush();
                    bw.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}
