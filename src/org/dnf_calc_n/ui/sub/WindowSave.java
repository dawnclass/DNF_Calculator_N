package org.dnf_calc_n.ui.sub;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.ui.main.PanelSelect;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;

public class WindowSave extends JFrame {

    PanelSelect panelSelect;
    Common common = new Common();
    HashMap<String, Font> mapFont;
    JPanel savePanel;

    public WindowSave(PanelSelect panelSelect){
        this.panelSelect = panelSelect;
        mapFont = common.loadFont();
        setResizable(false);
        setTitle("커스텀");
        setBounds(300, 200, 960, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        savePanel = new JPanel();
        savePanel.setLayout(null);
        savePanel.setBackground(new Color(34, 32, 37));
        savePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(savePanel);

        savePanel.updateUI();
    }

    public void startSave(){
        setVisible(true);
    }

    private void loadSaveSlotJson(){
        JSONObject saveJson = common.loadJsonObject("cache/saved.json");
        JSONArray saveSlotArray = (JSONArray) saveJson.get("array");
    }

    private void loadSaveJson(int index){
        JSONObject saveJson = common.loadJsonObject("cache/saved.json");
    }

    private void saveSaveJson(String name){

        JSONObject nowSelectJson = common.loadJsonObject("cache/selected.json");
    }

}
