package org.dnf_calc_n.ui.sub;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.calculate.ScoreFarming;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class WindowFarming extends JFrame {

    Common common = new Common();
    HashMap<String, Font> mapFont;
    JPanel panelWindow;
    HashMap<String, String> mapEquipment;
    HashMap<String, ImageIcon> mapIconItem;
    JSONObject equipmentData;
    HashMap<String, ImageIcon> mapIconExtra;
    InfoPanel infoPanel;
    ScoreFarming scoreFarming;

    public WindowFarming(JPanel root, HashMap<String, String> mapEquipment,
                         HashMap<String, ImageIcon> mapIconItem, HashMap<String, ImageIcon> mapIconExtra,
                         JSONObject equipmentData){
        this.equipmentData = equipmentData;
        this.mapEquipment = mapEquipment;
        this.mapIconItem = mapIconItem;
        this.mapIconExtra = mapIconExtra;
        scoreFarming = new ScoreFarming(equipmentData);
        mapFont = common.loadFont();
        setResizable(false);
        setTitle("파밍 분석");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(root);
        setSize(495, 505);
        setBackground(new Color(34, 32, 37));
        panelWindow = new JPanel();
        panelWindow.setLayout(null);
        panelWindow.setBorder(new EmptyBorder(5,5,5,5));
        panelWindow.setBackground(new Color(34, 32, 37));
        add(panelWindow);

        infoPanel = new InfoPanel();
        infoPanel.setBounds(10, 10, 250, 150);
        infoPanel.setLayout(null);
        infoPanel.setBackground(new Color(34, 32, 37));
        infoPanel.setBorder(new EmptyBorder(0,0,0,0));
        panelWindow.add(infoPanel);

        for(int i=0;i<11;i++){
            String tag = TAGS[i];
            int[] pos = POS[i];
            JButton nowBtn;
            nowBtn = new JButton();
            nowBtn.setBackground(new Color(50, 46, 52));
            nowBtn.setBounds(START_POS[0] + 34 * pos[0], START_POS[1] + 34 * pos[1], 30, 30);
            nowBtn.setBorder(new LineBorder(new Color(255, 255, 75)));
            infoPanel.add(nowBtn);
            mapToggle.put(tag, mapEquipment.get(tag));
            if(mapEquipment.get(tag) != null){
                nowBtn.setIcon(mapIconItem.get(mapEquipment.get(tag)));
                nowBtn.setToolTipText(
                        (String)((JSONObject)equipmentData.get(mapEquipment.get(tag))).get("이름")
                );
                nowBtn.addActionListener(e -> {
                    if(mapToggle.get(tag) == null){
                        nowBtn.setBorder(new LineBorder(new Color(255, 255, 75)));
                        nowBtn.setIcon(mapIconItem.get(mapEquipment.get(tag)));
                        mapToggle.put(tag, mapEquipment.get(tag));
                    }else{
                        nowBtn.setIcon(
                                common.changeBright(infoPanel, mapIconItem.get(mapEquipment.get(tag)), 0.6)
                        );
                        nowBtn.setBorder(new LineBorder(new Color(100, 100, 45)));
                        mapToggle.remove(tag);
                    }
                    setScore();
                });
            }else{
                nowBtn.setIcon(mapIconExtra.get("info"+tag));
            }
        }
        infoPanel.updateUI();

        panelScore = new JPanel();
        panelScore.setBounds(10, 165, 250, 285);
        panelScore.setBackground(new Color(50, 46, 52));
        panelScore.setBorder(new LineBorder(Color.BLACK));
        panelScore.setLayout(null);
        panelWindow.add(panelScore);

        setScore();

        panelExplain = new JPanel();
        panelExplain.setBounds(270, 10, 200, 440);
        panelExplain.setBorder(new LineBorder(Color.BLACK));
        panelExplain.setBackground(new Color(50, 46, 52));
        panelExplain.setLayout(null);
        panelWindow.add(panelExplain);
        setEquipmentDropTable();
    }

    private void setEquipmentDropTable(){
        JLabel guide = new JLabel("<드랍장소>");
        guide.setForeground(Color.WHITE);
        guide.setFont(mapFont.get("large"));
        guide.setBorder(new EmptyBorder(0,0,0,0));
        guide.setBounds(5, 3, 190, 35);
        guide.setHorizontalAlignment(JLabel.CENTER);
        panelExplain.add(guide);
        for(int i=0;i<TAGS.length;i++){
            String tag = TAGS[i];
            JLabel nowPart = new JLabel(PARTS[i]+" :");
            nowPart.setForeground(Color.WHITE);
            nowPart.setFont(mapFont.get("bold"));
            nowPart.setBorder(new EmptyBorder(0,0,0,0));
            nowPart.setBounds(15, 40+35*i, 40, 35);
            panelExplain.add(nowPart);

            String code = mapEquipment.get(tag);
            JSONArray dropArray = (JSONArray)((JSONObject)equipmentData.get(code)).get("드랍");
            StringBuilder drop = new StringBuilder();
            int dropTable = 0;
            for(Object o : dropArray){
                String now = (String) o;
                drop.append(" ").append(now);
                dropTable++;
            }
            JLabel nowDrop = new JLabel();
            if(dropTable ==2){
                nowDrop.setForeground(Color.WHITE);
            }else{
                drop.append("(고유)");
                nowDrop.setForeground(Color.PINK);
            }
            nowDrop.setText(drop.toString());
            nowDrop.setFont(mapFont.get("bold"));
            nowDrop.setBorder(new EmptyBorder(0,0,0,0));
            nowDrop.setBounds(62, 40+35*i, 150, 35);
            panelExplain.add(nowDrop);

        }
    }

    JPanel panelExplain;
    JPanel panelScore;
    private void setScore(){
        panelScore.removeAll();
        HashMap<String, Double> farmingScore = scoreFarming.calculateScore(mapToggle);
        ArrayList<Score> scoreArray = new ArrayList<>();
        try{
            for(String dungeon : farmingScore.keySet()){
                // System.out.println(dungeon + " " + farmingScore.get(dungeon).toString());
                scoreArray.add(new Score(dungeon, farmingScore.get(dungeon)));
            }
            Collections.sort(scoreArray);
            JLabel guide = new JLabel("<던전별 유효 드랍율>");
            guide.setFont(mapFont.get("large"));
            guide.setForeground(Color.WHITE);
            guide.setBounds(5, 2, 240, 40);
            guide.setHorizontalAlignment(JLabel.CENTER);
            panelScore.add(guide);
            for(int i=0;i<scoreArray.size();i++){
                JLabel nowDungeon = new JLabel(scoreArray.get(i).toString(true));
                nowDungeon.setFont(mapFont.get("bold"));
                nowDungeon.setBounds(59, 40+30*i, 70, 30);
                nowDungeon.setForeground(Color.WHITE);
                panelScore.add(nowDungeon);

                JLabel nowScore = new JLabel(scoreArray.get(i).toString(false));
                nowScore.setFont(mapFont.get("bold"));
                nowScore.setBounds(134, 40+30*i, 80, 30);
                nowScore.setForeground(Color.WHITE);
                panelScore.add(nowScore);
                if(i>=3){
                    nowDungeon.setForeground(Color.LIGHT_GRAY);
                    nowScore.setForeground(Color.LIGHT_GRAY);
                }
            }
        }catch (Exception ignored){}
        panelScore.updateUI();
    }


    public HashMap<String, String> mapToggle = new HashMap<>();
    private final String[] TAGS = {"11", "12", "13", "14", "15",
            "21", "22", "23", "31", "32", "33"};
    private final String[] PARTS = {"상의", "하의", "어깨", "벨트", "신발",
            "팔찌", "목걸", "반지", "보장", "법석", "귀걸"};
    private final int[][] POS = {{1, 0}, {0, 1}, {0, 0}, {1, 1}, {0, 2},
            {5, 0}, {6, 0}, {6, 1}, {5, 1}, {6, 2}, {5, 2}};
    private final int[] START_POS = {6, 6};

    class InfoPanel extends JPanel{
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            g.drawImage(mapIconExtra.get("bg_info").getImage(), 0, 0, new Color(50, 46, 52), this);
            g.setColor(Color.WHITE);
            g.drawString("클릭해서 파밍에 제외/포함 가능", 42, 140);
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
}
