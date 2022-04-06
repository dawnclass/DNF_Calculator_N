package org.dnf_calc_n.ui.sub;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.ui.main.PanelInfo;
import org.dnf_calc_n.ui.main.PanelSelect;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class WindowSave extends JFrame {

    PanelSelect panelSelect;
    Common common = new Common();
    HashMap<String, Font> mapFont;
    JPanel savePanel;
    HashMap<String, JComboBox<String>> mapWidgetCombo;
    PanelInfo panelInfo;

    public WindowSave(
            PanelSelect panelSelect, PanelInfo panelInfo,
            HashMap<String, JComboBox<String>> mapWidgetCombo
    ){
        this.panelInfo = panelInfo;
        this.mapWidgetCombo = mapWidgetCombo;
        this.panelSelect = panelSelect;
        mapFont = common.loadFont();
        setResizable(false);
        setTitle("세이브 로드");
        setBounds(300, 200, 220, 210);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        savePanel = new JPanel();
        savePanel.setLayout(null);
        savePanel.setBackground(new Color(45, 46, 52));
        savePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(savePanel);

        makeSaveSlot();
        loadSaveSlotJson();
        savePanel.updateUI();
    }

    public void startSave(){
        setVisible(true);
    }

    JComboBox<String> saveCombo;
    private void makeSaveSlot(){
        JLabel saveLabel = new JLabel("저장슬롯:");
        saveLabel.setFont(mapFont.get("bold"));
        saveLabel.setForeground(Color.WHITE);
        saveLabel.setBounds(15, 5, 70, 30);
        savePanel.add(saveLabel);

        saveCombo = new JComboBox<>();
        saveCombo.setFont(mapFont.get("normal"));
        saveCombo.setBounds(90, 9, 103, 25);
        savePanel.add(saveCombo);

        JButton loadButton = new JButton("로드");
        loadButton.setBackground(new Color(157, 157, 255));
        loadButton.setFont(mapFont.get("bold"));
        loadButton.setBounds(5, 40, 90, 30);
        loadButton.addActionListener(e -> {
            try{
                loadSaveJson((String)saveCombo.getSelectedItem());
            }catch (Exception d){}
        });
        savePanel.add(loadButton);

        JButton deleteButton = new JButton("삭제");
        deleteButton.setFont(mapFont.get("bold"));
        deleteButton.setBounds(105, 40, 90, 30);
        deleteButton.setBackground(new Color(255, 133, 133));
        deleteButton.addActionListener(e -> {
            try{
                deleteSaveJson((String)saveCombo.getSelectedItem());
            }catch (Exception d){}
        });
        savePanel.add(deleteButton);

        JLabel saveLabel2 = new JLabel("슬롯이름:");
        saveLabel2.setFont(mapFont.get("bold"));
        saveLabel2.setForeground(Color.WHITE);
        saveLabel2.setBounds(15, 95, 70, 30);
        savePanel.add(saveLabel2);
        JTextField saveField = new JTextField();
        saveField.setBounds(90, 100, 103, 25);
        savePanel.add(saveField);

        JButton saveButton = new JButton("저장");
        saveButton.setFont(mapFont.get("bold"));
        saveButton.setBounds(90, 130, 105, 30);
        saveButton.addActionListener(e -> {
            try{
                saveSaveJson(saveField.getText());
                loadSaveSlotJson();
                saveField.setText("");
            }catch (Exception d){}
        });
        savePanel.add(saveButton);
    }

    private void loadSaveSlotJson(){
        JSONObject saveJson = common.loadJsonObject("cache/saved.json");
        JSONArray saveSlotArray = (JSONArray) saveJson.get("array");

        saveCombo.removeAllItems();
        for(Object o : saveSlotArray){
            saveCombo.addItem((String) o);
        }
    }

    private void loadSaveJson(String key){
        String[] answers = {"로드", "취소"};
        JLabel alertLabel = new JLabel("정말로 저장된 값을 불러오겠습니까?");
        alertLabel.setFont(mapFont.get("bold"));
        int ans = JOptionPane.showOptionDialog(
                this, alertLabel, "로드",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, answers, answers[0]
        );
        if(ans != 0){
            return;
        }
        JSONObject saveJson = common.loadJsonObject("cache/saved.json");
        JSONObject saveValueJson = (JSONObject) saveJson.get("values");
        try{
            JSONObject nowSaveJson = (JSONObject) saveValueJson.get(key);
            BufferedWriter writer = new BufferedWriter(new FileWriter("cache/selected.json"));
            writer.write(nowSaveJson.toJSONString());
            writer.flush();
            writer.close();

            loadTotalValues();
        }catch (IOException | NullPointerException e){
            JLabel failedLabel = new JLabel("에러 발생");
            failedLabel.setFont(mapFont.get("bold"));
            JOptionPane.showMessageDialog(
                    this, failedLabel, "실패",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        JLabel completeLabel = new JLabel("불러오기 완료");
        completeLabel.setFont(mapFont.get("bold"));
        JOptionPane.showMessageDialog(
                this, completeLabel, "성공",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void saveSaveJson(String name){
        if("".equals(name)) return;
        JSONObject nowSelectJson = common.loadJsonObject("cache/selected.json");
        JSONObject saveJson = common.loadJsonObject("cache/saved.json");
        JSONArray saveArrayJson = (JSONArray) saveJson.get("array");
        if(!saveArrayJson.contains(name)){
            saveArrayJson.add(name);
        }else{
            String[] answers = {"네", "아니오"};
            JLabel alertLabel = new JLabel("중복된 이름이 존재합니다. 덮어씌우겠습니까?");
            alertLabel.setFont(mapFont.get("bold"));
            int ans = JOptionPane.showOptionDialog(
                    this, alertLabel, "세이브 이름 중복",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null, answers, answers[0]
            );
            if(ans != 0){
                return;
            }
        }

        JSONObject saveValueJson = (JSONObject) saveJson.get("values");
        saveValueJson.put(name, nowSelectJson);

        saveJson.replace("array", saveArrayJson);
        saveJson.replace("values", saveValueJson);
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter("cache/saved.json"));
            writer.write(saveJson.toJSONString());
            writer.flush();
            writer.close();
        }catch (IOException | NullPointerException e){
            JLabel failedLabel = new JLabel("에러 발생");
            failedLabel.setFont(mapFont.get("bold"));
            JOptionPane.showMessageDialog(
                    this, failedLabel, "실패",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        loadSaveSlotJson();
        JLabel completeLabel = new JLabel("저장 완료");
        completeLabel.setFont(mapFont.get("bold"));
        JOptionPane.showMessageDialog(
                this, completeLabel, "성공",
                JOptionPane.INFORMATION_MESSAGE
        );

    }

    private void deleteSaveJson(String key){
        String[] answers = {"삭제", "취소"};
        JLabel alertLabel = new JLabel("정말로 저장된 값을 삭제하겠습니까?");
        alertLabel.setFont(mapFont.get("bold"));
        int ans = JOptionPane.showOptionDialog(
                this, alertLabel, "삭제",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, answers, answers[0]
        );
        if(ans != 0){
            return;
        }
        JSONObject saveJson = common.loadJsonObject("cache/saved.json");
        JSONArray saveArrayJson = (JSONArray) saveJson.get("array");
        saveArrayJson.remove(key);
        JSONObject saveValueJson = (JSONObject) saveJson.get("values");
        saveValueJson.remove(key);

        saveJson.replace("array", saveArrayJson);
        saveJson.replace("values", saveValueJson);
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter("cache/saved.json"));
            writer.write(saveJson.toJSONString());
            writer.flush();
            writer.close();
        }catch (IOException e){
            JLabel failedLabel = new JLabel("에러 발생");
            failedLabel.setFont(mapFont.get("bold"));
            JOptionPane.showMessageDialog(
                    this, failedLabel, "실패",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        loadSaveSlotJson();
        JLabel completeLabel = new JLabel("삭제 완료");
        completeLabel.setFont(mapFont.get("bold"));
        JOptionPane.showMessageDialog(
                this, completeLabel, "성공",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void loadTotalValues(){
        JSONObject nowSelectJson = common.loadJsonObject("cache/selected.json");
        Set<String> keys = mapWidgetCombo.keySet();

        JSONArray equipmentList = (JSONArray) nowSelectJson.get("equipments");
        panelInfo.resetInfoPanel();
        for(Object o : equipmentList){
            panelInfo.setEquipment((String) o);
        }
        panelInfo.updateInfo();

        mapWidgetCombo.get("jobType").setSelectedItem(nowSelectJson.get("jobType"));
        mapWidgetCombo.get("job").setSelectedItem(nowSelectJson.get("job"));
        for(String key : keys){
            if(key.equals("job") || key.equals("jobType")) continue;
            if(nowSelectJson.get(key) == null) continue;
            mapWidgetCombo.get(key).setSelectedItem((String) nowSelectJson.get(key));
        }
        panelSelect.calculationPackage();
    }

}
