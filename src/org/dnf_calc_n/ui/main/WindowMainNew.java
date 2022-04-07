package org.dnf_calc_n.ui.main;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.calculate.Buff;
import org.dnf_calc_n.calculate.Damage;
import org.dnf_calc_n.data.LoadImage;
import org.dnf_calc_n.data.LoadJob;
import org.dnf_calc_n.ui.component.RoundButton;
import org.dnf_calc_n.ui.sub.WindowUpdate;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;

public class WindowMainNew extends JFrame {

    Common common = new Common();
    Buff buff;
    Damage damage;
    HashMap<String, Font> mapFont;
    JSONObject equipmentData;

    JPanel mainPanel;
    PanelInfo panelInfo;
    PanelSelect panelSelect;
    PanelCustom panelCustom;
    PanelResult panelResult;
    PanelCondition panelCondition;
    WindowUpdate windowUpdate;

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
        loadNowVersion();
        // 초기 데이터 로드
        LoadImage loadImage = new LoadImage();
        jsonCache = common.loadJsonObject("cache/selected.json");
        equipmentData = common.loadJsonObject("resources/data/equipment_data.json");
        buff = new Buff(equipmentData);
        damage = new Damage(equipmentData);
        mapIconItem = loadImage.loadAllImageItem();
        mapIconExtra = loadImage.loadAllImageExtra();
        mapFont = common.loadFont();
        setResizable(false);
        setTitle("에픽조합계산기N "+nowVersion);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 720);
        setLocationRelativeTo(null);
        mainPanel = new JPanel();
        mainPanel.setBackground(new Color(34, 32, 37));
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPanel);
        mainPanel.setLayout(null);

        // 영역 생성
        panelResult = new PanelResult(mainPanel);
        panelCondition = new PanelCondition(mainPanel, damage, buff, panelResult);
        panelInfo = new PanelInfo(mainPanel, mapIconItem, mapIconExtra);
        panelCustom = new PanelCustom(mainPanel, panelResult, panelInfo, panelCondition,
                mapWidgetCombo, buff, damage);
        panelSelect = new PanelSelect(
                mainPanel, panelResult, panelCondition,
                equipmentData, mapIconItem, panelInfo,
                buff, damage, mapWidgetCombo
        );

        JButton twip = new JButton();
        twip.setIcon(mapIconExtra.get("donate"));
        twip.setBounds(931, 489, 67, 56);
        twip.setBackground(new Color(34, 32, 37));
        twip.setBorder(new EmptyBorder(0,0,0,0));
        twip.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://twip.kr/dawnclass16"));
            }
            catch (IOException | URISyntaxException d) {
                d.printStackTrace();
            }
        });
        mainPanel.add(twip);

        RoundButton update = new RoundButton();
        update.setText("버전확인");
        update.setBounds(930, 560, 67, 56);
        update.setBackground(Color.LIGHT_GRAY);
        update.setFont(mapFont.get("normal_bold"));
        update.setHorizontalAlignment(JButton.CENTER);
        update.setForeground(Color.BLACK);
        update.addActionListener(e -> {
            try{
                windowUpdate.dispose();
            }catch (Exception ignored){}
            windowUpdate = new WindowUpdate(nowVersion);
            windowUpdate.setVisible(true);
        });
        mainPanel.add(update);

        JLabel maker = new JLabel("<html><body style='text-align:center;'>Made By<br>Dawnclass<br>(새벽반)</body></html>");
        maker.setForeground(Color.WHITE);
        maker.setBounds(931, 620, 67, 50);
        maker.setHorizontalAlignment(JLabel.CENTER);
        maker.setFont(mapFont.get("small"));
        mainPanel.add(maker);

        windowUpdate = new WindowUpdate(nowVersion);
        windowUpdate.setVisible(true);

    }

    String nowVersion = "1.0.0";
    private void loadNowVersion(){
        try {
            FileInputStream is = new FileInputStream("update.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            nowVersion = br.readLine();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(nowVersion);
    }


}
