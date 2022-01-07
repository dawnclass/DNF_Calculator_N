package org.dnf_calc_n.calculate

import org.dnf_calc_n.Common
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.util.*
import javax.swing.JComboBox
import javax.swing.JTextField
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Terminal {

    private var itemOptionJson : JSONObject

    init {
        println("계산 시작")
        this.itemOptionJson = Common.loadJsonObject("resources/data/item_option.json")
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

    private fun addSetOptionSingle(){  //싱글모드일때 세트옵션 추가
        val codeMap = HashMap<String, Int>()
        for(item in itemsNotDuplicated){
            if (item.length == 6) continue
            val setCode = item.substring(2,4)
            try {
                codeMap[setCode] = codeMap[setCode]!! + 1
            }catch (e: NullPointerException){
                codeMap[setCode] = 1
            }
        }
        codeMap.forEach{ (code, number) ->
            itemsNotDuplicated.add("1$code${(number*0.7).toInt()}")
        }
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
            e.printStackTrace()
            return true
        }
        println(customs.toString())
        return false
    }

    private val maxValueTag = arrayOf("D", "CD")
    private val simpleSumTag = arrayOf("DA","CDA","AD","AED","TD","A","AP","S","SP","E","DD","CR","AS","MS")
    private val simpleIndexSumTag = arrayOf("LVL","CRD")
    private val complexSumTag = arrayOf("SD")
    private val complexIndexSumTag = arrayOf("LVD", "CTD")
    var optionValueMap = HashMap<String, Double>()
    var optionArrayMap = HashMap<String, Array<Double>>()
    fun combineItemSingle(): Boolean{
        addSetOptionSingle()
        itemsNotDuplicated.add("111002") // 바드나후
        println("itemsNotDuplicated = $itemsNotDuplicated")

        optionValueMap = HashMap<String, Double>()
        for(opt in simpleSumTag + complexSumTag + maxValueTag) optionValueMap[opt] = 0.0
        optionArrayMap = HashMap<String, Array<Double>>()
        for(opt in simpleIndexSumTag + complexIndexSumTag) optionArrayMap[opt] = Array<Double>(19) { 0.0 }

        for (item in itemsNotDuplicated){
            // println(item)
            val itemJson : JSONObject = (itemOptionJson[item] ?: continue) as JSONObject

            for (optAny in itemJson.keys){
                when (val opt = optAny as String) {
                    in simpleSumTag -> {
                        val value = itemJson[opt] as Double
                        optionValueMap[opt] = optionValueMap[opt]!! + value
                    }
                    in complexSumTag -> {
                        val value = itemJson[opt] as Double
                        optionValueMap[opt] = (1 + optionValueMap[opt]!!/100) * (1 + value/100) * 100 - 100
                    }
                    in simpleIndexSumTag -> {
                        val value = itemJson[opt] as JSONArray
                        value.forEachIndexed { i, v ->
                            optionArrayMap[opt]!![i] += v as Double
                        }
                    }
                    in complexIndexSumTag -> {
                        val value = itemJson[opt] as JSONArray
                        value.forEachIndexed { i, v ->
                            optionArrayMap[opt]!![i] =
                                (1 + optionArrayMap[opt]!![i]/100) * (1 + v as Double/100) * 100 - 100
                        }
                    }
                    in maxValueTag -> {
                        val value = itemJson[opt] as Double
                        if (optionValueMap[opt]!! < value){
                            optionValueMap[opt] = value
                        }
                    }
                }
            }
        }
        optionValueMap.forEach { (t, v) ->  println("[$t] ${Common.convertCodeToExplain(t)} = $v")}
        optionArrayMap.forEach { (t, v) ->  println("optionArrayMap[$t] = ${v.contentToString()}")}
        return false
    }

    private val atkStandard = 5000.0
    private val statStandard = 60000.0
    private val statMultiple = 3.31
    private val elementBase = 13.0 + 15 + 30 * 3 + 30 + 6 + 25 + 7 + 60 - 50
    fun startCalculationSingle(): Boolean{
        try{
            val damageRatio : Double

            val atk = (atkStandard + optionValueMap["A"]!!) * (optionValueMap["AP"]!!/100 + 1)
            val atkRatio = atk / atkStandard
            val stat = (statStandard + optionValueMap["S"]!! * statMultiple) * (optionValueMap["SP"]!!/100 + 1)
            val statRatio = (stat / 250 + 1) / (statStandard / 250 + 1)
            val element = elementBase + optionValueMap["E"]!!
            val elementRatio = 1.05 + 0.0045 * element

            damageRatio = ((optionValueMap["D"]!! + optionValueMap["DA"]!!) / 100 + 1) *
                    ((optionValueMap["CD"]!! + optionValueMap["CDA"]!!) / 100 + 1) *
                    ((optionValueMap["AD"]!! + optionValueMap["AED"]!! * elementRatio) / 100 + 1) *
                    (optionValueMap["TD"]!! / 100 + 1) * elementRatio *
                    (optionValueMap["DD"]!! / 100 + 1) *
                    (optionValueMap["SD"]!! / 100 + 1) * statRatio * atkRatio
            println("damageRatio = $damageRatio")

        }catch (e: NullPointerException){
            e.printStackTrace()
            return true
        }

        return false
    }

}