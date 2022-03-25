package org.dnf_calc_n.ui.main;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.calculate.Buff;
import org.dnf_calc_n.calculate.Damage;
import org.dnf_calc_n.ui.component.RoundButton;
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

public class PanelSelect extends JPanel {

    Common common = new Common();
    Buff buff;
    Damage damage;
    HashMap<String, Font> mapFont;
    HashMap<String, ImageIcon> mapIconItem;
    HashMap<String, JButton> mapInfoButtons;

    PanelResult panelResult;
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

    String selectedMyth = "";

    public PanelSelect(
            JPanel root, PanelResult panelResult,
            JSONObject equipmentData, HashMap<String, ImageIcon> mapIconItem,
            PanelInfo panelInfo,
            Buff buff, Damage damage
    ){
        this.panelResult = panelResult;
        mapFont = common.loadFont();
        this.buff = buff;
        this.damage = damage;
        this.panelInfo = panelInfo;
        this.mapInfoButtons = panelInfo.getMapInfoButtons();
        this.mapIconItem = mapIconItem;
        this.equipmentData = equipmentData;
        this.setBackground(bgColor);
        this.setBounds(10, 170, 450, 500);
        this.setLayout(null);
        root.add(this);

        makePartButton();
        makeEquipmentButton();
        makeMouseOverNameLabel();
        makeFilterPanel();
    }

