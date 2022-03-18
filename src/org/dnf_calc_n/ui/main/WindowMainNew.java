package org.dnf_calc_n.ui.main;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.calculate.Buff;
import org.dnf_calc_n.data.LoadImage;
import org.dnf_calc_n.data.LoadJob;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;

public class WindowMainNew extends JFrame {

    Common common = new Common();
    Buff buff;
    HashMap<String, Font> mapFont;
    JSONObject equipmentData;

    JPanel mainPanel;
    PanelInfo panelInfo;
    PanelSelect panelSelect;
    PanelCustom panelCustom;
    PanelResult panelResult;

    JSONObject jsonCache;

    HashMap<String, JButton> mapWidgetBtn = new HashMap<>();
    HashMap<String, JComboBox<String>> mapWidgetCombo = new HashMap<>();
    HashMap<String, JTextField> mapWidgetField = new HashMap<>();

    HashMap<String, ImageIcon> mapIconItem;
    HashMap<String, ImageIcon> mapIconExtra;
    HashMap<String, Boolean> mapToggleItem = new HashMap<>();

    public static void main(String[] args) {
        // 시스템 텍스트 인코딩 지정
        System.setProperty("file.encoding","UTF-8");
        try{
            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null,null);
        }
        catch(Exception ignored){}
        EventQueue.invokeLater(() -> {
            try {
                WindowMainNew frame = new WindowMainNew();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("오류");
            }
        });
    }

    public WindowMainNew() {
        // 초기 데이터 로드
        var loadImage = new LoadImage();
        jsonCache = common.loadJsonObject("cache/selected.json");
        equipmentData = common.loadJsonObject("resources/data/equipment_data.json");
        buff = new Buff(equipmentData);
        mapIconItem = loadImage.loadAllImageItem();
        mapIconExtra = loadImage.loadAllImageExtra();
        mapFont = common.loadFont();
        setResizable(false);
        setTitle("에픽조합계산기N 0.1.1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1024, 720);
        mainPanel = new JPanel();
        mainPanel.setBackground(new Color(34, 32, 37));
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPanel);
        mainPanel.setLayout(null);

        // 영역 생성
        panelResult = new PanelResult(mainPanel);
        panelCustom = new PanelCustom(mainPanel, mapWidgetCombo);
        panelInfo = new PanelInfo(mainPanel, mapIconItem, mapIconExtra);
        panelSelect = new PanelSelect(
                mainPanel, panelResult,
                equipmentData, mapIconItem, panelInfo,
                buff
        );

    }


}