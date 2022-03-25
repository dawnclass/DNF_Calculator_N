package org.dnf_calc_n.ui.main;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.data.LoadJob;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.HashMap;

public class PanelCustom extends JPanel {

    JPanel root;
    JPanel customPanel;
    Common common = new Common();
    HashMap<String, Font> mapFont;
    HashMap<String, JComboBox<String>> mapWidgetCombo;

    public PanelCustom(JPanel root, HashMap<String, JComboBox<String>> mapWidgetCombo){
        this.root = root;
        this.mapWidgetCombo = mapWidgetCombo;
        mapFont = common.loadFont();
        createCustomSection();
    }

    private void createCustomSection(){
        customPanel = new JPanel();
        customPanel.setBackground(new Color(50, 46, 52));
        customPanel.setLayout(null);
        customPanel.setBounds(270, 10, 190, 150);
        root.add(customPanel);
        var json = common.loadJsonObject("cache/selected.json");
        var nowLabel = new JLabel();
        nowLabel.setText(" 직업");
        nowLabel.setFont(mapFont.get("bold"));
        nowLabel.setForeground(Color.WHITE);
        nowLabel.setBounds(10, 10, 60, 18);
        customPanel.add(nowLabel);
        var nowLabel2 = new JLabel();
        nowLabel2.setText(":");
        nowLabel2.setFont(mapFont.get("bold"));
        nowLabel2.setForeground(Color.WHITE);
        nowLabel2.setBounds(45, 10, 10, 18);
        customPanel.add(nowLabel2);

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
        var jobCombo = new JComboBox<>(mapJob.get(nowJobType));
        jobCombo.setBounds(60, 40, 110, 20);
        jobCombo.setFont(mapFont.get("normal"));

        var jobTypeCombo = new JComboBox<>(jobTypes);
        jobTypeCombo.setBounds(60, 10, 110, 20);
        jobTypeCombo.setFont(mapFont.get("normal"));
        jobTypeCombo.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED){
                var jobType = (String) jobTypeCombo.getSelectedItem();
                // System.out.println(jobType);
                var jobArray = mapJob.get(jobType);
                // System.out.println(Arrays.toString(jobArray));
                jobCombo.removeAllItems();
                for(String job : jobArray) jobCombo.insertItemAt(job, jobCombo.getItemCount());
                jobCombo.setSelectedIndex(0);
                common.saveCacheData("selected", "jobType", jobType);
            }
        });
        //System.out.println(nowJobType);
        jobTypeCombo.setSelectedItem(nowJobType);
        // System.out.println(jobCombo.getItemCount());
        // 초기 타입 설정 후 리스너 적용
        jobCombo.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED){
                var job = (String) jobCombo.getSelectedItem();
                // System.out.println(job);
                common.saveCacheData("selected", "job", job);
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

    }

}
