package org.dnf_calc_n;

import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType;
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

    public static String convertCodeToExplain(String code){
        return switch (code) {
            case "D" -> "데미지 증가";
            case "DA" -> "데미지 추가 증가";
            case "CD" -> "크리티컬 데미지 증가";
            case "CDA" -> "크리티컬 데미지 추가 증가";
            case "AD" -> "추가 데미지";
            case "AED" -> "속성 추가 데미지";
            case "TD" -> "모든 공격력 증가";
            case "A" -> "물리, 마법, 독립공격력 증가";
            case "AP" -> "물리, 마법, 독립공격력 % 증가";
            case "S" -> "힘, 지능 증가";
            case "SP" -> "힘, 지능 %증가";
            case "E" -> "속성 강화 증가";
            case "DD" -> "지속 피해";
            case "SD" -> "스킬 공격력 증가";
            case "CR" -> "크리티컬 확률 증가";
            case "AS" -> "공격 속도 증가";
            case "MS" -> "이동 속도 증가";
            case "LVL" -> "스킬 레벨 증가";
            case "LVD" -> "구간 스킬 공격력 증가";
            case "CTD" -> "쿨타임 감소";
            case "CRD" -> "쿨타임 회복 속도 증가";
            default -> "오류";
        };
    }

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

    public static void writeCSVFile(String jobName, Double[] tranArray){
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
