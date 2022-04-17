package org.dnf_calc_n.ui.main;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.calculate.Buff;
import org.dnf_calc_n.calculate.Damage;
import org.dnf_calc_n.data.LoadJob;
import org.dnf_calc_n.data.LoadString;
import org.dnf_calc_n.ui.component.RoundButton;
import org.dnf_calc_n.ui.sub.WindowCustom;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.HashMap;

public class PanelCustom extends JPanel {

    boolean isBuff = false;
    PanelResult panelResult;
    PanelCondition panelCondition;
    PanelInfo panelInfo;
    Buff buff;
    Damage damage;
    JPanel root;
    JPanel customPanel;
    Common common = new Common();
    HashMap<String, Font> mapFont;
    HashMap<String, JComboBox<String>> mapWidgetCombo;
    WindowCustom windowCustom;

    public PanelCustom(JPanel root, PanelResult panelResult, PanelInfo panelInfo,
                       PanelCondition panelCondition,
                       HashMap<String, JComboBox<String>> mapWidgetCombo,
                       Buff buff, Damage damage){
        PanelCustom panelCustom = this;
        this.panelResult = panelResult;
        this.panelCondition = panelCondition;
        this.buff = buff;
        this.damage = damage;
        this.root = root;
        this.mapWidgetCombo = mapWidgetCombo;
        this.panelInfo = panelInfo;
        mapFont = common.loadFont();
        createCustomSection();

        RoundButton customBtn = new RoundButton();
        customBtn.setText(LoadString.strGet("커스텀"));
        customBtn.setFont(mapFont.get("bold"));
        customBtn.setBackground(new Color(200, 200, 200));
        customBtn.setBounds(120, 100, 60, 40);
        customBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
        customBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    windowCustom.dispose();
                }catch (Exception ignored){}
                windowCustom = new WindowCustom(panelCustom);
                windowCustom.startCustom();
            }
        });
        customPanel.add(customBtn);
    }

    private void createCustomSection(){
        customPanel = new JPanel();
        customPanel.setBackground(new Color(50, 46, 52));
        customPanel.setLayout(null);
        customPanel.setBounds(270, 10, 190, 150);
        root.add(customPanel);
        JSONObject json = common.loadJsonObject("cache/selected.json");

        JLabel nowLabelOption = new JLabel();
        nowLabelOption.setText(LoadString.strGet("장비옵션레벨 :"));
        nowLabelOption.setFont(mapFont.get("bold"));
        nowLabelOption.setForeground(Color.WHITE);
        nowLabelOption.setBounds(10, 70, 100, 18);
        customPanel.add(nowLabelOption);

        String[] optionLvs = {"20", "40", "60", "80"};
        String nowOptionLv;
        try{
            nowOptionLv = (String) json.get("optionLv");
        }catch (Exception e){
            nowOptionLv = "60";
        }
        JComboBox<String> optionLvCombo = new JComboBox<>(optionLvs);
        optionLvCombo.setBounds(110, 70, 70, 20);
        optionLvCombo.setFont(mapFont.get("normal"));
        optionLvCombo.setSelectedItem(nowOptionLv);
        optionLvCombo.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED){
                String optionLv = (String) optionLvCombo.getSelectedItem();
                common.saveCacheData("selected", "optionLv", optionLv);
                calculationPackage();
            }
        });
        customPanel.add(optionLvCombo);
        mapWidgetCombo.put("optionLv", optionLvCombo);


        JLabel nowLabel = new JLabel();
        nowLabel.setText(LoadString.strGet(" 직업 :"));
        nowLabel.setFont(mapFont.get("bold"));
        nowLabel.setForeground(Color.WHITE);
        nowLabel.setBounds(10, 10, 60, 18);
        customPanel.add(nowLabel);

        LoadJob loadJob = new LoadJob();
        HashMap<String, String[]> mapJob = loadJob.getJobMap();
        // System.out.println(mapJob.toString());
        String[] jobTypes = mapJob.get("types");
        // System.out.println(Arrays.toString(jobTypes));

        String nowJobType = "";
        try{
            nowJobType = (String) json.get("jobType");
        }catch (Exception e){
            nowJobType = "귀검사(여)";
        }
        JComboBox<String> jobCombo = new JComboBox<>(mapJob.get(nowJobType));
        jobCombo.setBounds(60, 40, 110, 20);
        jobCombo.setFont(mapFont.get("normal"));

        JComboBox<String> jobTypeCombo = new JComboBox<>(jobTypes);
        jobTypeCombo.setBounds(60, 10, 110, 20);
        jobTypeCombo.setFont(mapFont.get("normal"));
        jobTypeCombo.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED){
                String jobType = (String) jobTypeCombo.getSelectedItem();
                common.saveCacheData("selected", "jobType", jobType);
                // System.out.println(jobType);
                String[] jobArray = mapJob.get(jobType);
                // System.out.println(Arrays.toString(jobArray));
                jobCombo.removeAllItems();
                for(String job : jobArray) jobCombo.insertItemAt(job, jobCombo.getItemCount());
                jobCombo.setSelectedIndex(0);
            }
        });
        //System.out.println(nowJobType);
        jobTypeCombo.setSelectedItem(nowJobType);
        // System.out.println(jobCombo.getItemCount());
        // 초기 타입 설정 후 리스너 적용
        jobCombo.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED){
                String job = (String) jobCombo.getSelectedItem();
                // System.out.println(job);
                common.saveCacheData("selected", "job", job);
                calculationPackage();
            }
        });
        String nowJob = "";
        try{
            nowJob = (String) json.get("job");
        }catch (Exception e){
            nowJob = "소드마스터";
        }
        //System.out.println(nowJob);
        jobCombo.setSelectedItem(nowJob);
        customPanel.add(jobTypeCombo);
        customPanel.add(jobCombo);
        mapWidgetCombo.put("job", jobCombo);
        mapWidgetCombo.put("jobType", jobTypeCombo);

        JLabel nowLabel3 = new JLabel();
        nowLabel3.setText(LoadString.strGet("쿨감보정 :"));
        nowLabel3.setFont(mapFont.get("bold"));
        nowLabel3.setForeground(Color.WHITE);
        nowLabel3.setBounds(10, 98, 100, 18);
        customPanel.add(nowLabel3);
        String[] coolStrings = {
                LoadString.strGet("원쿨감"), LoadString.strGet("보정값")
        };
        JComboBox<String> comboCool = new JComboBox<>(coolStrings);
        comboCool.setFont(mapFont.get("normal"));
        comboCool.setBorder(new EmptyBorder(0, 0, 0, 0));
        comboCool.setBounds(10, 120, 100, 20);
        comboCool.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED){
                String cool = (String) comboCool.getSelectedItem();
                // System.out.println(job);
                common.saveCacheData("selected", "cool", cool);
                calculationPackage();
            }
        });
        String nowCool;
        try{
            nowCool = LoadString.strGet((String) json.get("cool"));
        }catch (Exception e){
            nowCool = LoadString.strGet("원쿨감");
        }
        comboCool.setSelectedItem(nowCool);
        customPanel.add(comboCool);
        mapWidgetCombo.put("cool", comboCool);
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
            HashMap<String, String> mapResultBuff = buff.getMapResult();
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

}
