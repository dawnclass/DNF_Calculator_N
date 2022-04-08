package org.dnf_calc_n.ui.main;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.calculate.ScoreFarming;
import org.dnf_calc_n.ui.sub.WindowExplain;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PanelInfo extends JPanel {

    Common common = new Common();

    public HashMap<String, String> mapEquipments = new HashMap<>();
    public HashMap<String, String> getMapEquipments(){
        return mapEquipments;
    }
    public HashMap<String, JButton> mapInfoButtons = new HashMap<>();
    public HashMap<String, JButton> getMapInfoButtons(){
        return mapInfoButtons;
    }
    HashMap<String, ImageIcon> mapIconItem;
    HashMap<String, ImageIcon> mapIconExtra;
    JSONObject equipmentData;

    WindowExplain windowExplain;
    ScoreFarming scoreFarming;
    HashMap<String, HashMap<String, Double>> mapFarmingScore;

    public PanelInfo(JPanel root, HashMap<String, ImageIcon> mapIconItem,
                     HashMap<String, ImageIcon> mapIconExtra,
                     JSONObject equipmentData,
                     WindowExplain windowExplain){
        this.equipmentData = equipmentData;
        this.windowExplain = windowExplain;
        this.mapIconItem = mapIconItem;
        this.mapIconExtra = mapIconExtra;
        this.setBackground(new Color(50, 46, 52));
        this.setBounds(10, 10, 250, 150);
        this.setLayout(null);
        root.add(this);

        scoreFarming = new ScoreFarming(equipmentData);
        updateInfo();
    }

    String selectedMyth = "";

    public boolean setEquipment(String equipment){
        String code;
        boolean isMyth = false;
        if(equipment.length()==6){
            code = "77";
        }else{
            code = equipment.substring(0, 2);
            if("1".equals(equipment.substring(equipment.length()-1))) isMyth = true;
        }
        if(equipment.equals(mapEquipments.get(code))){  // 동일 장비 선택
            mapEquipments.remove(code);
            common.deleteEquipmentCacheData(equipment, false);
            if(isMyth){
                selectedMyth = "";
            }
        }else{  //동일 장비 아님
            if(isMyth && !"".equals(selectedMyth) && !selectedMyth.equals(code)){  //신화 중복
                return false;
            }else{
                if(mapEquipments.get(code) != null){  //이미 템 있을경우 (교체)
                    common.deleteEquipmentCacheData(mapEquipments.get(code), false);
                    if(!isMyth && selectedMyth.equals(code)) selectedMyth = "";
                }
                mapEquipments.put(equipment.substring(0, 2), equipment);
                common.saveCacheData("selected", "equipments", equipment);
                if(isMyth) selectedMyth = code;
            }

        }
        // System.out.println(mapEquipments);
        return true;
    }

    public void resetInfoPanel(){
        common.deleteEquipmentCacheData("", true);
        mapEquipments.clear();
        selectedMyth = "";
        updateInfo();
    }

    private final String[] TAGS = {"77", "11", "12", "13", "14", "15",
            "21", "22", "23", "31", "32", "33"};
    private final int[][] POS = {{6, 0}, {1, 0}, {0, 1}, {0, 0}, {1, 1}, {0, 2},
            {6, 1}, {7, 1}, {7, 2}, {6, 2}, {7, 3}, {6, 3}};
    private final int[] START_POS = {6, 6};

    public void updateInfo(){
        for(int i=0;i<12;i++){
            String tag = TAGS[i];
            int[] pos = POS[i];
            JButton nowBtn;
            if(mapInfoButtons.get(tag) == null){
                nowBtn = new JButton();
                nowBtn.setBackground(new Color(50, 46, 52));
                nowBtn.setBounds(START_POS[0] + 30 * pos[0], START_POS[1] + 30 * pos[1], 28, 28);
                nowBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
                this.add(nowBtn);
                mapInfoButtons.put(tag, nowBtn);
            }else{
                nowBtn = mapInfoButtons.get(tag);
            }
            if(mapEquipments.get(tag) != null){
                nowBtn.setIcon(mapIconItem.get(mapEquipments.get(tag)));
                nowBtn.setToolTipText(
                        (String)((JSONObject)equipmentData.get(mapEquipments.get(tag))).get("이름")
                );
                nowBtn.addActionListener(e -> {
                    windowExplain.getEquipmentCode(mapEquipments.get(tag));
                    windowExplain.setVisible(true);
                });
            }else{
                nowBtn.setIcon(mapIconExtra.get("info"+tag));
            }
        }
        this.updateUI();;

        farmingScore = scoreFarming.calculateScore(mapEquipments);
        this.repaint();



    }
    HashMap<String, Double> farmingScore;
    ArrayList<Score> scoreArray = new ArrayList<>();

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        scoreArray = new ArrayList<>();
        g.drawImage(mapIconExtra.get("bg_info").getImage(), 0, 0, new Color(50, 46, 52), this);
        try{
            for(String dungeon : farmingScore.keySet()){
                // System.out.println(dungeon + " " + farmingScore.get(dungeon).toString());
                scoreArray.add(new Score(dungeon, farmingScore.get(dungeon)));
            }
            Collections.sort(scoreArray);
            g.setColor(Color.WHITE);
            g.setFont(common.loadFont().get("small"));
            g.drawString("<파밍 드랍율>", 95, 15);
            for(int i=0;i<scoreArray.size();i++){
                if(i < 3) {
                    g.setColor(Color.WHITE);
                }else{
                    g.setColor(Color.LIGHT_GRAY);
                }
                g.drawString(scoreArray.get(i).toString(true), 86, 35+15*i);
                g.drawString(scoreArray.get(i).toString(false), 131, 35+15*i);
            }
        }catch (Exception ignored){}
    }
}

class Score implements Comparable<Score>{

    String dungeon;
    double score;

    public Score(String dungeon, double score){
        this.dungeon = dungeon;
        this.score = score;
    }

    @Override
    public int compareTo(Score score) {
        if(score.score < this.score){
            return -1;
        }else if(score.score > this.score){
            return 1;
        }
        return 0;
    }

    public String toString(boolean tg){
        if(tg){
            return dungeon;
        }else{
            return ": "+(int)(score*10)/10.0 + "%";
        }
    }
}