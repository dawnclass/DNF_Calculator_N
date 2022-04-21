package org.dnf_calc_n.ui.sub;

import org.dnf_calc_n.Common;
import org.dnf_calc_n.data.LoadString;
import org.dnf_calc_n.ui.main.WindowMainNew;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

public class WindowUpdate extends JFrame {

    Common common = new Common();
    HashMap<String, Font> mapFont;
    JPanel panelUpdate;
    WindowUpdate window;

    String nowVersion;
    public WindowUpdate(String nowVersion, WindowMainNew root) {
        window = this;
        this.nowVersion = nowVersion;
        mapFont = common.loadFont();
        setResizable(false);
        setTitle(LoadString.strGet("패치노트"));
        setSize(400, 440);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(root);

        panelUpdate = new JPanel();
        panelUpdate.setLayout(null);
        panelUpdate.setBackground(new Color(50, 46, 52));
        panelUpdate.setBorder(new EmptyBorder(0, 0, 0, 0));
        panelUpdate.setSize(400, 500);
        setContentPane(panelUpdate);

        loadUpdate();
        makeButtonSection();
        panelUpdate.updateUI();
    }

    StringBuilder updateText;
    String latestVersion;
    private void loadUpdate(){
        updateText = new StringBuilder();
        updateText.append("<html>");
        String inputLine;
        try (InputStream in = new URL(
                "https://raw.githubusercontent.com/dawnclass/DNF_Calculator_N/master/update.txt"
        ).openStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            int i = 0;
            while((inputLine = br.readLine())!= null){
                if(i == 0) latestVersion = inputLine;
                updateText.append(inputLine).append("<br>");
                i++;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateText.append("</html>");
        // System.out.println(updateText);
    }

    public boolean isClientLatest(){
        int nowVer, latestVer;
        try{
            nowVer = Integer.parseInt(nowVersion.split("\\.")[2]);
            latestVer = Integer.parseInt(latestVersion.split("\\.")[2]);
        }catch (Exception e){
            nowVer = 0;
            latestVer = 1;
        }
        return nowVer >= latestVer;
    }

    private void makeButtonSection(){
        JLabel label = new JLabel();
        label.setText(updateText.toString());
        label.setFont(mapFont.get("normal"));
        label.setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(
                label, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBounds(1, 0, 394, 360);
        scroll.getViewport().setBackground(new Color(34, 32, 37));
        panelUpdate.add(scroll);

        int nowVer, latestVer;
        try{
            nowVer = Integer.parseInt(nowVersion.split("\\.")[2]);
            latestVer = Integer.parseInt(latestVersion.split("\\.")[2]);
        }catch (Exception e){
            nowVer = 0;
            latestVer = 1;
        }
        if(nowVer < latestVer){
            JButton btn = new JButton(LoadString.strGet("최신버전이 아닙니다. (업데이트 링크)"));
            btn.setFont(mapFont.get("bold"));
            btn.setBounds(0, 361, 395, 50);
            btn.setBackground(Color.LIGHT_GRAY);
            btn.setForeground(Color.BLACK);
            btn.setBorder(new EmptyBorder(0,0,0,0));
            btn.setVerticalAlignment(JLabel.CENTER);
            btn.addActionListener(e -> {
                try {
                    Desktop.getDesktop().browse(new URI(
                            "https://drive.google.com/file/d/1Ty3wTvHvfVQARwkbaHOleC9kSeyflcDn/view?usp=sharing"));
                }
                catch (IOException | URISyntaxException d) {
                    d.printStackTrace();
                }
                window.dispose();
            });
            panelUpdate.add(btn);
        }else{
            JButton btn = new JButton(LoadString.strGet("최신버전입니다. (다신 보지 않기)"));
            btn.setFont(mapFont.get("bold"));
            btn.setBounds(0, 361, 395, 50);
            btn.setBackground(Color.LIGHT_GRAY);
            btn.setForeground(Color.BLACK);
            btn.setBorder(new EmptyBorder(0,0,0,0));
            btn.setVerticalAlignment(JLabel.CENTER);
            btn.addActionListener(e -> {
                try {
                    JSONObject saveJson = common.loadJsonObject("cache/saved.json");
                    saveJson.put("patchNoShow", "1");
                    common.saveJson("cache/saved.json", saveJson);
                }catch (Exception d) {
                    d.printStackTrace();
                }
            });
            panelUpdate.add(btn);
        }




    }

}
