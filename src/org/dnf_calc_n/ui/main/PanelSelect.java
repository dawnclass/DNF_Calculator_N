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

    String selectedMyth = "";
    JSONArray equipmentListJson;

    public PanelSelect(
            JPanel root, PanelResult panelResult, PanelCondition panelCondition,
            JSONObject equipmentData, HashMap<String, ImageIcon> mapIconItem,
            PanelInfo panelInfo,
            Buff buff, Damage damage
    ){
        this.panelResult = panelResult;
        this.panelCondition = panelCondition;
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
        makeExtraButton();

        JSONObject jsonSave = common.loadJsonObject("cache/selected.json");
        try{
            equipmentListJson = (JSONArray) jsonSave.get("equipments");
            for(Object o : equipmentListJson){
                String code = (String) o;
                boolean isPassed = panelInfo.setEquipment(code);
                if(!isPassed){
                    JLabel alertLabel = new JLabel("신화 중복 선택");
                    alertLabel.setFont(mapFont.get("bold"));
                    JOptionPane.showMessageDialog(
                            null, alertLabel, "오류",
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
        resetButton.setText("<html><body style='text-align:center;'>선택<br>초기화</body></html>");
        resetButton.setHorizontalAlignment(JLabel.CENTER);
        resetButton.setBackground(new Color(255, 157, 157));
        resetButton.setForeground(Color.BLACK);
        resetButton.setBounds(260, 10, 80, 50);
        resetButton.setFont(mapFont.get("bold"));
        resetButton.addActionListener(e -> {
            String[] answers = {"초기화", "취소"};
            JLabel alertLabel = new JLabel("정말로 초기화하겠습니까?");
            alertLabel.setFont(mapFont.get("bold"));
            int ans = JOptionPane.showOptionDialog(
                    this, alertLabel, "확인 알림",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null, answers, answers[0]
            );
            if(ans == 0){
                panelInfo.resetInfoPanel();
            }

        });
        this.add(resetButton);
    }

    private void calculationPackage(){
        System.out.println("딜러 계산 시작");
        damage.startDamageCalculate(panelInfo.getMapEquipments());
        panelCondition.setConditions(damage.getConditionJson());
        damage.applyCondition(panelCondition.getMapSelectCondition());
        panelResult.setDamageArray(
                damage.getArrayTotalLevelDamage(),
                damage.getArrayTotalCoolDown(),
                damage.getArrayTotalLevelDamageWithCool()
        );
        panelResult.resetBuffValue();
        buff.setLevelingArray(damage.getArrayLeveling());
        boolean isBuff = buff.startBuffCalculate(panelInfo.getMapEquipments());
        if(isBuff){
            System.out.println("버퍼 계산 시작");
            mapResultBuff = buff.getMapResult();
            panelResult.setBuffResult(mapResultBuff);
        }
    }

    JTextField searchByName;
    private void makeFilterPanel(){
        panelFilter = new JPanel();
        panelFilter.setBackground(bgColor);
        panelFilter.setBorder(new EmptyBorder(0,0,0,0));
        panelFilter.setBounds(255, 70, 190, 485);
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
                {"화속강", "bae7af", "화상", "eeafaf", "중독", "eeafaf"},
                {"수속강", "bae7af", "빙결", "eeafaf", "저주", "eeafaf"},
                {"명속강", "bae7af", "감전", "eeafaf", "기절", "eeafaf"},
                {"암속강", "bae7af", "암흑", "eeafaf", "석화", "eeafaf"},
                {"모속강", "bae7af", "출혈", "eeafaf", "수면", "eeafaf"},
                {"쿨감", "afc4e7", "저HP", "f3cda0", "저MP", "f3cda0"},
                {"방어", "afc4e7", "고HP", "f3cda0", "고MP", "f3cda0"},
                {"유틸", "afc4e7", "HP회복", "f3cda0", "MP회복", "f3cda0"},
                {"마법부여", "cccccc", "부자", "cccccc", "MP소모", "f3cda0"},
                {"무큐", "cccccc", "콤보", "cccccc", "커맨드", "cccccc"},
                {"레벨링", "cccccc", "파티", "cccccc", "카운터", "cccccc"},
                {"평타", "cccccc", "기본기", "cccccc", "상급기", "cccccc"},
                {"신화", "ddaadd", "산물", "ddaadd", "커스텀", "ddaadd"},
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
                    boolean isPassed = panelInfo.setEquipment(code);
                    if(!isPassed){
                        JLabel alertLabel = new JLabel("신화 중복 선택");
                        alertLabel.setFont(mapFont.get("bold"));
                        JOptionPane.showMessageDialog(
                                null, alertLabel, "오류",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                    panelInfo.updateInfo();
                    calculationPackage();
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
