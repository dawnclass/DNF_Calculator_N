package org.dnf_calc_n.ui.main;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.calculate.Buff;
import org.dnf_calc_n.calculate.Damage;
import org.dnf_calc_n.data.LoadString;
import org.dnf_calc_n.ui.component.RoundButton;
import org.dnf_calc_n.ui.sub.WindowCustom;
import org.dnf_calc_n.ui.sub.WindowCustomOption;
import org.dnf_calc_n.ui.sub.WindowExplain;
import org.dnf_calc_n.ui.sub.WindowSave;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class PanelSelect extends JPanel {

    JPanel root;
    Common common = new Common();
    Buff buff;
    Damage damage;
    HashMap<String, Font> mapFont;
    HashMap<String, ImageIcon> mapIconItem;
    HashMap<String, JButton> mapInfoButtons;

    PanelResult panelResult;
    PanelCondition panelCondition;
    HashMap<String, String> mapResultBuff;

    PanelInfo panelInfo;
    JPanel panelFilter;
    JPanel panelSelectItem;
    JScrollPane scrollPane;
    private final ArrayList<Equipment> listEquipment = new ArrayList<>();
    JSONObject equipmentData;
    Color bgColor = new Color(50, 46, 52);
    Color sectionColor = new Color(34, 32, 37);
    JLabel labelNowName, labelNowExplain;
    WindowSave windowSave;
    HashMap<String, JComboBox<String>> mapWidgetCombo;
    WindowExplain windowExplain;
    WindowCustomOption windowCustomOption;

    String selectedMyth = "";
    JSONArray equipmentListJson;
    PanelSelect panelSelect;

    public PanelSelect(
            JPanel root, PanelResult panelResult, PanelCondition panelCondition,
            JSONObject equipmentData, HashMap<String, ImageIcon> mapIconItem,
            PanelInfo panelInfo,
            Buff buff, Damage damage,
            HashMap<String, JComboBox<String>> mapWidgetCombo,
            WindowExplain windowExplain, WindowCustomOption windowCustomOption
    ){
        this.windowCustomOption = windowCustomOption;
        this.root = root;
        this.mapWidgetCombo = mapWidgetCombo;
        this.panelSelect = this;
        this.panelResult = panelResult;
        this.panelCondition = panelCondition;
        mapFont = common.loadFont();
        this.buff = buff;
        this.damage = damage;
        this.panelInfo = panelInfo;
        this.mapInfoButtons = panelInfo.getMapInfoButtons();
        this.mapIconItem = mapIconItem;
        this.equipmentData = equipmentData;
        this.windowExplain = windowExplain;

        this.setBackground(bgColor);
        this.setBounds(10, 170, 450, 500);
        this.setLayout(null);
        root.add(this);

        makePartButton();
        makeEquipmentButton();
        makeMouseOverNameLabel();
        makeFilterPanel();
        makeExtraButton();

        JSONObject jsonSave = common.loadJsonObject("cache/selected.json");
        try{
            equipmentListJson = (JSONArray) jsonSave.get("equipments");
            for(Object o : equipmentListJson){
                String code = (String) o;
                boolean isPassed = panelInfo.setEquipment(code);
                if(!isPassed){
                    JLabel alertLabel = new JLabel(LoadString.strGet("?????? ?????? ??????"));
                    alertLabel.setFont(mapFont.get("bold"));
                    JOptionPane.showMessageDialog(
                            this, alertLabel, LoadString.strGet("??????"),
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            }
            panelInfo.updateInfo();
            calculationPackage();
        }catch (NullPointerException e){
            equipmentListJson = new JSONArray();
        }
    }

    private void makeExtraButton(){
        JButton resetButton = new JButton();
        resetButton.setText(LoadString.strGet("<html><body style='text-align:center;'>??????<br>?????????</body></html>"));
        resetButton.setHorizontalAlignment(JLabel.CENTER);
        resetButton.setBackground(new Color(255, 157, 157));
        resetButton.setForeground(Color.BLACK);
        resetButton.setBounds(265, 10, 80, 50);
        resetButton.setFont(mapFont.get("bold"));
        resetButton.addActionListener(e -> {
            String[] answers = {
                    LoadString.strGet("?????????"), LoadString.strGet("??????")
            };
            JLabel alertLabel = new JLabel(LoadString.strGet("????????? ?????????????????????????"));
            alertLabel.setFont(mapFont.get("bold"));
            int ans = JOptionPane.showOptionDialog(
                    panelSelect, alertLabel, LoadString.strGet("?????? ??????"),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null, answers, answers[0]
            );
            if(ans == 0){
                panelInfo.resetInfoPanel();
                calculationPackage();
            }

        });
        this.add(resetButton);

        JButton saveButton = new JButton();
        saveButton.setText(LoadString.strGet("<html><body style='text-align:center;'>?????????<br>??????</body></html>"));
        saveButton.setHorizontalAlignment(JLabel.CENTER);
        saveButton.setBackground(new Color(157, 214, 255));
        saveButton.setForeground(Color.BLACK);
        saveButton.setBounds(357, 10, 80, 50);
        saveButton.setFont(mapFont.get("bold"));
        saveButton.addActionListener(e -> {
            try{
                windowSave.dispose();
            }catch (Exception ignored){}
            windowSave = new WindowSave(root, panelSelect, panelInfo, mapWidgetCombo);
            windowSave.startSave();
        });
        this.add(saveButton);
    }

    public void calculationPackage(){
        panelResult.resetBuffValue();
        System.out.println("?????? ?????? ??????");
        damage.startDamageCalculate(panelInfo.getMapEquipments());
        panelCondition.setConditions(damage.getConditionJson());
        damage.applyCondition(panelCondition.getMapSelectCondition());
        buff.setLevelingArray(damage.getArrayLeveling());
        boolean isBuff = buff.startBuffCalculate(panelInfo.getMapEquipments());
        if(isBuff){
            System.out.println("?????? ?????? ??????");
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
        panelResult.setDetailMap(damage.getDetailMap());
    }

    JTextField searchByName;
    private void makeFilterPanel(){
        panelFilter = new JPanel();
        panelFilter.setBackground(bgColor);
        panelFilter.setBorder(new EmptyBorder(0,0,0,0));
        panelFilter.setBounds(255, 70, 190, 485);
        panelFilter.setLayout(null);
        this.add(panelFilter);

        // ???????????? ???????????? ??????
        JLabel labelSearch = new JLabel(LoadString.strGet("???????????? :"));
        labelSearch.setBorder(new EmptyBorder(0,0,0,0));
        labelSearch.setBounds(5, 3, 70, 20);
        labelSearch.setForeground(Color.WHITE);
        labelSearch.setFont(mapFont.get("normal_bold"));
        panelFilter.add(labelSearch);
        searchByName = new JTextField(10);
        searchByName.setBounds(70, 5, 100, 20);
        searchByName.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        containText = searchByName.getText();
                        updateEquipmentList();
                    }
                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        containText = searchByName.getText();
                        updateEquipmentList();
                    }
                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        containText = searchByName.getText();
                        updateEquipmentList();
                    }
                }
        );
        panelFilter.add(searchByName);

        //?????? ?????? ?????????
        String[][] arrayTheme = {
                {"?????????", "bae7af", "??????", "eeafaf", "??????", "eeafaf"},
                {"?????????", "bae7af", "??????", "eeafaf", "??????", "eeafaf"},
                {"?????????", "bae7af", "??????", "eeafaf", "??????", "eeafaf"},
                {"?????????", "bae7af", "??????", "eeafaf", "??????", "eeafaf"},
                {"?????????", "bae7af", "??????", "eeafaf", "??????", "eeafaf"},
                {"??????", "afc4e7", "???HP", "f3cda0", "???MP", "f3cda0"},
                {"??????", "afc4e7", "???HP", "f3cda0", "???MP", "f3cda0"},
                {"??????", "afc4e7", "HP??????", "f3cda0", "MP??????", "f3cda0"},
                {"????????????", "cccccc", "??????", "cccccc", "MP??????", "f3cda0"},
                {"??????", "cccccc", "??????", "cccccc", "?????????", "cccccc"},
                {"?????????", "cccccc", "??????", "cccccc", "?????????", "cccccc"},
                {"??????", "cccccc", "?????????", "cccccc", "?????????", "cccccc"},
                {"??????", "ddaadd", "??????", "ddaadd", "?????????", "ddaadd"},
                {"", "cccccc", "", "cccccc", "", "cccccc"},
                {"", "ddaadd", "", "eeafaf", "", "cccccc"}
                //
        };
        Color test = new Color(255, 255, 255);

        for(int i=0;i<arrayTheme.length;i++){
            for(int j=0;j<3;j++){
                String nowTheme = arrayTheme[i][j*2];
                if("".equals(nowTheme)) continue;
                int nowColor = Integer.parseInt(arrayTheme[i][j*2+1], 16);
                RoundButton tagSearchBtn = new RoundButton(nowTheme);
                tagSearchBtn.setBounds(10+60*j, 35+30*i, 50, 25);
                tagSearchBtn.setBackground(new Color(
                        common.changeBright(nowColor, 0.5)
                ));
                tagSearchBtn.setFont(mapFont.get("normal_bold"));
                tagSearchBtn.setForeground(Color.BLACK);
                tagSearchBtn.addActionListener(e -> {
                    if(themeTag.contains(nowTheme)){
                        themeTag.remove(nowTheme);
                        tagSearchBtn.setBackground(new Color(
                                common.changeBright(nowColor, 0.5)
                        ));
                    }else{
                        themeTag.add(nowTheme);
                        tagSearchBtn.setBackground(new Color(nowColor));
                    }
                    updateEquipmentList();
                });
                panelFilter.add(tagSearchBtn);
            }
        }
    }

    private void makeMouseOverNameLabel(){
        labelNowName = new JLabel();
        labelNowName.setBorder(new EmptyBorder(0, 0, 0, 0));
        labelNowName.setBackground(bgColor);
        labelNowName.setFont(mapFont.get("bold"));
        labelNowName.setForeground(new Color(0xffffff));
        labelNowName.setBounds(4, 375, 250, 30);
        this.add(labelNowName);

        labelNowExplain = new JLabel();
        labelNowExplain.setBorder(new EmptyBorder(0, 0, 0, 0));
        labelNowExplain.setBackground(bgColor);
        labelNowExplain.setFont(mapFont.get("normal"));
        labelNowExplain.setForeground(new Color(0xffffff));
        labelNowExplain.setBounds(4, 400, 250, 100);
        labelNowExplain.setVerticalAlignment(JLabel.TOP);
        this.add(labelNowExplain);
    }

    private void makeEquipmentButton(){
        panelSelectItem = new JPanel();
        panelSelectItem.setBackground(sectionColor);
        panelSelectItem.setSize(new Dimension(242, 300));
        //panelSelectItem.setBounds(4, 74, 224, 322);
        panelSelectItem.setLayout(new GridBagLayout());
        scrollPane = new JScrollPane(panelSelectItem
                , ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //scrollPane.setPreferredSize(new Dimension(242, 322));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBounds(4, 74, 242, 300);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.getViewport().setBackground(sectionColor);
        this.add(scrollPane);

        updateEquipmentButton();
    }

    List<String> isCustomCodeArray = Arrays.asList("012", "022", "032", "242");

    private void updateEquipmentButton(){
        int len = listEquipment.size();
        panelSelectItem.removeAll();
        GridBagConstraints frameConstraints = new GridBagConstraints();
        int border;
        if(len > 81){
            border = 0;
        }else{
            border = 1;
        }
        for(int i=0;i<len;i++){
            String code = listEquipment.get(i).toString();
            JButton btnNow = new JButton();
            btnNow.setBackground(new Color(34, 32, 37));
            btnNow.setIcon(mapIconItem.get(code));
            btnNow.setBorder(new EmptyBorder(1,border,1,border));
            frameConstraints.gridx = i%8;
            frameConstraints.gridy = i/8;
            frameConstraints.weightx = 0;
            frameConstraints.weighty = 0;
            btnNow.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    super.mouseEntered(e);
                    // System.out.println("??????????????? : "+code);
                    try{
                        StringBuilder nowExplain = new StringBuilder();
                        nowExplain.append("<html>");
                        JSONObject nowItem = (JSONObject) equipmentData.get(code);
                        labelNowName.setText((String)nowItem.get("??????"));

                        nowExplain.append((String) nowItem.get("??????")).append(" / ");
                        JSONArray themeArray = (JSONArray)nowItem.get("??????");
                        for (Object o : themeArray) {
                            nowExplain.append(o).append(" ");
                        }
                        nowExplain.append("<br>"+LoadString.strGet("??????")+" : ");
                        JSONArray damageArray = (JSONArray)nowItem.get("????????????");
                        for (Object o : damageArray) {
                            Double oo = (Double) o;
                            nowExplain.append(oo.intValue()).append(" ");
                        }
                        nowExplain.append("<br>"+LoadString.strGet("??????")+" : ");
                        //nowExplain.append(((Double)nowItem.get("basicBuff")).intValue()).append(" / ");
                        JSONArray buffArray = (JSONArray)nowItem.get("????????????");
                        for (Object o : buffArray) {
                            Double oo = (Double) o;
                            nowExplain.append(oo.intValue()).append(" ");
                        }
                        nowExplain.append("<br>"+LoadString.strGet("??????")+" : ");
                        JSONArray dropArray = (JSONArray)nowItem.get("??????");
                        for (Object o : dropArray) {
                            nowExplain.append(o).append(" ");
                        }

                        nowExplain.append("</html>");
                        labelNowExplain.setText(nowExplain.toString());
                    }catch (NullPointerException ignored){}
                }
                @Override
                public void mousePressed(MouseEvent e) {clickedTime = e.getWhen();}
                @Override
                public void mouseReleased(MouseEvent e){
                    long releaseTime = e.getWhen();
                    System.out.println(releaseTime-clickedTime);
                    if(releaseTime-clickedTime > 500){
                        windowExplain.getEquipmentCode(code);
                        windowExplain.setLocationRelativeTo(panelSelect);
                        windowExplain.setVisible(true);
                        return;
                    }
                    // System.out.println("?????? : "+code);
                    boolean isPassed = panelInfo.setEquipment(code);
                    if(!isPassed){
                        JLabel alertLabel = new JLabel(LoadString.strGet("?????? ?????? ??????"));
                        alertLabel.setFont(mapFont.get("bold"));
                        JOptionPane.showMessageDialog(
                                panelSelect, alertLabel, LoadString.strGet("??????"),
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                    if(code.length()==5){ // ?????????
                        String isCustomCode = code.substring(2, 5);
                        if(isCustomCodeArray.contains(isCustomCode)){
                            if(code.equals(panelInfo.getMapEquipments().get(code.substring(0, 2)))){
                                windowCustomOption.setCustomEquipment(code);
                            }

                        }
                    }
                    panelInfo.updateInfo();
                    calculationPackage();
                }
            });
            panelSelectItem.add(btnNow, frameConstraints);
        }
        if(len < 8){
            JLabel gap = new JLabel();
            gap.setBackground(new Color(34, 32, 37));
            frameConstraints.gridx = len;
            frameConstraints.gridy = 0;
            frameConstraints.weightx = 8-len;
            panelSelectItem.add(gap, frameConstraints);
        }
        if(len < 73){
            JLabel gap = new JLabel();
            gap.setBackground(new Color(34, 32, 37));
            frameConstraints.gridx = 7;
            frameConstraints.gridy = len/8+1;
            frameConstraints.weighty = 1;
            panelSelectItem.add(gap, frameConstraints);
        }
        panelSelectItem.updateUI();
    }
    static long clickedTime = 0;

    ArrayList<JButton> listPartBtn = new ArrayList<>();
    private final String[] TAGS = {"",
            "77", "11", "12", "13", "14", "15",
            "21", "22", "23", "31", "32", "33"};
    private final String[] NAMES = {"<HTML>???<br>???</HTML>",
            LoadString.strGet("??????"), LoadString.strGet("??????"), LoadString.strGet("??????"),
            LoadString.strGet("??????"), LoadString.strGet("??????"), LoadString.strGet("??????"),
            LoadString.strGet("??????"), LoadString.strGet("??????"), LoadString.strGet("??????"),
            LoadString.strGet("??????"), LoadString.strGet("??????"), LoadString.strGet("??????")};
    private void makePartButton(){
        for(int i=0;i<13;i++){
            final String tag = TAGS[i];
            JButton btnNow = new JButton(NAMES[i]);
            btnNow.setBackground(new Color(255, 255, 255));
            btnNow.setFont(new Font("", Font.PLAIN, 12));
            btnNow.setBorder(new BevelBorder(BevelBorder.RAISED));
            btnNow.setFocusPainted(false);
            int posX = 0, posY = 0;
            if(i==0){
                btnNow.setBounds(8, 8, 30, 60);
            }else{
                if(i > 6){
                    posX = 8+34*(i-6);
                    posY = 8+32;
                }else{
                    posX = 8+34*i;
                    posY = 8;
                }
                btnNow.setBounds(posX, posY, 30, 28);
            }
            btnNow.addActionListener(e -> {
                selectedTag = tag;
                for(JButton btn : listPartBtn){
                    btn.setBorder(new BevelBorder(BevelBorder.RAISED));
                }
                btnNow.setBorder(new BevelBorder(BevelBorder.LOWERED));
                updateEquipmentList();
            });
            this.add(btnNow);
            listPartBtn.add(btnNow);
        }
    }

    String selectedTag = "";
    String containText = "";
    HashSet<String> themeTag = new HashSet<>();
    private void updateEquipmentList(){
        listEquipment.clear();
        for(Object key : equipmentData.keySet()){
            try{
                String code = (String) key;
                if(!code.startsWith(selectedTag)) continue;  // ?????? ??????

                JSONObject nowItemJson = (JSONObject) equipmentData.get(code);
                String name = (String) nowItemJson.get("??????");
                if(!"".equals(containText) && !name.contains(containText)) continue;  // ?????? ??????

                if(themeTag.size() != 0){
                    boolean isContain = false;
                    JSONArray themeArray = (JSONArray) nowItemJson.get("??????");
                    for(Object t : themeArray){
                        String theme = (String) t;
                        if (themeTag.contains(theme)) {
                            isContain = true;
                            break;  //????????? OR ????????? ???????????????
                        }
                    }
                    if(!isContain) continue; // ?????? ?????? (OR???)
                }

                listEquipment.add(new Equipment(code));

            }catch (Exception ignored){}
        }
        Collections.sort(listEquipment);
        updateEquipmentButton();
        //System.out.println(listEquipment);
    }

}

class Equipment implements Comparable<Equipment>{

    private String code;
    private int num;
    private int set;

    public Equipment(String code){
        this.code = code;
        num = Integer.parseInt(code.substring(code.length()-1));
        if(code.length()==5){
            set = Integer.parseInt(code.substring(2, 4));
        }else if(code.length()==8){
            set = Integer.parseInt(code.substring(2, 7));
        }else{
            set = Integer.parseInt(code.substring(3, 6));
        }
    }

    @Override
    public int compareTo(Equipment equipment) {
        if(equipment.num < num){
            if(equipment.set < set){
                return 2;
            }else{
                return 1;
            }
        } else if (equipment.num > num){
            return -1;
        }
        return 0;
    }

    @Override
    public String toString(){
        return code;
    }
}
