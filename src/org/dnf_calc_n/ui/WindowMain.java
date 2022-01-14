package org.dnf_calc_n.ui;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.calculate.Terminal;
import org.dnf_calc_n.data.LoadImage;
import org.dnf_calc_n.data.LoadJob;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.border.LineBorder;

public class WindowMain extends JFrame {

    HashMap<String, Font> fontMap;

    JPanel mainPanel;
    HashMap<String, JButton> btnMap = new HashMap<>();
    HashMap<String, JComboBox<String>> comboBoxMap = new HashMap<>();
    HashMap<String, JTextField> fieldMap = new HashMap<>();
    HashMap<String, ImageIcon> itemImgMap;
    HashMap<String, ImageIcon> extraImgMap;
    HashMap<String, Boolean> itemToggleMap = new HashMap<>();

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                WindowMain frame = new WindowMain();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("오류");
            }
        });
    }

    static Double[] resultGraph;
    public WindowMain() {
        var loadImage = new LoadImage();
        itemImgMap = loadImage.loadAllImageItem();
        extraImgMap = loadImage.loadAllImageExtra();
        fontMap = Common.loadFont();
        setResizable(false);
        setTitle("에픽조합계산기 2.0.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1024, 720);
        mainPanel = new JPanel();
        mainPanel.setBackground(new Color(34, 32, 37));
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPanel);
        mainPanel.setLayout(null);

        createItemButtonSection(); // 아이템 버튼 생성
        createJobSection(); // 직업 영역 생성
        createCustomSection(); // 커스텀 영역 생성 (직업과 패널 공유)
        createResultSection(); // 결과 영역 생성

        var calc_btn = new JButton();
        calc_btn.setBackground(new Color(34, 32, 37));  // 배경색과 동일
        calc_btn.setIcon(extraImgMap.get("calc"));
        calc_btn.setBounds(420, 10, 111, 51);
        calc_btn.addActionListener(e -> {
            Terminal calcTerminal = new Terminal();
            Thread calcThread = new Thread(() -> {
                if(calcTerminal.getItemData(itemToggleMap)) {
                    System.out.println("오류: 미선택 부위 있음 or 신화 중복 체크");
                    showErrorDialog("미선택 부위 있음 or 신화 중복 체크");
                    return;
                }
                if(calcTerminal.getCustomData(comboBoxMap, fieldMap)){
                    System.out.println("오류: 커스텀 입력값 오류");
                    showErrorDialog("커스텀 입력값 오류");
                    return;
                }
                if(calcTerminal.getJobData()){
                    System.out.println("오류: 직업 데이터 누락");
                    showErrorDialog("직업 데이터 누락");
                    return;
                }
                if(calcTerminal.combineItemSingle()){
                    System.out.println("오류: 데이터베이스 오류");
                    showErrorDialog("데이터베이스 오류");
                    return;
                }
                if(calcTerminal.startCalculationSingle()){
                    System.out.println("오류: 계산 오류");
                    showErrorDialog("계산 오류");
                    return;
                }
                if(calcTerminal.calculateJobDamageData()){
                    System.out.println("오류: 직업 스킬별 데미지 계산 오류");
                    showErrorDialog("직업 스킬별 데미지 계산 오류");
                    return;
                }
                if(calcTerminal.tranJobDamage()){
                    System.out.println("오류: 시간별 데미지 그래프 계산 오류");
                    showErrorDialog("시간별 데미지 그래프 계산 오류");
                    return;
                }
                resultGraph = calcTerminal.damageTranArray;
                resultPanel.addTranArray(resultGraph);
                resultPanel.repaint();
            });
            calcThread.start();
        });
        mainPanel.add(calc_btn);

    }

    JPanel resultSection;
    ResultPanel resultPanel;
    private void createResultSection(){
        resultSection = new JPanel();
        resultSection.setBackground(new Color(50, 46, 52));
        resultSection.setLayout(null);
        resultSection.setBounds(420, 215, 550, 355);
        mainPanel.add(resultSection);

        resultPanel = new ResultPanel();
        resultPanel.setBounds(25, 25, 400, 300);
        resultSection.add(resultPanel);

        var btnResetTran = new JButton();
        btnResetTran.setBackground(new Color(34, 32, 37));  // 배경색과 동일
        btnResetTran.setIcon(extraImgMap.get("reset"));
        btnResetTran.setBounds(432, 25, 111, 30);
        btnResetTran.addActionListener(e -> {
            resultPanel.resetTranArray();
            resultPanel.repaint();
        });
        resultSection.add(btnResetTran);
    }

    class ResultPanel extends JPanel{
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN};
        ArrayList<Double[]> tranArrayList = new ArrayList<>();
        public void addTranArray(Double[] tranArray) {
            if(tranArrayList.size() == 3) {
                showErrorDialog("표시 그래프 제한 초과");
            }else{tranArrayList.add(tranArray);}
        }
        public void resetTranArray() {tranArrayList.clear();}
        public void paint(Graphics g) {
            g.clearRect(0, 0, getWidth(), getHeight());
            g.drawLine(25, 275, 375, 275);
            g.drawLine(25, 275, 25, 25);
            g.drawString("데미지", 25, 15);
            g.drawString("시간(초)", 351, 268);

            int max_x =0;
            double max_y = 0;
            for(Double[] array : tranArrayList){
                int now_x = array.length;
                double now_y = array[now_x-1];
                if (now_x > max_x) max_x = now_x;
                if (now_y > max_y) max_y = now_y;
            }
            g.drawString("0", 25, 290);
            g.drawString(String.valueOf(max_x/20), 195, 290);
            g.drawString(String.valueOf(max_x/10), 365, 290);
            g.drawString((int)(max_y/2000000)+"M", 28, 150);
            g.drawString((int)(max_y/1000000)+"M", 28, 28);
            for(int i=0;i<tranArrayList.size();i++){
                if(i==3) break;
                var nowTranArray = tranArrayList.get(i);
                g.setColor(colors[i]);
                for(int j=0;j<350;j++){
                    var nowDamage = nowTranArray[max_x/350*j];
                    int now_y = (int)(275 - (nowDamage / max_y) * 250);
                    g.drawRect(j+25, now_y, 2, 2);
                }
            }
        }
    }

    private void showErrorDialog(String error){
        JLabel label = new JLabel(error);
        label.setFont(fontMap.get("normal"));
        JOptionPane.showMessageDialog(null, label, "오류", JOptionPane.ERROR_MESSAGE);
    }


    private void createCustomSection(){
        var cacheJson = Common.loadJsonObject("cache/selected.json");
        var jsonWidget = Common.loadJsonObject("resources/ui_layout/widget.json");
        var jsonCombo = (JSONArray) jsonWidget.get("JComboBox");
        var jsonField = (JSONArray) jsonWidget.get("JTextField");
        JSONArray jsonAdded = new JSONArray();
        jsonAdded.addAll(jsonField);
        jsonAdded.addAll(jsonCombo);
        for (Object ob : jsonAdded) {
            var nowSetting = (JSONObject) ob;
            var text = (String) nowSetting.get("text");
            var tag = (String) nowSetting.get("tag");
            var position = (JSONArray) nowSetting.get("position");
            var position_x = ((Number) position.get(0)).intValue();
            var position_y = ((Number) position.get(1)).intValue();
            var width = ((Number) position.get(2)).intValue();

            var nowLabel = new JLabel();
            nowLabel.setText(" " + text);
            nowLabel.setFont(fontMap.get("bold"));
            nowLabel.setForeground(Color.WHITE);
            nowLabel.setBounds(position_x, position_y, 60, 18);
            customPanel.add(nowLabel);
            var nowLabel2 = new JLabel();
            nowLabel2.setText(":");
            nowLabel2.setFont(fontMap.get("bold"));
            nowLabel2.setForeground(Color.WHITE);
            nowLabel2.setBounds(position_x + 55, position_y, 10, 18);
            customPanel.add(nowLabel2);

            if(jsonCombo.contains(ob)){
                var list = (JSONArray) nowSetting.get("list");
                var nowList = new String[list.size()];
                for (int j = 0; j < nowList.length; j++) nowList[j] = (String) list.get(j);
                var nowCombo = new JComboBox<>(nowList);
                nowCombo.setBounds(position_x + 70, position_y, width, 20);
                nowCombo.setFont(fontMap.get("normal"));
                String cacheValue;
                cacheValue = (String) cacheJson.get(tag);
                // System.out.println(tag + cacheValue);
                nowCombo.setSelectedItem(cacheValue);
                nowCombo.addItemListener(e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        Common.saveCacheData("selected", tag, (String) nowCombo.getSelectedItem());
                    }
                });
                comboBoxMap.put(tag, nowCombo);
                customPanel.add(nowCombo);
            }else{
                var nowField = new JTextField();
                nowField.setBounds(position_x + 70, position_y, width, 20);
                nowField.setFont(fontMap.get("normal"));
                String cacheValue;
                cacheValue = (String) cacheJson.get(tag);
                nowField.setText(cacheValue);
                nowField.addKeyListener(new KeyListener() {
                    @Override
                    public void keyTyped(KeyEvent e) {}
                    @Override
                    public void keyPressed(KeyEvent e) {}
                    @Override
                    public void keyReleased(KeyEvent e) {
                        Common.saveCacheData("selected", tag, (String) nowField.getText());
                    }
                });
                fieldMap.put(tag, nowField);
                customPanel.add(nowField);
            }
        }
    }

    JPanel customPanel;
    HashMap<String, String[]> jobMap;
    private void createJobSection(){
        customPanel = new JPanel();
        customPanel.setBackground(new Color(50, 46, 52));
        customPanel.setLayout(null);
        customPanel.setBounds(420, 70, 550, 135);
        mainPanel.add(customPanel);
        var json = Common.loadJsonObject("cache/selected.json");

        var nowLabel = new JLabel();
        nowLabel.setText(" 직업");
        nowLabel.setFont(fontMap.get("bold"));
        nowLabel.setForeground(Color.WHITE);
        nowLabel.setBounds(10, 10, 60, 18);
        customPanel.add(nowLabel);
        var nowLabel2 = new JLabel();
        nowLabel2.setText(":");
        nowLabel2.setFont(fontMap.get("bold"));
        nowLabel2.setForeground(Color.WHITE);
        nowLabel2.setBounds(65, 10, 10, 18);
        customPanel.add(nowLabel2);

        LoadJob loadJob = new LoadJob();
        jobMap = loadJob.getJobMap();
        // System.out.println(jobMap.toString());
        String[] jobTypes = jobMap.get("types");
        // System.out.println(Arrays.toString(jobTypes));

        var jobCombo = new JComboBox<>(jobMap.get("귀검사(남)"));
        jobCombo.setBounds(190, 10, 100, 20);
        jobCombo.setFont(fontMap.get("normal"));

        var jobTypeCombo = new JComboBox<>(jobTypes);
        jobTypeCombo.setBounds(80, 10, 100, 20);
        jobTypeCombo.setFont(fontMap.get("normal"));
        jobTypeCombo.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED){
                var jobType = (String) jobTypeCombo.getSelectedItem();
                // System.out.println(jobType);
                var jobArray = jobMap.get(jobType);
                // System.out.println(Arrays.toString(jobArray));
                jobCombo.removeAllItems();
                for(String job : jobArray) jobCombo.insertItemAt(job, jobCombo.getItemCount());
                jobCombo.setSelectedIndex(0);
                Common.saveCacheData("selected", "jobType", jobType);
            }
        });
        String nowJobType = (String) json.get("jobType");
        // System.out.println(nowJobType);
        jobTypeCombo.setSelectedItem(nowJobType);
        // System.out.println(jobCombo.getItemCount());
        // 초기 타입 설정 후 리스너 적용
        jobCombo.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED){
                var job = (String) jobCombo.getSelectedItem();
                // System.out.println(job);
                Common.saveCacheData("selected", "job", job);
            }
        });
        String nowJob = (String) json.get("job");
        // System.out.println(nowJob);
        jobCombo.setSelectedItem(nowJob);
        customPanel.add(jobTypeCombo);
        customPanel.add(jobCombo);
        comboBoxMap.put("job", jobCombo);
        comboBoxMap.put("jobType", jobTypeCombo);
    }

    JPanel itemPanel;
    private void createItemButtonSection(){  // 아이템 버튼 생성 메소드
        itemPanel = new JPanel();
        itemPanel.setBackground(new Color(50, 46, 52));
        itemPanel.setLayout(null);
        itemPanel.setBounds(10, 70, 400, 500);
        mainPanel.add(itemPanel);
        var json = Common.loadJsonObject("resources/ui_layout/item.json");

        var jsonPosition = (JSONArray) json.get("position");
        var start_x = ((Number) jsonPosition.get(0)).intValue();
        var start_y = ((Number) jsonPosition.get(1)).intValue();

        var jsonItem = (JSONArray) json.get("items");
        for (Object o : jsonItem) {
            var nowItem = (JSONObject) o;
            // System.out.println(nowItem.toString());
            var code = (String) nowItem.get("code");
            var position = (JSONArray) nowItem.get("position");
            var position_y = ((Number) position.get(0)).intValue();
            var position_x = ((Number) position.get(1)).intValue();

            var btnNow = new JButton();
            itemToggleMap.put(code, false);
            btnNow.setBackground(new Color(34, 32, 37));  // 배경색과 동일
            int[] borderRGB;
            if(!code.endsWith("1")){  // 테두리 색
                borderRGB = new int[]{102, 80, 0, 255, 200, 0};
            }else{
                borderRGB = new int[]{102, 0, 80, 255, 0, 200};
            }
            btnNow.setBorder(new LineBorder(new Color(borderRGB[0], borderRGB[1], borderRGB[2]), 1));
            btnNow.setIcon(Common.changeBright(mainPanel, itemImgMap.get(code), 0.4));
            btnNow.addActionListener(e -> {
                toggleItemButton(code, btnNow, true, borderRGB);  // 중복체크 금지
                    /*  중복 체크 허용시
                    if (itemToggleMap.get(code)) {
                        btnNow.setBorder(new LineBorder(new Color(RGB[0], RGB[1], RGB[2]), 1));
                        btnNow.setIcon(Common.changeBright(btnNow, itemImgMap.get(code), 0.4));
                    } else {
                        btnNow.setBorder(new LineBorder(new Color(RGB[3], RGB[4], RGB[5]), 1));
                        btnNow.setIcon(itemImgMap.get(code));
                    }
                    itemToggleMap.put(code, !itemToggleMap.get(code));
                    */
                // System.out.println(code + " = " + itemToggleMap.get(code));
            });
            btnNow.setBounds(
                    start_x + 30 * position_x, start_y + 30 * position_y, 28, 28
            );  // 좌표와 크기
            btnMap.put(code, btnNow);
            itemPanel.add(btnMap.get(code));
        }
    }

    // 중복 체크를 금지할 경우
    HashMap<String, String> isPartExist = new HashMap<>();
    private void toggleItemButton(String code, JButton button, boolean isCreate, int[] RGB){
        var part = code.substring(0, 2);
        if(isCreate && isPartExist.get(part) != null){
            var existCode = isPartExist.get(part);
            if(!existCode.equals(code)){
                int[] borderRGB;
                if(!existCode.endsWith("1")){  // 테두리 색
                    borderRGB = new int[]{102, 80, 0, 255, 200, 0};
                }else{
                    borderRGB = new int[]{102, 0, 80, 255, 0, 200};
                }
                toggleItemButton(existCode, btnMap.get(existCode), false, borderRGB);
            }
        }
        if (itemToggleMap.get(code)) {
            button.setBorder(new LineBorder(new Color(RGB[0], RGB[1], RGB[2]), 1));
            button.setIcon(Common.changeBright(button, itemImgMap.get(code), 0.4));
            isPartExist.remove(part);
        } else {
            button.setBorder(new LineBorder(new Color(RGB[3], RGB[4], RGB[5]), 1));
            button.setIcon(itemImgMap.get(code));
            isPartExist.put(part, code);
        }
        itemToggleMap.put(code, !itemToggleMap.get(code));
        // System.out.println(isPartExist.toString());
    }


}
