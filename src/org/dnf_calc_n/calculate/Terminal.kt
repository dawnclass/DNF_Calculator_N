package org.dnf_calc_n.calculate

import org.dnf_calc_n.Common
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.util.*
import java.util.function.ToDoubleFunction
import java.util.function.ToIntFunction
import javax.swing.JComboBox
import javax.swing.JTextField
import kotlin.math.roundToInt


class Terminal : Thread() {

    private var itemOptionJson : JSONObject
    private var itemNameJson : JSONObject
    private val common = Common()

    init {
        println("계산 시작")
        this.itemOptionJson = common.loadJsonObject("resources/data/item_option.json")
        this.itemNameJson = common.loadJsonObject("resources/data/item_name.json")
    }

    override fun run() {

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
        //println(items.toString())
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
        comboBoxMap: HashMap<String, JComboBox<String>>, fieldMap: HashMap<String, JTextField>,
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
            getEnvironmentData()
        }catch (e: NullPointerException){
            e.printStackTrace()
            return true
        }
        //println(customs.toString())
        return false
    }

    private lateinit var jobName : String
    private lateinit var jobJson : JSONObject
    private val jobActiveMap = HashMap<String, HashMap<String, Any>>()
    fun getJobData(): Boolean{
        val jobType = customs["jobType"] ?: return true
        val job = customs["job"] ?: return true
        jobName = "$jobType-$job"
        jobJson = common.loadJsonObject("resources/data/job/$jobType-$job.json") ?: return true
        val jobJsonActive = jobJson["active"] as JSONArray
        for(i in 0 until jobJsonActive.size){
            val active = jobJsonActive[i] as JSONObject
            if (active["notUse"] == 1.0){
                continue
            }
            val nowMap = HashMap<String, Any>()
            var talismanSlot = 0.0
            for(keyRow in active.keys){
                val key = keyRow as String
                if(key == "talisman"){
                    val jsonTalisman = (active[key] ?: continue) as JSONObject
                    talismanSlot = (jsonTalisman["slot"] ?: continue) as Double
                }else if(key == "gapLv"){
                    nowMap[key] = ((active[key] ?: continue) as Number).toInt()
                }else{
                    nowMap[key] = (active[key] ?: continue) as Any
                }
            }
            if(talismanSlot != 0.0){
                val jsonTalisman = active["talisman"] as JSONObject
                for(keyRow in jsonTalisman.keys){
                    val key = keyRow as String
                    nowMap[key] = (jsonTalisman[key] ?: continue) as Any
                }
            }
            val name = nowMap["name"] as String
            jobActiveMap[name] = nowMap
        }

        //println(jobActiveMap)
        return false
    }

    private val maxValueTag = arrayOf("D", "CD")
    private val simpleSumTag = arrayOf("DA","CDA","AD","AED","TD","A","AP","S","SP","E","DD","CR","AS","MS")
    private val simpleIndexSumTag = arrayOf("LVL","CRD")
    private val complexSumTag = arrayOf("SD")
    private val complexIndexSumTag = arrayOf("LVD", "CTD")
    var weaponType : String = "공통"
    var optionValueMap = HashMap<String, Double>()
    var optionArrayMap = HashMap<String, Array<Double>>()
    fun combineItemSingle(): Boolean{
        addSetOptionSingle()
        itemsNotDuplicated.add("111002") // 바드나후
        // println("itemsNotDuplicated = $itemsNotDuplicated")

        optionValueMap = HashMap<String, Double>()
        for(opt in simpleSumTag + complexSumTag + maxValueTag) optionValueMap[opt] = 0.0
        optionArrayMap = HashMap<String, Array<Double>>()
        for(opt in simpleIndexSumTag + complexIndexSumTag) optionArrayMap[opt] = Array<Double>(19) { 0.0 }

        for (item in itemsNotDuplicated){
            // println(item)
            val itemJson = (itemOptionJson[item] ?: continue) as JSONObject
            if(item.length == 6){
                weaponType = try{
                    val nameJson = itemNameJson[item] as JSONObject
                    nameJson["part"] as String
                }catch (e: NullPointerException){"공통"}
            }

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
        //optionValueMap.forEach { (t, v) ->  println("[$t] ${Common.convertCodeToExplain(t)} = $v")}
        //optionArrayMap.forEach { (t, v) ->  println("optionArrayMap[$t] = ${v.contentToString()}")}
        return false
    }

    private val atkStandard = 5000.0
    private val statStandard = 60000.0
    private val statMultiple = 3.31
    private val elementBase = 13.0 + 15 + 30 * 3 + 30 + 6 + 25 + 7 + 60 - 50

    private var damageRatio = 1.0
    private val finalCoolRatioArray = Array<Double>(19) {1.0}
    fun startCalculationSingle(): Boolean{
        try{
            val atk = (atkStandard + optionValueMap["A"]!!) * (optionValueMap["AP"]!!/100 + 1)
            val atkRatio = atk / atkStandard
            val stat = (statStandard + optionValueMap["S"]!! * statMultiple) * (optionValueMap["SP"]!!/100 + 1)
            val statRatio = (stat / 250 + 1) / (statStandard / 250 + 1)
            val element = elementBase + optionValueMap["E"]!!
            val elementRatio = (1.05 + 0.0045 * element) / 1.05

            damageRatio = ((optionValueMap["D"]!! + optionValueMap["DA"]!!) / 100 + 1) *
                    ((optionValueMap["CD"]!! + optionValueMap["CDA"]!!) / 100 + 1) *
                    ((optionValueMap["AD"]!! + optionValueMap["AED"]!! * elementRatio) / 100 + 1) *
                    (optionValueMap["TD"]!! / 100 + 1) * elementRatio *
                    (optionValueMap["DD"]!! / 100 + 1) *
                    (optionValueMap["SD"]!! / 100 + 1) * statRatio * atkRatio
            // println("damageRatio = $damageRatio")

            for(i in 0 until 19){
                finalCoolRatioArray[i] = 1.0 / (1.0 + optionArrayMap["CRD"]!![i] / 100.0) *
                        (1.0 + optionArrayMap["CTD"]!![i] / 100.0)
            }
            // println("finalCoolRatioArray = ${finalCoolRatioArray.contentToString()}")
        }catch (e: NullPointerException){
            e.printStackTrace()
            return true
        }
        return false
    }

    private lateinit var jobFinalResultMap : HashMap<String, HashMap<String, Any>>
    private val standardDamageRatio = 383.2
    private val levelingEfficiency = arrayOf(0.0, 0.1, 0.1015, 0.1593, 0.0, 0.2319)
    private val levelingIndex: Array<Double> = arrayOf(
        1.0, 5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 48.0, 50.0, 60.0, 70.0, 75.0, 80.0, 85.0, 95.0, 100.0)
    fun calculateJobDamageData(): Boolean{
        val itemList = itemsNotDuplicated
        val cDamageRatio = damageRatio / standardDamageRatio
        jobFinalResultMap = this.jobActiveMap.clone() as HashMap<String, HashMap<String, Any>>
        val jobSkillRatioMap = HashMap<String, HashMap<String, Double>>()
        jobSkillRatioMap["DAMAGE"] = HashMap()
        jobSkillRatioMap["COOL"] = HashMap()
        // 패시브
        val jobPassiveJson = jobJson["passive"] as JSONArray
        for(i in 0 until jobPassiveJson.size){
            val nowPassive = jobPassiveJson[i] as JSONObject
            var isCondition = true
            val requireType = nowPassive["requirementType"] as String?
            if(requireType != null){
                val requireValue = nowPassive["requirementValue"] as String
                when(requireType){
                    "WEAPON" -> {
                        if(weaponType == "공통") {
                            if(requireValue.endsWith("!")) isCondition = false
                        }else if(weaponType != requireValue) isCondition = false
                    }
                    "EQUIPMENT" -> {
                        if(!itemList.contains(requireValue)) isCondition = false
                    }
                }
            }
            if(!isCondition) continue
            val type = nowPassive["type"] as String
            val upLv = optionArrayMap["LVL"]!![levelingIndex.indexOf(nowPassive["requireLv"])]
            val nowValue = nowPassive["maxValue"] as Double
            val ratio = if(type=="DAMAGE"){
                (100.0 + (nowPassive["upValue"] as Double * upLv + nowValue)) / (100.0 + nowValue)
            }else{
                (100.0 - (nowPassive["upValue"] as Double * upLv + nowValue)) / (100.0 - nowValue)
            }
            val apply = nowPassive["target"] as String
            if(apply == "ALL"){
                for(name in jobActiveMap.keys) {
                    jobSkillRatioMap[type]!![name] = ratio * (jobSkillRatioMap[type]!![name] ?: 1.0)
                }
            }else{
                val applyList = apply.split("^")
                for(name in applyList){
                    jobSkillRatioMap[type]!![name] = ratio * (jobSkillRatioMap[type]!![name] ?: 1.0)
                }
            }
        }
        // println("jobSkillRatioMap=$jobSkillRatioMap")
        // 특수
        val jsonSpecial = jobJson["special"] as JSONArray
        for(i in 0 until jsonSpecial.size){
            val nowSpecial = jsonSpecial[i] as JSONObject
            var isCondition = true
            val requireType = nowSpecial["requirementType"] as String
            if(requireType != "ALL"){
                val requireValue = nowSpecial["requirementValue"] as String
                when(requireType){
                    "WEAPON" -> {
                        if(weaponType == "공통") {
                            if(requireValue.endsWith("!")) isCondition = false
                        }else if(weaponType != requireValue) isCondition = false
                    }
                    "EQUIPMENT" -> {
                        if(!itemList.contains(requireValue)) isCondition = false
                    }
                }
            }
            if(!isCondition) continue
            val type = nowSpecial["type"] as String
            val value = nowSpecial["value"] as Double
            val ratio = if(type == "DAMAGE"){
                1.0 + value / 100.0
            }else{
                1.0 - value / 100.0
            }
            val apply = nowSpecial["target"] as String
            if(apply == "ALL"){
                for(name in jobActiveMap.keys) {
                    jobSkillRatioMap[type]!![name] = ratio * (jobSkillRatioMap[type]!![name] ?: 1.0)
                }
            }else{
                val applyList = apply.split("^")
                for(name in applyList){
                    jobSkillRatioMap[type]!![name] = ratio * (jobSkillRatioMap[type]!![name] ?: 1.0)
                }
            }
        }
        // println("jobSkillRatioMap=$jobSkillRatioMap")
        // 무기
        var weaponDamageRatio = 1.0
        var weaponCoolTimeRatio = 1.0
        val jsonWeapon = jobJson["weapon"] as JSONArray
        for(i in 0 until jsonWeapon.size){
            val nowWeapon = jsonWeapon[i] as JSONObject
            if(nowWeapon["type"] == weaponType){
                weaponDamageRatio = nowWeapon["damage"] as Double
                weaponCoolTimeRatio = nowWeapon["coolTime"] as Double
                break
            }
        }
        // 액티브
        jobFinalResultMap.forEach { (name, activeMap) ->
            val index = levelingIndex.indexOf(activeMap["requireLv"])
            val nowLv = activeMap["nowLv"] as Double + optionArrayMap["LVL"]!![index]
            var nowDamage = activeMap["damage"] as Double * (1+ activeMap["nowTp"] as Double) *
                    (1 + (nowLv - 1) * levelingEfficiency[activeMap["gapLv"] as Int]) *
                    (1 + (optionArrayMap["LVD"]!![index]) / 100.0)  * cDamageRatio *
                    (jobSkillRatioMap["DAMAGE"]!![name] ?: 1.0) * weaponDamageRatio
            var nowCoolTime = activeMap["coolTime"] as Double * finalCoolRatioArray[index] *
                    (jobSkillRatioMap["COOL"]!![name] ?: 1.0) * weaponCoolTimeRatio * envCoolDown
            if(itemList.contains("15140") && activeMap["slot"] != null){
                when(activeMap["slot"]){
                    1.0 -> {
                        nowDamage *= 1.55
                        nowCoolTime *= 0.7
                    }
                    2.0 -> {
                        nowDamage *= 1.45
                        nowCoolTime *= 0.75
                    }
                }
            }
            nowDamage = nowDamage.roundToInt() * 1.0
            nowCoolTime = (nowCoolTime * 10).roundToInt() / 10.0
            if(activeMap["coolFix"] == 1.0) nowCoolTime = activeMap["coolTime"] as Double
            activeMap["damage"] = nowDamage
            activeMap["coolTime"] = nowCoolTime
            // println("name=$name, damage=${nowDamage.toInt()}, coolTime=$nowCoolTime")
        }
        println(jobFinalResultMap)
        return false
    }

    lateinit var damageTranArray : Array<Double>
    fun tranJobDamage(): Boolean{
        //데미지 순으로 정렬
        val num = jobFinalResultMap.size
        val valueList = Array<Array<Double>>(num){Array<Double>(10){0.0} }
        var index = 0
        jobFinalResultMap.forEach { (name, activeMap) ->
            valueList[index][0] = activeMap["damage"] as Double
            valueList[index][1] = activeMap["coolTime"] as Double
            valueList[index][2] = activeMap["delay"] as Double
            valueList[index][3] = activeMap["reality"] as Double
            valueList[index][4] = 0.0  // 잔여 쿨타임
            valueList[index][5] = (activeMap["stack"] as Double?) ?: 1.0  // 최대 스택
            valueList[index][6] = valueList[index][5]  // 현재 스택
            valueList[index][7] = (activeMap["duration"] as Double?) ?: 0.0  // 지속 시간
            valueList[index][8] = 0.0  // 현재 지속시간
            valueList[index][9] = (activeMap["endCool"] as Double?) ?: 0.0  // 끝난후 쿨타임 여부
            index ++
        }
        Arrays.sort(valueList, Comparator.comparingDouble { o1: Array<Double> -> -o1[0] })
        // valueList.forEach { e -> println(e.contentToString()) }

        damageTranArray = Array<Double>(maxTime){0.0}
        var finalDamage = 0.0
        val buffTimeMap = HashMap<String, Double>()
        var cannotTime = 0.0
        for(time in 0 until maxTime){
            damageTranArray[time] = finalDamage
            //시간 지남
            cannotTime -= 0.1
            for(arr in valueList){
                arr[4] -= 0.1
                arr[8] -= 0.1
                if(arr[4] <= 0 && arr[6] < arr[5]){
                    arr[6] = arr[6] + 1
                    arr[4] = arr[1]
                }
                if(arr[8] > 0){
                    finalDamage += arr[0] / arr[7] / 10.0 * tranDamageRatioArray[time][0]
                }
            }
            //딜레이 판정
            if(cannotTime > 0 || tranDamageRatioArray[time][2]==0.0) continue
            //데미지 가능 시
            for(arr in valueList){
                if(arr[6] >= 1){
                    arr[6] = arr[6] - 1
                    if(arr[7] == 0.0){
                        finalDamage += arr[0] * tranDamageRatioArray[time][0]
                    }else{
                        arr[8] = arr[7]
                    }
                    cannotTime = arr[2] + (4-arr[3]) * tranDamageRatioArray[time][1] * proficiency
                    arr[4] = arr[1] + cannotTime * arr[9]
                    break
                }
            }
        }

        println(damageTranArray.contentToString())
        // common.writeCSVFile(jobName, damageTranArray)
        return false
    }

    private lateinit var tranDamageRatioArray : Array<Array<Double>>
    private var proficiency = 0.5
    private var maxTime = 1800
    private var envCoolDown = 1.0
    private fun getEnvironmentData(){
        val env = customs["environment"]
        proficiency = 1.0 - (customs["proficiency"] ?: "0.5").toDouble()
        val environmentJson = common.loadJsonObject("resources/data/environment.json") as JSONObject
        val nowEnv = environmentJson[env] as JSONObject
        maxTime = (nowEnv["maxTime"] as Number).toInt()
        tranDamageRatioArray = Array(maxTime){arrayOf()}
        envCoolDown = nowEnv["coolDown"] as Double
        val envJsonArray = nowEnv["tran"] as JSONArray
        for(i in 0 until envJsonArray.size){
            val nowTimeJson = envJsonArray[i] as JSONObject
            val timeArray = (nowTimeJson["time"] as String).split("-")
            val damageRatio = nowTimeJson["damage"] as Double
            val mobAttack = nowTimeJson["mobAttack"] as Double
            val skillAllowed = if(nowTimeJson["skillAllowed"] as Boolean){1.0}else{0.0}
            for(j in timeArray[0].toInt() until timeArray[1].toInt()){
                tranDamageRatioArray[j] = arrayOf(damageRatio, mobAttack, skillAllowed)
            }
        }
        // for((index, tran) in tranDamageRatioArray.withIndex()) println("$index="+tran.contentToString())
    }

}