    JTextField searchByName;
    private void makeFilterPanel(){
        panelFilter = new JPanel();
        panelFilter.setBackground(bgColor);
        panelFilter.setBorder(new EmptyBorder(0,0,0,0));
        panelFilter.setBounds(255, 10, 190, 485);
        panelFilter.setLayout(null);
        this.add(panelFilter);

        // 이름으로 검색하는 기능
        JLabel labelSearch = new JLabel("이름검색 :");
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

        //태그 테마 생성부
        String[][] arrayTheme = {
                {"화속강", "bae7af", "화속저", "bae7af", "쿨감", "afc4e7"},
                {"수속강", "bae7af", "수속저", "bae7af", "회피", "afc4e7"},
                {"명속강", "bae7af", "명속저", "bae7af", "보호막", "afc4e7"},
                {"암속강", "bae7af", "암속저", "bae7af", "군중제어", "afc4e7"},
                {"모속강", "bae7af", "모속저", "bae7af", "편의성", "afc4e7"},
                {"중독", "eeafaf", "저HP", "f3cda0", "저MP", "f3cda0"},
                {"화상", "eeafaf", "고HP", "f3cda0", "고MP", "f3cda0"},
                {"빙결", "eeafaf", "HP소모", "f3cda0", "MP소모", "f3cda0"},
                {"감전", "eeafaf", "HP회복", "f3cda0", "MP회복", "f3cda0"},
                {"암흑", "eeafaf", "", "cccccc", "마법부여", "cccccc"},
                {"출혈", "eeafaf", "기본숙련", "cccccc", "부자", "cccccc"},
                {"저주", "eeafaf", "하급스킬", "cccccc", "콤보", "cccccc"},
                {"기절", "eeafaf", "상급스킬", "cccccc", "파티", "cccccc"},
                {"석화", "eeafaf", "무큐소모", "cccccc", "커맨드", "cccccc"},
                {"수면", "eeafaf", "상변다수", "eeafaf", "커스텀", "cccccc"}
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
        scrollPane.setBounds(4, 74, 242, 300);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.getViewport().setBackground(sectionColor);
        this.add(scrollPane);

        updateEquipmentButton();
    }
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
            var btnNow = new JButton();
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
                    // System.out.println("마우스오버 : "+code);
                    try{
                        StringBuilder nowExplain = new StringBuilder();
                        nowExplain.append("<html>");
                        JSONObject nowItem = (JSONObject) equipmentData.get(code);
                        labelNowName.setText((String)nowItem.get("이름"));

                        nowExplain.append((String) nowItem.get("부위")).append(" / ");
                        JSONArray themeArray = (JSONArray)nowItem.get("테마");
                        for (Object o : themeArray) {
                            nowExplain.append(o).append(" ");
                        }
                        nowExplain.append("<br>피증 : ");
                        JSONArray damageArray = (JSONArray)nowItem.get("옵션피증");
                        for (Object o : damageArray) {
                            Double oo = (Double) o;
                            nowExplain.append(oo.intValue()).append(" ");
                        }
                        nowExplain.append("<br>버프 : ");
                        //nowExplain.append(((Double)nowItem.get("basicBuff")).intValue()).append(" / ");
                        JSONArray buffArray = (JSONArray)nowItem.get("옵션버프");
                        for (Object o : buffArray) {
                            Double oo = (Double) o;
                            nowExplain.append(oo.intValue()).append(" ");
                        }

                        nowExplain.append("</html>");
                        labelNowExplain.setText(nowExplain.toString());
                    }catch (NullPointerException ignored){}
                }
                @Override
                public void mouseReleased(MouseEvent e){
                    // System.out.println("눌림 : "+code);
                    if(code.length()!=6 && "1".equals(code.substring(code.length()-1))){
                        String nowPart = code.substring(0, 2);
                        if(!nowPart.equals(selectedMyth) && !"".equals(selectedMyth)){
                            JLabel alertLabel = new JLabel("신화 중복 선택");
                            alertLabel.setFont(mapFont.get("bold"));
                            JOptionPane.showMessageDialog(
                                    null, alertLabel, "오류",
                                    JOptionPane.ERROR_MESSAGE
                            );
                            return;
                        }else{
                            selectedMyth = nowPart;
                        }
                    }
                    panelInfo.setEquipment(code);
                    boolean isBuff = buff.startBuffCalculate(panelInfo.getMapEquipments());
                    if(isBuff){
                        System.out.println("버퍼 계산 시작");
                        mapResultBuff = buff.getMapResult();
                        panelResult.setBuffResult(mapResultBuff);
                    }else{
                        System.out.println("딜러 계산 시작");
                        damage.startDamageCalculate(panelInfo.getMapEquipments());

                    }

                    panelInfo.updateInfo();
                }
            });
            panelSelectItem.add(btnNow, frameConstraints);
        }
        if(len < 8){
            var gap = new JLabel();
            gap.setBackground(new Color(34, 32, 37));
            frameConstraints.gridx = len;
            frameConstraints.gridy = 0;
            frameConstraints.weightx = 8-len;
            panelSelectItem.add(gap, frameConstraints);
        }
        if(len < 73){
            var gap = new JLabel();
            gap.setBackground(new Color(34, 32, 37));
            frameConstraints.gridx = 7;
            frameConstraints.gridy = len/8+1;
            frameConstraints.weighty = 1;
            panelSelectItem.add(gap, frameConstraints);
        }
        panelSelectItem.updateUI();
    }

    ArrayList<JButton> listPartBtn = new ArrayList<>();
    private final String[] TAGS = {"",
            "77", "11", "12", "13", "14", "15",
            "21", "22", "23", "31", "32", "33"};
    private final String[] NAMES = {"<HTML>전<br>체</HTML>",
            "무기", "상의", "하의", "어깨", "벨트", "신발",
            "팔찌", "목걸", "반지", "보장", "법석", "귀걸"};
    private void makePartButton(){
        for(int i=0;i<13;i++){
            final String tag = TAGS[i];
            var btnNow = new JButton(NAMES[i]);
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

            final int index = i;
            if(i!=0){
                mapInfoButtons.get(tag).addActionListener(e -> {
                    selectedTag = tag;
                    for(JButton btn : listPartBtn){
                        btn.setBorder(new BevelBorder(BevelBorder.RAISED));
                    }
                    listPartBtn.get(index).setBorder(new BevelBorder(BevelBorder.LOWERED));
                    updateEquipmentList();
                });
            }
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
                if(!code.startsWith(selectedTag)) continue;  // 부위 필터

                JSONObject nowItemJson = (JSONObject) equipmentData.get(code);
                String name = (String) nowItemJson.get("이름");
                if(!"".equals(containText) && !name.contains(containText)) continue;  // 이름 필터

                if(themeTag.size() != 0){
                    boolean isContain = false;
                    JSONArray themeArray = (JSONArray) nowItemJson.get("테마");
                    for(Object t : themeArray){
                        String theme = (String) t;
                        if (themeTag.contains(theme)) {
                            isContain = true;
                            break;  //일단은 OR 구조로 설계해놨음
                        }
                    }
                    if(!isContain) continue; // 테마 필터 (OR식)
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

    public Equipment(String code){
        this.code = code;
        num = Integer.parseInt(code.substring(code.length()-1));
    }

    @Override
    public int compareTo(Equipment equipment) {
        if(equipment.num < num){
            return 1;
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
