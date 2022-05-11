package org.dnf_calc_n.ui.main;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.data.LoadString;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PanelResult extends JPanel {

    Common common = new Common();
    HashMap<String, Font> mapFont;
    HashMap<String, String> mapResultBuff;
    JPanel root;

    DamageGraphPanel damageGraphPanel;
    JPanel damagePanel;

    public PanelResult(JPanel root){
        this.mapFont = common.loadFont();
        this.root = root;
        this.setBackground(new Color(50, 46, 52));
        this.setBounds(470, 170, 780, 500);
        this.setLayout(null);
        root.add(this);

        damageGraphPanel = new DamageGraphPanel(this, mapFont);
        makeDamagePanel();
        makeDetailPanel();
        makeBuffPanel();
        // setDamagePanel();
    }

    Double[] damageArray, coolDownArray, coolDamageArray;
    public void setDamageArray(Double[] damageArray, Double[] coolDownArray, Double[] coolDamageArray){
        toggleCoolMode();
        this.damageArray = damageArray;
        this.coolDownArray = coolDownArray;
        this.coolDamageArray = coolDamageArray;
        damageGraphPanel.setDamageArray(damageArray, coolDownArray, coolDamageArray);
        setDamageValue(damageArray, coolDamageArray, coolDownArray);
    }

    HashMap<String, String> detailMap;
    public void setDetailMap(HashMap<String, String> detailMap){
        this.detailMap = detailMap;

        for(String key : mapDetailLabel.keySet()){
            if(detailMap.get(key) == null) continue;
            String nowText = detailMap.get(key);
            mapDetailLabel.get(key).setText(nowText);
            if(nowText.contains("-")){
                mapDetailLabel.get(key).setForeground(new Color(188, 50, 50));
            }else if(!nowText.contains("1") && !nowText.contains("2") && !nowText.contains("3") &&
                    !nowText.contains("4") && !nowText.contains("5") && !nowText.contains("6") &&
                    !nowText.contains("7") && !nowText.contains("8") && !nowText.contains("9")
            ){
                mapDetailLabel.get(key).setForeground(new Color(127, 116, 87));
            }else{
                mapDetailLabel.get(key).setForeground(new Color(74, 162, 86));
            }
        }
        detailPanel.updateUI();
    }

    int[] groupTotal = {};
    int[] group0 = {0};
    int[] group1 = {3, 4, 5, 6};
    int[] group2 = {7, 8, 12};
    int[] group3 = {9, 13, 14, 15, 17};
    int[] group4 = {11, 16, 18};
    int[][] group = {groupTotal, group0, group1, group2, group3, group4};
    private void setDamageValue(Double[] damageArray, Double[] coolDamageArray, Double[] coolDownArray){
        DecimalFormat formatter = new DecimalFormat("###,###");
        damageValueLabel.get(LoadString.strGet("종합")).setText(LoadString.strGet("추가예정"));
        damageValueLabel.get(LoadString.strGet("종합")+"2").setText(LoadString.strGet("추가예정"));
        damageValueLabel.get(LoadString.strGet("평타기숙")).setText(formatter.format(damageArray[0]*100));
        damageValueLabel.get(LoadString.strGet("평타기숙")+"2").setText("-");
        for(int i=2;i<6;i++){
            double value = 0;
            double value2 = 0;
            double value3 = 0;
            for(int j : group[i]){
                value+=damageArray[j];
                value2+=coolDamageArray[j];
                value3+=coolDownArray[j];
            }
            value = value / group[i].length;
            value2 = value2 / group[i].length;
            value3 = value3 / group[i].length;
            damageValueLabel.get(tagDamageValue[i]).setText(formatter.format(value*100));
            if(isCoolOriginal){
                damageValueLabel.get(tagDamageValue[i]+"2").setText(Math.round(value3*1000)/10.0+"%");
            }else{
                damageValueLabel.get(tagDamageValue[i]+"2").setText(formatter.format(value2*100));
            }
        }
    }

    static boolean isCoolOriginal = true;
    public void toggleCoolMode(){
        JSONObject nowJson = common.loadJsonObject("cache/selected.json");
        if(LoadString.strGet("원쿨감").equals(
                LoadString.strGet((String)nowJson.get("cool")))){
            isCoolOriginal = true;
            nowLabelWithDamage.setText(LoadString.strGet("쿨감%"));
        }else{
            isCoolOriginal = false;
            nowLabelWithDamage.setText(LoadString.strGet("쿨감보정"));
        }
    }

    static class DamageGraphPanel extends JPanel{
        PanelResult root;
        Double[] damageArray, coolDownArray, coolDamageArray;
        HashMap<String, Font> mapFont;
        public DamageGraphPanel(PanelResult root, HashMap<String, Font> mapFont){
            this.mapFont = mapFont;
            this.root = root;
            this.setBackground(Color.WHITE);
            this.setBounds(5, 5, 250, 490);
            this.setLayout(null);
            this.setBorder(new EmptyBorder(0,0,0,0));
            root.add(this);
        }

        double maxValue = 0.0;
        public void setDamageArray(Double[] damageArray, Double[] coolDownArray, Double[] coolDamageArray){
            this.damageArray = damageArray;
            this.coolDownArray = coolDownArray;
            this.coolDamageArray = coolDamageArray;
            maxValue = 0.0;
            for(double v : damageArray){
                if(v > maxValue) maxValue = v;
            }
            if(!isCoolOriginal){
                for(double v : coolDamageArray){
                    if(v > maxValue) maxValue = v;
                }
            }
            // System.out.println("maxValue = "+maxValue);
            this.repaint();
        }

        int[] index = {1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 48, 50, 60, 70, 75, 80, 85, 95, 100};
        @Override
        public void paintComponent(Graphics g){
            DecimalFormat formatter = new DecimalFormat("###,###");
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 250, 490);
            g.setColor(Color.GRAY);
            g.drawLine(30, 5, 30, 485);
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(120, 20, 120, 485);
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(210, 5, 210, 485);
            for(int i=0;i<index.length;i++){
                g.setColor(Color.BLACK);
                g.setFont(mapFont.get("normal_bold"));
                g.drawString(String.valueOf(index[i]), 5, 27+25*i);
                try{
                    g.setColor(Color.RED);
                    int x = (int) ((damageArray[i] / maxValue) * 200);
                    g.fillRect(30, 22+25*i, x, 5);
                    g.setFont(mapFont.get("more_small"));
                    g.drawString(formatter.format(damageArray[i]*100), x, 20+25*i);
                    if(!isCoolOriginal){
                        int x2 = (int) ((coolDamageArray[i] / maxValue) * 200)-x;
                        if(x2 >= 0){
                            g.setColor(Color.BLUE);
                        }else{
                            g.setColor(new Color(144, 0, 255));
                        }
                        g.fillRect(30+x, 22+25*i, x2, 5);
                        if(!damageArray[i].equals(coolDamageArray[i])){
                            g.drawString(formatter.format(coolDamageArray[i]*100), x+x2, 35+25*i);
                        }
                    }else{
                        int x3 = (int) (coolDownArray[i] * 90);
                        if(x3 < -90) x3= -90;
                        g.setColor(Color.BLUE);
                        g.fillRoundRect(117+x3, 21+25*i, 6, 6, 6, 6);
                        g.drawString(Math.round(coolDownArray[i]*1000)/10.0+"%", 90+x3, 36+25*i);
                    }

                }catch (Exception ignored){}
            }
            g.setColor(new Color(0, 0, 0));
            g.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            g.drawString(LoadString.strGet("←증가  쿨타임  감소→"), 55, 14);
        }
    }

    String[] tagLayout = {
            "피해증가", "스킬공격력", "화속성강화", "수속성강화", "명속성강화", "암속성강화",
            "화속성저항", "수속성저항", "명속성저항", "암속성저항", "공격속도", "캐스팅속도",
            "이동속도", "크리티컬", "출혈전환", "출혈뎀증", "중독전환", "중독뎀증",
            "화상전환", "화상뎀증", "감전전환", "감전뎀증",

            "마나소모량", "물리방어력", "마법방어력", "회피율", "적중률", "HP회복량",
            "MP회복량", "HP MAX", "MP MAX", "출혈내성", "중독내성", "화상내성",
            "감전내성", "빙결내성", "둔화내성", "기절내성", "저주내성", "암흑내성",
            "석화내성", "수면내성", "혼란내성", "구속내성"
    };
    HashMap<String, JLabel> mapDetailLabel = new HashMap<>();
    JPanel detailPanel;
    private void makeDetailPanel(){
        detailPanel = new JPanel();
        detailPanel.setBackground(new Color(34, 32, 37));
        detailPanel.setBorder(new LineBorder(Color.DARK_GRAY));
        detailPanel.setBounds(450, 5, 320, 490);
        detailPanel.setLayout(null);
        this.add(detailPanel);
        JLabel label = new JLabel(LoadString.strGet("<세부 장비 옵션>"));
        label.setFont(mapFont.get("bold"));
        label.setForeground(Color.WHITE);
        label.setBounds(0, 0, 320, 30);
        label.setHorizontalAlignment(JLabel.CENTER);
        detailPanel.add(label);
        JPanel guide = new JPanel();
        guide.setBackground(Color.DARK_GRAY);
        guide.setBounds(0, 30, 320, 2);
        detailPanel.add(guide);
        JPanel guide2 = new JPanel();
        guide2.setBackground(Color.DARK_GRAY);
        guide2.setBounds(160, 30, 1, 460);
        detailPanel.add(guide2);

        for(int i=0;i<tagLayout.length;i++){
            int[] position = {7, 35+i*20};
            if(i > 21){
                position[0] = 165;
                position[1] -= 22*20;
            }
            JLabel nowLabel = new JLabel(LoadString.strGet(tagLayout[i]));
            nowLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            nowLabel.setForeground(new Color(127, 116, 87));
            nowLabel.setBounds(position[0], position[1], 70, 20);
            nowLabel.setHorizontalAlignment(JLabel.LEFT);
            detailPanel.add(nowLabel);

            JLabel nowLabel2 = new JLabel();
            nowLabel2.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            nowLabel2.setForeground(new Color(74, 162, 86));
            nowLabel2.setBounds(position[0]+75, position[1], 70, 20);
            nowLabel2.setHorizontalAlignment(JLabel.RIGHT);
            detailPanel.add(nowLabel2);
            mapDetailLabel.put(tagLayout[i], nowLabel2);
        }
    }

    JLabel nowLabelWithDamage;
    HashMap<String, JLabel> damageValueLabel = new HashMap<>();
    String[] tagDamageValue = {
            LoadString.strGet("종합"), LoadString.strGet("평타기숙"),
            LoadString.strGet("기본스킬"), LoadString.strGet("하급스킬"),
            LoadString.strGet("상급스킬"), LoadString.strGet("각성스킬")
    };
   private void makeDamagePanel(){
        damagePanel = new JPanel();
        damagePanel.setBackground(new Color(34, 32, 37));
        damagePanel.setBorder(new LineBorder(Color.DARK_GRAY));
        damagePanel.setBounds(260, 5, 185, 305);
        damagePanel.setLayout(null);
        this.add(damagePanel);
        JLabel nowLabelDamage = new JLabel(LoadString.strGet("<장비 데미지%>"));
        nowLabelDamage.setFont(mapFont.get("bold"));
        nowLabelDamage.setBackground(new Color(34, 32, 37));
        nowLabelDamage.setForeground(Color.WHITE);
        nowLabelDamage.setBounds(0, 0, 185, 30);
        nowLabelDamage.setHorizontalAlignment(JLabel.CENTER);
        damagePanel.add(nowLabelDamage);
        JLabel nowLabelOnlyDamage = new JLabel(LoadString.strGet("순데미지%"));
        nowLabelOnlyDamage.setFont(mapFont.get("small_bold"));
        nowLabelOnlyDamage.setBackground(new Color(34, 32, 37));
        nowLabelOnlyDamage.setForeground(new Color(255, 132, 132));
        nowLabelOnlyDamage.setBounds(50, 30, 67, 25);
        nowLabelOnlyDamage.setHorizontalAlignment(JLabel.CENTER);
        damagePanel.add(nowLabelOnlyDamage);
        String coolText;
        if(isCoolOriginal){
            coolText = LoadString.strGet("쿨감%");
        }else{
            coolText = LoadString.strGet("쿨감보정");
        }
        nowLabelWithDamage = new JLabel(coolText);
        nowLabelWithDamage.setFont(mapFont.get("small_bold"));
        nowLabelWithDamage.setBackground(new Color(34, 32, 37));
        nowLabelWithDamage.setForeground(new Color(132, 155, 255));
        nowLabelWithDamage.setBounds(118, 30, 67, 25);
        nowLabelWithDamage.setHorizontalAlignment(JLabel.CENTER);
        damagePanel.add(nowLabelWithDamage);

        for(int i=0;i<6;i++){
            Font font;
            int height;
            if(i==0){
                font = mapFont.get("bold");
                height = 30;
            }else{
                font = mapFont.get("small_bold");
                height = 25;
            }
            JLabel nowLabel = new JLabel(tagDamageValue[i]);
            nowLabel.setFont(font);
            nowLabel.setForeground(Color.WHITE);
            nowLabel.setBounds(0, 55+25*i+(30-height), 50, height);
            nowLabel.setHorizontalAlignment(JLabel.CENTER);
            nowLabel.setVerticalAlignment(JLabel.CENTER);
            damagePanel.add(nowLabel);
            JLabel nowValueLabel = new JLabel("");
            nowValueLabel.setForeground(new Color(255, 132, 132));
            nowValueLabel.setBounds(51, 55+25*i+(30-height), 67, height);
            nowValueLabel.setHorizontalAlignment(JLabel.CENTER);
            damagePanel.add(nowValueLabel);
            damageValueLabel.put(tagDamageValue[i], nowValueLabel);
            JLabel nowValueLabel2 = new JLabel("");
            nowValueLabel2.setForeground(new Color(132, 155, 255));
            nowValueLabel2.setBounds(118, 55+25*i+(30-height), 67, height);
            nowValueLabel2.setHorizontalAlignment(JLabel.CENTER);
            damagePanel.add(nowValueLabel2);
            damageValueLabel.put(tagDamageValue[i]+"2", nowValueLabel2);
            JPanel guide = new JPanel();
            guide.setBackground(Color.DARK_GRAY);
            guide.setBounds(0, 55+25*i+(30-height), 185, 1);
            damagePanel.add(guide);
        }
        JPanel guide4 = new JPanel();
        guide4.setBackground(Color.DARK_GRAY);
        guide4.setBounds(0, 60+25*6, 185, 1);
        damagePanel.add(guide4);
        JPanel guide3 = new JPanel();
        guide3.setBackground(Color.DARK_GRAY);
        guide3.setBounds(0, 30, 185, 2);
        damagePanel.add(guide3);
        JPanel guide = new JPanel();
        guide.setBackground(Color.DARK_GRAY);
        guide.setBounds(50, 30, 2, 400);
        damagePanel.add(guide);
        JPanel guide2 = new JPanel();
        guide2.setBackground(Color.DARK_GRAY);
        guide2.setBounds(117, 30, 1, 400);
        damagePanel.add(guide2);
    }

    JPanel buffPanel;
    HashMap<String, JLabel> buffValueLabel = new HashMap<>();
    private void makeBuffPanel(){
        buffPanel = new JPanel();
        buffPanel.setBackground(new Color(34, 32, 37));
        buffPanel.setBorder(new LineBorder(Color.DARK_GRAY));
        buffPanel.setBounds(260, 315, 185, 180);
        buffPanel.setLayout(null);
        this.add(buffPanel);
        JLabel nowLabelBuff = new JLabel(LoadString.strGet("<버프점수 계산>"));
        nowLabelBuff.setFont(mapFont.get("bold"));
        nowLabelBuff.setBackground(new Color(34, 32, 37));
        nowLabelBuff.setForeground(Color.WHITE);
        nowLabelBuff.setBounds(0, 0, 185, 30);
        nowLabelBuff.setHorizontalAlignment(JLabel.CENTER);
        buffPanel.add(nowLabelBuff);
        String[] tag = {
                LoadString.strGet("총버프력"),LoadString.strGet("축스탯"),
                LoadString.strGet("축공격력"),
                LoadString.strGet("각성스탯"), LoadString.strGet("버프점수")
        };
        for(int i=0;i<5;i++){
            JLabel nowLabel = new JLabel(tag[i]);
            nowLabel.setFont(mapFont.get("bold"));
            nowLabel.setBackground(new Color(34, 32, 37));
            nowLabel.setForeground(Color.WHITE);
            nowLabel.setBounds(0, 30+30*i, 80, 30);
            nowLabel.setHorizontalAlignment(JLabel.CENTER);
            buffPanel.add(nowLabel);
            JPanel guide = new JPanel();
            guide.setBackground(Color.DARK_GRAY);
            guide.setBounds(0, 30+30*i, 185, 2);
            buffPanel.add(guide);
        }
        JPanel guide = new JPanel();
        guide.setBackground(Color.DARK_GRAY);
        guide.setBounds(80, 30, 2, 400);
        buffPanel.add(guide);

        JLabel buffTotal = new JLabel("");
        buffTotal.setBounds(80, 30, 105, 30);
        JLabel buffBlessStat = new JLabel("");
        buffBlessStat.setBounds(80, 60, 105, 30);
        JLabel buffBlessAtk = new JLabel("");
        buffBlessAtk.setBounds(80, 90, 105, 30);
        JLabel buffCruxStat = new JLabel("");
        buffCruxStat.setBounds(80, 120, 105, 30);
        JLabel buffScore = new JLabel("");
        buffScore.setBounds(80, 150, 105, 30);

        buffValueLabel.put("total", buffTotal);
        buffValueLabel.put("blessStat", buffBlessStat);
        buffValueLabel.put("blessAtk", buffBlessAtk);
        buffValueLabel.put("cruxStat", buffCruxStat);
        buffValueLabel.put("score", buffScore);

        buffValueLabel.forEach((k, v) ->{
            v.setForeground(Color.WHITE);
            v.setHorizontalAlignment(JLabel.CENTER);
            v.setFont(mapFont.get("bold"));
            buffPanel.add(v);
                });

        buffPanel.updateUI();
        this.updateUI();

    }

    public void resetBuffValue(){
        buffValueLabel.forEach((k, v) ->{
            v.setText("");
        });
        this.updateUI();
    }

    public void setBuffResult(HashMap<String, String> mapResultBuff){
        this.mapResultBuff = mapResultBuff;
        for(String key : mapResultBuff.keySet()){
            JLabel nowLabel = buffValueLabel.get(key);
            if(nowLabel == null) continue;
            nowLabel.setText(mapResultBuff.get(key));
        }
        this.updateUI();
    }

}
