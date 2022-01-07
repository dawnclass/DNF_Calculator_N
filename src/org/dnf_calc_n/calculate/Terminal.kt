package org.dnf_calc_n.calculate

import javax.swing.JComboBox
import javax.swing.JTextField

class Terminal {

    init {
        println("계산 시작")
    }

    private val requireParts: Array<String> = arrayOf("11", "12", "13", "14", "15", "21", "22", "23", "31", "32", "33")
    private val items = HashMap<String, ArrayList<String>>()
    private val itemsNotDuplicated = ArrayList<String>()  // 단일 선택 모드일때
    fun getItemData(itemMap: HashMap<String, Boolean>) : Boolean {
        var isMyth = false
        for (key in itemMap.keys){
            if(itemMap[key] == true){
                itemsNotDuplicated.add(key)
                //무기제외
                if(key.length == 6){
                    try{
                        items["111"]!!.add(key)
                    }catch (e: NullPointerException){
                        items["111"] = ArrayList()
                        items["111"]!!.add(key)
                    }
                }else{
                    try{
                        items[key.substring(0, 2)]!!.add(key)
                    }catch (e: NullPointerException){
                        items[key.substring(0, 2)] = ArrayList()
                        items[key.substring(0, 2)]!!.add(key)
                    }
                    // 신화 중복 체크 여부 (단일 선택 모드일때만 작동하면 됨)
                    if(key.endsWith("1")){
                        if(isMyth){
                            return true
                        }else{
                            isMyth = true
                        }
                    }
                }
            }
        }
        for(part in requireParts){
            if(items[part] == null) return true
        }
        println(items.toString())
        return false
    }

    private val customs = HashMap<String, String>()
    fun getCustomData(
        comboBoxMap: HashMap<String, JComboBox<String>>, fieldMap: HashMap<String, JTextField>
    ): Boolean{
        try{
            for(key in comboBoxMap.keys){
                val nowCombo = comboBoxMap[key]
                customs[key] = nowCombo?.selectedItem as String
            }
            for(key in fieldMap.keys){
                val nowField = fieldMap[key]
                customs[key] = nowField?.text as String
            }
        }catch (e: NullPointerException){
            return true
        }
        println(customs.toString())
        return false
    }

    fun startCalculateSingle(): Boolean{

        return false
    }

}