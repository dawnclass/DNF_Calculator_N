package org.dnf_calc_n.ui.main;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.calculate.ScoreFarming;
import org.dnf_calc_n.data.LoadString;
import org.dnf_calc_n.ui.component.RoundButton;
import org.dnf_calc_n.ui.sub.WindowCustomOption;
import org.dnf_calc_n.ui.sub.WindowExplain;
import org.dnf_calc_n.ui.sub.WindowFarming;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PanelInfo extends JPanel {

    Common common = new Common();

    public HashMap<String, String[]> mapCustomOption = new HashMap<>();
    public HashMap<String, String[]> getMapCustomOption() { return mapCustomOption; }
    public HashMap<String, String> mapEquipments = new HashMap<>();
    public HashMap<String, String> getMapEquipments(){
        return mapEquipments;
    }
    public HashMap<String, JButton> mapInfoButtons = new HashMap<>();
    public HashMap<String, JButton> getMapInfoButtons(){
        return mapInfoButtons;
    }
    HashMap<String, ImageIcon> mapIconItem;
    HashMap<String, ImageIcon> mapIconExtra;
    JSONObject equipmentData;
    JPanel root;

    WindowFarming windowFarming;
    WindowExplain windowExplain;
    ScoreFarming scoreFarming;
    HashMap<String, HashMap<String, Double>> mapFarmingScore;

    public PanelInfo(JPanel root, HashMap<String, ImageIcon> mapIconItem,
                     HashMap<String, ImageIcon> mapIconExtra,
                     JSONObject equipmentData,
                     WindowExplain windowExplain){
        this.root = root;
        this.equipmentData = equipmentData;
        this.windowExplain = windowExplain;
        this.mapIconItem = mapIconItem;
        this.mapIconExtra = mapIconExtra;
        this.setBackground(new Color(50, 46, 52));
        this.setBounds(10, 10, 250, 150);
        this.setLayout(null);
        root.add(this);

        scoreFarming = new ScoreFarming(equipmentData);
        updateInfo();

        RoundButton farmingBtn = new RoundButton(LoadString.strGet("파밍 분석"));
        farmingBtn.setFont(common.loadFont().get("normal_bold"));
        farmingBtn.setBounds(5, 120,75, 30);
        farmingBtn.addActionListener(e->{
            try{
                windowFarming.dispose();
            }catch (Exception ignored){}
            windowFarming = new WindowFarming( root,
                    mapEquipments, mapIconItem, mapIconExtra, equipmentData
            );
            windowFarming.setVisible(true);
        });

        this.add(farmingBtn);
    }

    String selectedMyth = "";
    List<String> isCustomCodeArray = Arrays.asList("012", "022", "032");

    public boolean setEquipment(String equipment){

        String code;
        boolean isMyth = false;
        if(equipment.length()==6){
            code = "77";
        }else{
            code = equipment.substring(0, 2);
            if("1".equals(equipment.substring(equipment.length()-1))) isMyth = true;
        }
        if(equipment.equals(mapEquipments.get(code))){  // 동일 장비 선택
            mapEquipments.remove(code);
            common.deleteEquipmentCacheData(equipment, false);
            if(isMyth){
                selectedMyth = "";
            }
        }else{  //동일 장비 아님
            if(isMyth && !"".equals(selectedMyth) && !selectedMyth.equals(code)){  //신화 중복
                return false;
            }else{
                if(mapEquipments.get(code) != null){  //이미 템 있을경우 (교체)
                    common.deleteEquipmentCacheData(mapEquipments.get(code), false);
                    if(!isMyth && selectedMyth.equals(code)) selectedMyth = "";
                }
                mapEquipments.put(equipment.substring(0, 2), equipment);
                common.saveCacheData("selected", "equipments", equipment);
                if(isMyth) selectedMyth = code;
            }

        }
        // System.out.println(mapEquipments);
        return true;
    }

    public void resetInfoPanel(){
        common.deleteEquipmentCacheData("", true);
        mapEquipments.clear();
        selectedMyth = "";
        updateInfo();
    }

    private final String[] TAGS = {"77", "11", "12", "13", "14", "15",
            "21", "22", "23", "31", "32", "33"};
    private final int[][] POS = {{6, 0}, {1, 0}, {0, 1}, {0, 0}, {1, 1}, {0, 2},
            {6, 1}, {7, 1}, {7, 2}, {6, 2}, {7, 3}, {6, 3}};
    private final int[] START_POS = {6, 6};

    public void updateInfo(){
        for(int i=0;i<12;i++){
            String tag = TAGS[i];
            int[] pos = POS[i];
            JButton nowBtn;
            if(mapInfoButtons.get(tag) == null){
                nowBtn = new JButton();
                nowBtn.setBackground(new Color(50, 46, 52));
                nowBtn.setBounds(START_POS[0] + 30 * pos[0], START_POS[1] + 30 * pos[1], 28, 28);
                nowBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
                this.add(nowBtn);
                mapInfoButtons.put(tag, nowBtn);
            }else{
                nowBtn = mapInfoButtons.get(tag);
            }
            if(mapEquipments.get(tag) != null){
                nowBtn.setIcon(mapIconItem.get(mapEquipments.get(tag)));
                nowBtn.setToolTipText(
                        (String)((JSONObject)equipmentData.get(mapEquipments.get(tag))).get("이름")
                );
                nowBtn.addActionListener(e -> {
                    windowExplain.getEquipmentCode(mapEquipments.get(tag));
                    windowExplain.setLocationRelativeTo(this);
                    windowExplain.setVisible(true);
                });
            }else{
                nowBtn.setIcon(mapIconExtra.get("info"+tag));
            }
        }
        this.updateUI();


    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(mapIconExtra.get("bg_info").getImage(), 0, 0, new Color(50, 46, 52), this);

    }
}
