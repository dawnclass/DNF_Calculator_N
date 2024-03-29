package org.dnf_calc_n.ui.main;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.calculate.Buff;
import org.dnf_calc_n.calculate.Damage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class PanelCondition extends JPanel {

    Common common = new Common();
    HashMap<String, Font> mapFont;
    JPanel root;
    Color bgColor = new Color(50, 46, 52);
    Damage damage;
    Buff buff;
    PanelResult panelResult;

    public PanelCondition(JPanel root, Damage damage, Buff buff, PanelResult panelResult){
        this.panelResult =panelResult;
        this.buff = buff;
        this.damage = damage;
        this.mapFont = common.loadFont();
        this.root = root;
        this.setBackground(bgColor);
        this.setBounds(470, 10, 675, 150);
        this.setLayout(null);
        root.add(this);
    }

    ArrayList<String> listToggle = new ArrayList<>();
    ArrayList<String> listGauge = new ArrayList<>();

    int[][] grid = {
            {0, 0},{0, 1},{0, 2},{0, 3},{0, 4},{0, 5},
            {1, 0},{1, 1},{1, 2},{1, 3},{1, 4},{1, 5},
            {2, 0},{2, 1},{2, 2},{2, 3},{2, 4},{2, 5}
    };
    ArrayList<String> conditionTag = new ArrayList<>();
    HashMap<String, JLabel> labelConditions = new HashMap<>();
    HashMap<String, JCheckBox> widgetToggle = new HashMap<>();
    HashMap<String, JComboBox<String>> widgetGauge = new HashMap<>();
    HashMap<String, String> mapSelectCondition = new HashMap<>();

    public void setConditions(JSONObject conditionJson){
        resetConditionPanel();
        System.out.println(conditionJson.toJSONString());
        Set keySet = conditionJson.keySet();
        ArrayList<String> conditionTagCopy = (ArrayList<String>) conditionTag.clone();
        int minusIndex = 0;
        for(int i=0;i<conditionTagCopy.size();i++){  //순서 지키기용
            if(!keySet.contains(conditionTagCopy.get(i))){
                conditionTag.remove(i-minusIndex);
                minusIndex++;
            }
        }
        for(Object k : conditionJson.keySet()){
            int index;
            if(conditionTag.contains(k)){
                index = conditionTag.indexOf(k);
            }else{
                index = conditionTag.size();
                conditionTag.add((String)k);
            }
            int[] nowGrid = grid[index];
            String key = (String) k;
            JLabel nowLabel = new JLabel(key);
            nowLabel.setForeground(Color.WHITE);
            nowLabel.setFont(mapFont.get("normal_bold"));
            nowLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
            nowLabel.setBounds(5+nowGrid[0]*225, nowGrid[1]*25, 140, 25);
            this.add(nowLabel);
            labelConditions.put(key, nowLabel);
            if("tg".equals(conditionJson.get(key))){
                // 토글형
                listToggle.add(key);
                JCheckBox nowCheck = new JCheckBox();
                nowCheck.setBackground(bgColor);
                nowCheck.setBounds(145+nowGrid[0]*225, 6+nowGrid[1]*24, 30, 20);
                if(mapSelectCondition.get(key) == null){
                    mapSelectCondition.put(key, "true");
                    nowCheck.setSelected(true);
                }else if("true".equals(mapSelectCondition.get(key))){
                    nowCheck.setSelected(true);
                }else if("false".equals(mapSelectCondition.get(key))){
                    nowCheck.setSelected(false);
                }

                nowCheck.addItemListener(e -> {
                    if(nowCheck.isSelected()){
                        mapSelectCondition.put(key, "true");
                    }else{
                        mapSelectCondition.put(key, "false");
                    }
                    damage.applyCondition(mapSelectCondition);
                    buff.setLevelingArray(damage.getArrayLeveling());
                    panelResult.resetBuffValue();
                    if(buff.getIsBuff()){
                        System.out.println("버퍼 계산 시작");
                        buff.calculateBuff();
                        HashMap<String, String> mapResultBuff = buff.getMapResult();
                        panelResult.setBuffResult(mapResultBuff);
                        double buffAdditionalStat = buff.getAdditionalDealerStat();
                        damage.setAdditionalStat(buffAdditionalStat);
                    }
                    panelResult.setDamageArray(
                            damage.getArrayTotalLevelDamage(),
                            damage.getArrayTotalCoolDown(),
                            damage.getArrayTotalLevelDamageWithCool()
                    );
                    panelResult.setDetailMap(damage.getDetailMap());
                });
                this.add(nowCheck);
                widgetToggle.put(key, nowCheck);
            }else{
                // 게이지 형
                listGauge.add(key);
                JSONArray nowJson = (JSONArray) conditionJson.get(key);
                String[] nowArray = new String[nowJson.size()];
                for(int i=0;i<nowJson.size();i++){
                    nowArray[i] = (String) nowJson.get(i);
                }

                JComboBox<String> nowCombo = new JComboBox<>(nowArray);
                nowCombo.setFont(mapFont.get("normal"));
                nowCombo.setBounds(145+nowGrid[0]*225, 5+nowGrid[1]*24, 65, 20);
                if(mapSelectCondition.get(key) == null){
                    mapSelectCondition.putIfAbsent(key, nowArray[nowArray.length-1]);
                    nowCombo.setSelectedItem(nowArray[nowArray.length-1]);
                }else{
                    nowCombo.setSelectedItem(mapSelectCondition.get(key));
                }
                nowCombo.addItemListener(e -> {
                    if(e.getStateChange() == ItemEvent.SELECTED){
                        mapSelectCondition.put(key, (String) nowCombo.getSelectedItem());
                        damage.applyCondition(mapSelectCondition);
                        buff.setLevelingArray(damage.getArrayLeveling());
                        panelResult.resetBuffValue();
                        if(buff.getIsBuff()){
                            System.out.println("버퍼 계산 시작");
                            buff.calculateBuff();
                            HashMap<String, String> mapResultBuff = buff.getMapResult();
                            panelResult.setBuffResult(mapResultBuff);
                            double buffAdditionalStat = buff.getAdditionalDealerStat();
                            damage.setAdditionalStat(buffAdditionalStat);
                        }
                        panelResult.setDamageArray(
                                damage.getArrayTotalLevelDamage(),
                                damage.getArrayTotalCoolDown(),
                                damage.getArrayTotalLevelDamageWithCool()
                        );
                        panelResult.setDetailMap(damage.getDetailMap());
                    }
                });
                this.add(nowCombo);
                widgetGauge.put(key, nowCombo);
            }
        }
        this.updateUI();
    }

    private void resetConditionPanel(){
        listToggle.clear();
        listGauge.clear();
        labelConditions.clear();
        widgetToggle.clear();
        widgetGauge.clear();
        this.removeAll();

    }

    public HashMap<String, String> getMapSelectCondition(){
        return mapSelectCondition;
    }

}
