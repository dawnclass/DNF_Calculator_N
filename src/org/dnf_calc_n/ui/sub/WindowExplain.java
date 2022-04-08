package org.dnf_calc_n.ui.sub;

import org.dnf_calc_n.Common;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class WindowExplain extends JFrame {

    Common common = new Common();
    static HashMap<String, Font> mapFont;
    JPanelEx panelExplain;
    JPanel panelWindow, panelSame1, panelSame2;
    JSONObject equipmentData;
    HashMap<String, ImageIcon> mapIconItem;
    JLabel icon, name;

    public WindowExplain(JSONObject equipmentData, HashMap<String, ImageIcon> mapIconItem){
        this.equipmentData = equipmentData;
        this.mapIconItem = mapIconItem;
        mapFont = common.loadFont();
        setResizable(false);
        setTitle("상세 설명");
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        GridBagConstraints gbc = new GridBagConstraints();
        panelWindow = new JPanel();
        panelWindow.setLayout(new GridBagLayout());
        panelWindow.setBorder(new EmptyBorder(5,5,5,5));
        add(panelWindow);

        gbc.gridx = 0;
        gbc.gridy = 0;
        icon = new JLabel();
        panelWindow.add(icon, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        name = new JLabel();
        name.setBorder(new EmptyBorder(0, 5, 0, 0));
        name.setFont(mapFont.get("normal_bold"));
        panelWindow.add(name, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 5;
        panelExplain = new JPanelEx(equipmentData, mapIconItem);
        panelWindow.add(panelExplain, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 5;
        JLabel same1 = new JLabel("옵션 2개 동일");
        same1.setFont(mapFont.get("normal_bold"));
        panelWindow.add(same1, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 5;
        panelSame2 = new JPanel();
        panelWindow.add(panelSame2, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 5;
        JLabel same2 = new JLabel("옵션 1개 동일");
        same2.setFont(mapFont.get("normal_bold"));
        panelWindow.add(same2, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 5;
        panelSame1 = new JPanel();
        panelWindow.add(panelSame1, gbc);

    }


    public void getEquipmentCode(String code){
        icon.setIcon(mapIconItem.get(code));
        name.setText((String)((JSONObject)equipmentData.get(code)).get("이름"));
        panelExplain.setElement(code);
        panelExplain.repaint();

        findSameOption(code);

        panelWindow.updateUI();
        pack();
    }

    private void findSameOption(String code){
        panelSame2.removeAll();
        panelSame1.removeAll();
        String part = code.substring(0, 2);
        JSONArray optType;
        ArrayList<String> same2List = new ArrayList<>();
        ArrayList<String> same1List = new ArrayList<>();
        try{
            optType = (JSONArray)((JSONObject)equipmentData.get(code)).get("옵션종류");
        }catch (Exception e) {return;}
        for(Object o : equipmentData.keySet()){
            String now = (String) o;
            int sameNum = 0;
            if(!now.substring(0, 2).equals(part)) continue;
            try{
                JSONArray nowOptType = (JSONArray)((JSONObject)equipmentData.get(now)).get("옵션종류");
                for (Object value : nowOptType) {
                    if (optType.contains(value)) sameNum++;
                }
                if(sameNum == 2){
                    same2List.add(now);
                }else if(sameNum == 1){
                    same1List.add(now);
                }
            }catch (Exception ignored){}
        }
        for(String now : same2List){
            JLabel nowIcon = new JLabel();
            nowIcon.setIcon(mapIconItem.get(now));
            nowIcon.setToolTipText((String)((JSONObject)equipmentData.get(now)).get("이름"));
            panelSame2.add(nowIcon);
        }
        for(String now : same1List){
            JLabel nowIcon = new JLabel();
            nowIcon.setIcon(mapIconItem.get(now));
            nowIcon.setToolTipText((String)((JSONObject)equipmentData.get(now)).get("이름"));
            panelSame1.add(nowIcon);
        }
        panelSame2.updateUI();
        panelSame1.updateUI();
    }


    static class JPanelEx extends JPanel{

        JSONObject equipmentData;
        HashMap<String, ImageIcon> mapIconItem;

        int numElements = 1;
        String[] elementArray = {""};

        public JPanelEx(JSONObject equipmentData, HashMap<String, ImageIcon> mapIconItem) {
            super(new FlowLayout());
            this.mapIconItem = mapIconItem;
            this.equipmentData = equipmentData;
        }

        public void setElement(String code){
            try{
                JSONObject nowItem = (JSONObject)equipmentData.get(code);
                JSONArray explainTextArray = (JSONArray)nowItem.get("설명");
                JSONArray damageArray = (JSONArray)nowItem.get("옵션피증");
                JSONArray buffArray = (JSONArray)nowItem.get("옵션버프");
                JSONArray dropArray = (JSONArray)nowItem.get("드랍");

                StringBuilder explainText = new StringBuilder();
                explainText.append("드랍: ");
                for (Object o : dropArray) {
                    explainText.append(o).append(" ");
                }
                explainText.append("\n\n");
                for(int i=0;i<explainTextArray.size();i++){
                    explainText.append("<").append(i + 1).append("옵션>\n");
                    explainText.append("피해증가: ").append(((Double)(damageArray.get(i))).intValue());
                    explainText.append("\n");
                    explainText.append("버프력: ").append(((Double)(buffArray.get(i))).intValue());
                    explainText.append("\n");
                    explainText.append((String) explainTextArray.get(i)).append("\n \n");
                }
                elementArray = explainText.toString().split("\n");
                System.out.println(Arrays.toString(elementArray));
                numElements = elementArray.length;
            }catch (Exception ignored){}
        }

        @Override
        public Dimension getPreferredSize(){
            FontMetrics fm = getFontMetrics(getFont());
            int width = 0;
            int height = fm.getHeight() * numElements;
            for (int index = 0; index < numElements; index++) {
                width = Math.max(width, fm.stringWidth(elementArray[index]));
            }
            return new Dimension(width, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int x = 0;
            int y = 0;
            //g.setFont(mapFont.get("normal"));
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i < numElements; i++) {
                g.drawString(elementArray[i], x, y + fm.getAscent());
                y += fm.getHeight();
            }
        }
    }

}


