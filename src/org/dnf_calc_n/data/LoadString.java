package org.dnf_calc_n.data;

import org.dnf_calc_n.Common;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.HashMap;

public class LoadString {

    public static HashMap<Object, String> strs = new HashMap<>();
    public static String strGet(Object key){
        if(strs.get(key) == null){
            return (String)key;
        }else{
            return strs.get(key);
        }
    }

    Common common = new Common();
    String setting = "ko";

    public LoadString() {
        loadLanguage();
    }

    private void loadLanguage(){
        try (FileInputStream in = new FileInputStream(
                    "translate/language.setting"
            )){
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                setting = br.readLine();
                br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            JSONObject nowJson = common.loadJsonObject("translate/" + setting + "/String.json");
            for(Object k : nowJson.keySet()){
                strs.put((String) k, (String)nowJson.get(k));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
