package org.dnf_calc_n.ui.sub;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.ui.main.PanelCustom;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.HashMap;

public class WindowCustom extends JFrame {

    PanelCustom panelCustom;
    JPanel customPanel;
    Common common = new Common();
    HashMap<String, Font> mapFont;

    public WindowCustom(PanelCustom panelCustom, JPanel root){
        this.panelCustom = panelCustom;
        mapFont = common.loadFont();
        setResizable(false);
        setTitle("커스텀");
        setBounds(200, 200, 960, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(root);
        customPanel = new JPanel();
        customPanel.setLayout(null);
        customPanel.setBackground(new Color(45, 46, 52));
        customPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(customPanel);

        makeCustomWidget();
        customPanel.updateUI();
    }


    public void startCustom(){
        setVisible(true);
    }

    private void makeCustomWidget(){
        JLabel labelDealer = new JLabel("<딜러>");
        labelDealer.setFont(mapFont.get("huge"));
        labelDealer.setForeground(Color.WHITE);
        labelDealer.setBounds(65, 5, 250, 60);
        labelDealer.setHorizontalAlignment(JLabel.CENTER);
        customPanel.add(labelDealer);

        JLabel labelBuffer = new JLabel("<버퍼>");
        labelBuffer.setFont(mapFont.get("huge"));
        labelBuffer.setForeground(Color.WHITE);
        labelBuffer.setBounds(515, 5, 250, 60);
        labelBuffer.setHorizontalAlignment(JLabel.CENTER);
        customPanel.add(labelBuffer);

        JSONObject cacheJson = common.loadJsonObject("cache/selected.json");
        JSONObject widgetJson = common.loadJsonObject("resources/ui_layout/custom.json");

        JSONArray widgetField = (JSONArray) widgetJson.get("textField");
        JSONArray widgetCombo = (JSONArray) widgetJson.get("comboBox");

        for(Object json : widgetField){
            JSONObject jsonField = (JSONObject) json;
            String tag = (String) jsonField.get("tag");

            JTextField nowField = new JTextField();
            nowField.setFont(mapFont.get("normal"));

            JSONArray position = (JSONArray) jsonField.get("position");
            int posX = 20+200*((Number)position.get(0)).intValue();
            if(posX > 300) posX += 50;
            int posY = 70+30*((Number)position.get(1)).intValue();
            nowField.setBounds(posX+80, posY,90, 25);
            String firstValue = "0";
            try{
                firstValue = (String)cacheJson.get(tag);
            }catch (NullPointerException ignored){}
            nowField.setText(firstValue);
            nowField.getDocument().addDocumentListener(
                    new DocumentListener() {
                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            common.saveCacheData("selected", tag, nowField.getText());
                            panelCustom.calculationPackage();
                        }
                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            common.saveCacheData("selected", tag, nowField.getText());
                            panelCustom.calculationPackage();
                        }
                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            common.saveCacheData("selected", tag, nowField.getText());
                            panelCustom.calculationPackage();
                        }
                    }
            );

            JLabel nowLabel = new JLabel();
            nowLabel.setText((String) jsonField.get("text")+" :");
            nowLabel.setFont(mapFont.get("normal_bold"));
            nowLabel.setForeground(Color.WHITE);
            nowLabel.setBorder(new EmptyBorder(0,0,0,0));
            nowLabel.setBounds(posX, posY,70, 25);
            customPanel.add(nowLabel);
            customPanel.add(nowField);
        }

        for(Object json : widgetCombo){
            JSONObject jsonCombo = (JSONObject) json;
            String tag = (String) jsonCombo.get("tag");

            JSONArray nowValues = (JSONArray)jsonCombo.get("values");
            String[] values = new String[nowValues.size()];
            for(int i=0;i<nowValues.size();i++) values[i] = (String) nowValues.get(i);

            JComboBox<String> nowCombo = new JComboBox<>(values);
            nowCombo.setFont(mapFont.get("normal"));

            JSONArray position = (JSONArray) jsonCombo.get("position");
            int posX = 20+200*((Number)position.get(0)).intValue();
            if(posX > 300) posX += 50;
            int posY = 70+30*((Number)position.get(1)).intValue();
            nowCombo.setBounds(posX+80, posY,90, 25);
            String firstValue = values[0];
            try{
                firstValue = (String)cacheJson.get(tag);
            }catch (NullPointerException ignored){}
            nowCombo.setSelectedItem(firstValue);
            nowCombo.addItemListener(e -> {
                if(e.getStateChange() == ItemEvent.SELECTED){
                    common.saveCacheData("selected", tag, (String) nowCombo.getSelectedItem());
                    panelCustom.calculationPackage();
                }
            });

            JLabel nowLabel = new JLabel();
            nowLabel.setText((String) jsonCombo.get("text")+" :");
            nowLabel.setFont(mapFont.get("normal_bold"));
            nowLabel.setForeground(Color.WHITE);
            nowLabel.setBorder(new EmptyBorder(0,0,0,0));
            nowLabel.setBounds(posX, posY,70, 25);
            customPanel.add(nowLabel);
            customPanel.add(nowCombo);
        }
    }




}
