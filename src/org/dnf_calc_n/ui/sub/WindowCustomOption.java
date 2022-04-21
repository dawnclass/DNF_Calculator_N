package org.dnf_calc_n.ui.sub;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.calculate.Buff;
import org.dnf_calc_n.calculate.Damage;
import org.dnf_calc_n.data.LoadString;
import org.dnf_calc_n.ui.main.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class WindowCustomOption extends JFrame {

    JSONObject customOptionData;
    Set<Object> optionSet;
    Common common = new Common();
    HashMap<String, Font> mapFont;
    PanelSelect panelSelect;
    PanelInfo panelInfo;
    JPanel optionPanel;
    HashMap<String, ImageIcon> mapIconExtra;
    PanelResult panelResult;
    Buff buff;
    Damage damage;
    PanelCondition panelCondition;

    public WindowCustomOption(JSONObject customOptionData,
                              JPanel root, PanelSelect panelSelect,
                              PanelInfo panelInfo, HashMap<String, ImageIcon> mapIconExtra,
                              PanelResult panelResult, Buff buff, Damage damage, PanelCondition panelCondition){
        this.customOptionData = customOptionData;
        optionSet = customOptionData.keySet();
        this.panelSelect = panelSelect;
        this.panelInfo = panelInfo;
        this.mapIconExtra = mapIconExtra;
        this.panelResult = panelResult;
        this.buff = buff;
        this.damage = damage;
        this.panelCondition = panelCondition;
        mapFont = common.loadFont();
        setResizable(false);
        setSize(800, 600);
        setTitle(LoadString.strGet("커스텀 에픽 선택"));
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(root);

        optionPanel = new JPanel();
        optionPanel.setLayout(null);
        optionPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        optionPanel.setBackground(new Color(34, 32, 37));
        setContentPane(optionPanel);

        makeWindow();
    }

    String itemCode;
    ArrayList<String> ableOptionList = new ArrayList<>();
    public void setCustomEquipment(String code){
        searchByName.setText("");
        this.itemCode = code;
        ableOptionList.clear();
        containerOptionList.clear();
        System.out.println("커스텀 시작 = "+code);
        ArrayList<String> ableList = new ArrayList<>();
        ableList.add("전부");
        ableList.add(code);
        String part = code.substring(0, 1);
        if("1".equals(part)) ableList.add("방어구");
        if("2".equals(part)) ableList.add("악세");
        if("3".equals(part)) ableList.add("특장");
        for(Object k : optionSet){
            JSONObject nowJson = (JSONObject) customOptionData.get(k);
            String ablePart = (String) nowJson.get("부위");
            if(ableList.contains(ablePart)){
                ableOptionList.add((String)k);
            }
        }
        selectedOptionArray = new String[]{"0", "0", "0", "0"};
        try{
            JSONObject nowSave = common.loadJsonObject("cache/selected.json");
            JSONObject customJson = (JSONObject) nowSave.get("customOption");
            JSONArray nowEquipmentOption = (JSONArray)customJson.get(code);
            for(int i=0;i<4;i++){
                selectedOptionArray[i] = (String) nowEquipmentOption.get(i);
            }
        }catch (Exception ignored){}
        System.out.println("커스텀 가능 옵션 = "+ableOptionList);
        containerOptionList.addAll(ableOptionList);
        updateLabel();
        updateContainerOption();
        optionPanel.updateUI();
        containerPanel.updateUI();
        setVisible(true);
    }

    JLabel[] optionLabelArray = new JLabel[4];
    String[] selectedOptionArray;
    private void makeWindow(){
        for(int i=0;i<4;i++){
            optionLabelArray[i] = new JLabel();
            optionLabelArray[i].setBounds(10, 10+135*i, 340, 125);
            optionLabelArray[i].setBorder(new LineBorder(Color.GRAY, 3));
            optionLabelArray[i].setBackground(new Color(50, 46, 52));
            optionLabelArray[i].setOpaque(true);
            optionLabelArray[i].setForeground(Color.WHITE);
            optionLabelArray[i].setFont(mapFont.get("normal"));
            optionLabelArray[i].setVerticalTextPosition(JLabel.CENTER);
            optionPanel.add(optionLabelArray[i]);

            JButton nowButton = new JButton();
            nowButton.setBounds(360, 10+67-15+135*i, 30, 30);
            nowButton.setBorder(new LineBorder(Color.BLACK, 2));
            nowButton.setHorizontalAlignment(JButton.CENTER);
            nowButton.setVerticalAlignment(JButton.CENTER);
            nowButton.setIcon(mapIconExtra.get("X"));
            int finalI = i;
            nowButton.addActionListener(e -> {
                selectedOptionArray[finalI] = "0";
                updateLabel();
                calculationPackage();
            });
            optionPanel.add(nowButton);
        }
        JLabel labelSearch = new JLabel(LoadString.strGet("내용검색 :"));
        labelSearch.setBorder(new EmptyBorder(0,0,0,0));
        labelSearch.setBounds(450, 13, 70, 20);
        labelSearch.setForeground(Color.WHITE);
        labelSearch.setFont(mapFont.get("normal_bold"));
        optionPanel.add(labelSearch);
        searchByName = new JTextField(20);
        searchByName.setBounds(515, 15, 200, 20);
        searchByName.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        containText = searchByName.getText();
                        searchByText();
                    }
                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        containText = searchByName.getText();
                        searchByText();
                    }
                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        containText = searchByName.getText();
                        searchByText();
                    }
                }
        );
        optionPanel.add(searchByName);

        containerPanel = new JPanel();
        containerPanel.setSize(320, 500);
        containerPanel.setBackground(new Color(34, 32, 37));
        containerPanel.setLayout(new GridBagLayout());
        containerPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        JScrollPane scrollPane = new JScrollPane(
                containerPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setBounds(420, 50, 340, 500);
        scrollPane.setBorder(new LineBorder(Color.GRAY, 3));
        scrollPane.getViewport().setBackground(new Color(34, 32, 37));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        optionPanel.add(scrollPane);
    }

    JPanel containerPanel;
    JTextField searchByName;
    String containText;
    ArrayList<String> containerOptionList = new ArrayList<>();
    private void searchByText(){
        containerOptionList.clear();
        for(String k : ableOptionList){
            if("".equals(containText)){
                containerOptionList.add(k);
            }else{
                JSONObject nowJson = (JSONObject) customOptionData.get(k);
                String text = (String) nowJson.get("이름");
                if(text.contains(containText)){
                    containerOptionList.add(k);
                }
            }
        }
        updateContainerOption();
    }

    private void updateContainerOption(){
        containerPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        int i = 0;
        for(String key : containerOptionList){
            gbc.gridx=0;
            gbc.gridy=i;
            JButton nowButton = new JButton();
            nowButton.setBorder(new BevelBorder(BevelBorder.RAISED, Color.DARK_GRAY, Color.DARK_GRAY));
            nowButton.setMinimumSize(new Dimension(314, 100));
            nowButton.setMaximumSize(new Dimension(314, 100));
            nowButton.setPreferredSize(new Dimension(314, 100));
            nowButton.setFont(mapFont.get("normal"));
            nowButton.setForeground(Color.WHITE);
            nowButton.setBackground(new Color(50, 46, 52));
            nowButton.setHorizontalAlignment(SwingConstants.LEFT);
            nowButton.setFocusPainted(false);

            JSONObject nowJson = (JSONObject) customOptionData.get(key);
            StringBuilder nowText = new StringBuilder();
            nowText.append("<html><body topmargin=\"5\"  leftmargin=\"5\" marginwidth=\"5\" marginheight=\"5\">");
            nowText.append("피해증가 : ").append(((Double) nowJson.get("피증")).intValue());
            nowText.append(" / 버프력 : ").append(((Double) nowJson.get("버프")).intValue()).append("<br><br>");
            String nowTextOriginal = (String) nowJson.get("이름");
            nowTextOriginal = nowTextOriginal.replace("\n", "<br>");
            nowText.append(nowTextOriginal).append("</body></html>");
            nowButton.setText(nowText.toString());
            nowButton.addActionListener(e -> {
                System.out.println(Arrays.toString(selectedOptionArray));
                if(Arrays.asList(selectedOptionArray).contains(key)){
                    return;
                }
                for(int j=0;j<4;j++){
                    if("0".equals(selectedOptionArray[j])){
                        selectedOptionArray[j] = key;
                        updateLabel();
                        calculationPackage();
                        return;
                    }
                }
            });
            containerPanel.add(nowButton, gbc);

            i++;
        }
        containerPanel.updateUI();
    }

    private void updateLabel(){
        JSONObject selectedJson = common.loadJsonObject("cache/selected.json");
        JSONObject nowEquipmentCustom;
        if(selectedJson.get("customOption") == null){
            nowEquipmentCustom = new JSONObject();
        }else{
            nowEquipmentCustom = (JSONObject) selectedJson.get("customOption");
        }
        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<4;i++){
            StringBuilder nowText = new StringBuilder();
            nowText.append("<html><body topmargin=\"5\"  leftmargin=\"5\" marginwidth=\"5\" marginheight=\"5\">");
            JSONObject nowJson = (JSONObject) customOptionData.get(selectedOptionArray[i]);
            nowText.append("피해증가 : ").append(((Double) nowJson.get("피증")).intValue());
            nowText.append(" / 버프력 : ").append(((Double) nowJson.get("버프")).intValue()).append("<br><br>");
            String nowTextOriginal = (String) nowJson.get("이름");
            nowTextOriginal = nowTextOriginal.replace("\n", "<br>");
            nowText.append(nowTextOriginal).append("</body></html>");
            optionLabelArray[i].setText(nowText.toString());

            jsonArray.add(selectedOptionArray[i]);
            nowEquipmentCustom.put(itemCode, jsonArray);
            common.saveCacheData("selected", "customOption", nowEquipmentCustom);
        }

    }

    public void calculationPackage(){
        panelResult.resetBuffValue();
        System.out.println("딜러 계산 시작");
        damage.startDamageCalculate(panelInfo.getMapEquipments());
        panelCondition.setConditions(damage.getConditionJson());
        damage.applyCondition(panelCondition.getMapSelectCondition());
        buff.setLevelingArray(damage.getArrayLeveling());
        boolean isBuff = buff.startBuffCalculate(panelInfo.getMapEquipments());
        if(isBuff){
            System.out.println("버퍼 계산 시작");
            mapResultBuff = buff.getMapResult();
            panelResult.setBuffResult(mapResultBuff);
            double buffAdditionalStat = buff.getAdditionalDealerStat();
            damage.setAdditionalStat(buffAdditionalStat);
        }
        panelResult.setDamageArray(
                damage.getArrayTotalLevelDamage(),
                damage.getArrayTotalCoolDown(),
                damage.getArrayTotalLevelDamageWithCool()
        );
    }
    HashMap<String, String> mapResultBuff;

}
