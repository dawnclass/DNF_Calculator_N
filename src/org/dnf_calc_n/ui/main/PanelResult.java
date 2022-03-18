package org.dnf_calc_n.ui.main;

import org.dnf_calc_n.Common;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class PanelResult extends JPanel {

    Common common = new Common();
    HashMap<String, Font> mapFont;
    HashMap<String, String> mapResultBuff;
    JPanel root;

    public PanelResult(JPanel root){
        this.mapFont = common.loadFont();
        this.root = root;
        this.setBackground(new Color(50, 46, 52));
        this.setBounds(470, 170, 450, 500);
        this.setLayout(null);
        root.add(this);
    }

    public void setBuffResult(HashMap<String, String> mapResultBuff){
        this.removeAll();
        this.mapResultBuff = mapResultBuff;
        int i = 0;
        for(String key : mapResultBuff.keySet()){
            JLabel nowLabel = new JLabel(key+" : "+mapResultBuff.get(key));
            nowLabel.setFont(mapFont.get("bold"));
            nowLabel.setForeground(Color.WHITE);
            nowLabel.setBounds(10, 10+i*30, 100, 20);
            this.add(nowLabel);
            i++;
        }
        this.updateUI();
    }

}
