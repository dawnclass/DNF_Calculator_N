package org.dnf_calc_n.ui.main;

import org.dnf_calc_n.Common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
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
        this.setBounds(470, 170, 450, 500);
        this.setLayout(null);
        root.add(this);

        damageGraphPanel = new DamageGraphPanel(this, mapFont);
        makeBuffPanel();
        // setDamagePanel();
    }

    public void setDamageArray(Double[] damageArray, Double[] coolDownArray, Double[] coolDamageArray){
        damageGraphPanel.setDamageArray(damageArray, coolDownArray, coolDamageArray);
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
            for(double v : coolDamageArray){
                if(v > maxValue) maxValue = v;
            }
            System.out.println("maxValue = "+maxValue);
            this.repaint();
        }

        int[] index = {1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 48, 50, 60, 70, 75, 80, 85, 95, 100};
        @Override
        public void paintComponent(Graphics g){
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 250, 490);
            g.setColor(Color.GRAY);
            g.drawLine(30, 5, 30, 485);
            g.drawLine(120, 5, 120, 485);
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
                    g.drawString((int)(damageArray[i]*100)+"%", x, 20+25*i);
                    g.setColor(Color.BLUE);
                    int x2 = (int) ((coolDamageArray[i] / maxValue) * 200)-x;
                    g.fillRect(30+x, 22+25*i, x2, 5);
                    if(!damageArray[i].equals(coolDamageArray[i])){
                        g.drawString((int)(coolDamageArray[i]*100)+"%", x+x2, 35+25*i);
                    }
                }catch (Exception ignored){}
            }
        }
    }

    JPanel buffPanel;
    HashMap<String, JLabel> buffValueLabel = new HashMap<>();
    private void makeBuffPanel(){
        buffPanel = new JPanel();
        buffPanel.setBackground(new Color(34, 32, 37));
        buffPanel.setBorder(new EmptyBorder(0,0,0,0));
        buffPanel.setBounds(260, 200, 185, 150);
        buffPanel.setLayout(null);
        this.add(buffPanel);

        String[] tag = {"총버프력","축스탯", "축공격력", "각성스탯", "버프점수"};
        for(int i=0;i<5;i++){
            JLabel nowLabel = new JLabel(tag[i]);
            nowLabel.setFont(mapFont.get("bold"));
            nowLabel.setBackground(new Color(34, 32, 37));
            nowLabel.setForeground(Color.WHITE);
            nowLabel.setBounds(0, 30*i, 80, 30);
            nowLabel.setHorizontalAlignment(JLabel.CENTER);
            buffPanel.add(nowLabel);
            if(i==4) break;
            JPanel guide = new JPanel();
            guide.setBackground(Color.DARK_GRAY);
            guide.setBounds(0, 30+30*i, 185, 2);
            buffPanel.add(guide);
        }
        JPanel guide = new JPanel();
        guide.setBackground(Color.DARK_GRAY);
        guide.setBounds(80, 0, 2, 400);
        buffPanel.add(guide);

        JLabel buffTotal = new JLabel("");
        buffTotal.setBounds(80, 0, 105, 30);
        JLabel buffBlessStat = new JLabel("");
        buffBlessStat.setBounds(80, 30, 105, 30);
        JLabel buffBlessAtk = new JLabel("");
        buffBlessAtk.setBounds(80, 60, 105, 30);
        JLabel buffCruxStat = new JLabel("");
        buffCruxStat.setBounds(80, 90, 105, 30);
        JLabel buffScore = new JLabel("");
        buffScore.setBounds(80, 120, 105, 30);

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
