package org.dnf_calc_n.calculate

import com.sun.xml.internal.fastinfoset.util.StringArray
import org.dnf_calc_n.Common
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.lang.IndexOutOfBoundsException
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.pow
import kotlin.math.round

class Damage(private var equipmentData: JSONObject, private var customData: JSONObject) {

    private lateinit var job : String
    private lateinit var arrayEquipment : Array<String>
    private var arrayCustomOption = ArrayList<String>()
    private val common = Common()
    //private var jobSkillData : JSONObject = common.loadJsonObject("")
    private var mapResult = HashMap<String, String>()

    private lateinit var arrayUpDamage : Array<Double>
    private var skillDamage = 1.0
    private var isBuffer = false

    private val damageCondition = DamageCondition()

    private fun resetData(){
        isBuffer = false
        mapResult.clear()
        arrayEquipment = Array(13){""}
        arrayCustomOption.clear()
        arrayUpDamage = Array(4){0.0}
        listArrayLeveling.clear()
        listArrayStatusResist.clear()
        listArraySpeed.clear()
        listArrayActiveLeveling.clear()
        listArrayCoolDown.clear()
        listArrayCoolRecover.clear()
        listArraySkillDamage.clear()
        listArrayElement.clear()
        arrayLeveling = Array<Double>(19){0.0}
        arrayActiveLeveling = Array<Double>(19){0.0}
        arrayCoolDown = Array<Double>(19){0.0}
        arrayCoolRecover = Array<Double>(19){0.0}
        arraySkillDamage = Array<Double>(19){1.0}
        skillDamage = 1.0
        totalDamage = 0.0
        simpleSumOptions.clear()
        statusTrans.clear()
        statusTotalDamage.clear()
        statusDamage.clear()
        statusAntiResist.clear()
        statusResist.clear()
        jsonConditionToggle.clear()
        jsonConditionArray.clear()
        jsonConditionGauge.clear()
        arrayCubeUse = arrayOf(0, 0, 0, 0, 0, 0, 0, 1, 1, 2, 0, 5, 2, 3, 3, 5, 10, 7, 15)
        isHPAlwaysLow = false
        isMythExist = false
        is100Exist = false
    }

    private fun resetCondition(){
        listArrayActiveLevelingCondition.clear()
        listArrayLevelingCondition.clear()
        listArrayStatusResistCondition.clear()
        listArraySpeedCondition.clear()
        listArrayCoolDownCondition.clear()
        listArrayCoolRecoverCondition.clear()
        listArraySkillDamageCondition.clear()
        listArrayElementCondition.clear()
        listArrayElementResistCondition.clear()
        statusDamageCondition.clear()
        totalDamageCondition = 0.0
        mapConditionSimpleSum.clear()
        totalMpConsumptionIncrease = 0.0
        additionalStat = 0.0
    }


    private lateinit var jobPassiveArray: JSONArray
    private lateinit var jsonSave: JSONObject
    private var extraAtkSpeed = 0.0

    fun startDamageCalculate(mapEquipment: HashMap<String,String>) : Boolean {
        resetData()
        jsonSave = common.loadJsonObject("cache/selected.json")
        job = "${(jsonSave["jobType"] ?: "?????????(???)") as String} ${(jsonSave["job"] ?: "???????????????") as String}"
        println("????????? ?????? = $job")
        if(job == "????????????(???) ???????????????" || job =="?????????(???) ???????????????"){
            isBuffer = true
        }
        extraAtkSpeed = try{
            (((jsonSave["atkSpeedExtra"] ?: "20") as String).toDouble())/100.0
        }catch (e: NumberFormatException){
            0.0
        }
        val optionLevelString = (jsonSave["optionLv"] ?: "60") as String
        optionLevel = when(optionLevelString){
            "20" -> 1.5497
            "40" -> 2.0885
            "50" -> 2.2917
            "60" -> 2.6088
            "70" -> 2.8597
            "80" -> 3.1106
            else -> 2.6088
        }
        optionMythLevel = when(optionLevelString){
            "20" -> 1.03*1.03
            "40" -> 1.03*1.03*1.03*1.03
            "50" -> 1.03*1.03*1.03*1.03*1.03
            "60" -> 1.03*1.03*1.03*1.03*1.03*1.03
            "70" -> 1.03*1.03*1.03*1.03*1.03*1.03*1.03
            "80" -> 1.03*1.03*1.03*1.03*1.03*1.03*1.03*1.03
            else -> 1.03*1.03*1.03*1.03
        }
        optionNot100Level = when(optionLevelString){
            "60" -> 1.01*1.01*1.01*1.01*1.01*1.01*1.01*1.01*1.01*1.01*1.01
            "70" -> 1.02*1.02*1.02*1.02*1.02*1.02*1.02*1.02*1.02*1.02*1.02
            "80" -> 1.03*1.03*1.03*1.03*1.03*1.03*1.03*1.03*1.03*1.03*1.03
            else -> 1.0
        }
        val jsonJob = common.loadJsonObject("resources/data/job_data.json")
        jobPassiveArray = ((jsonJob[job] ?: return false) as JSONObject)["passive"] as JSONArray

        mapEquipment.forEach { (k, v) ->
            val index = when(k){
                "77" -> 0
                "11" -> 1
                "12" -> 2
                "13" -> 3
                "14" -> 4
                "15" -> 5
                "21" -> 6
                "22" -> 7
                "23" -> 8
                "31" -> 9
                "32" -> 10
                "33" -> 11
                else -> 12
            }
            arrayEquipment[index] = v
        }
        loadEquipmentData()
        loadCustomOptionData()
        loadCustomData()
        return true
    }

    private fun loadCustomOptionData(){
        val jsonCustom = (jsonSave["customOption"] ?: return) as JSONObject
        for(equipment in arrayEquipment){
            if(jsonCustom[equipment] != null){
                val nowJsonArray = jsonCustom[equipment] as JSONArray
                nowJsonArray.forEach { v -> arrayCustomOption.add(v as String) }
            }
        }

        for(code in arrayCustomOption){
            val nowJson : JSONObject = (customData[code] ?: continue) as JSONObject

            val upDamage : Double = nowJson["??????"] as Double
            arrayUpDamage[0] += upDamage

            for(j in simpleSumKeys.indices){
                try{
                    val nowKey = simpleSumKeys[j]
                    val nowValue = nowJson[nowKey] as Double
                    simpleSumOptions[nowKey] = (simpleSumOptions[nowKey] ?: 0.0) + nowValue
                }catch (ignored: Exception){}
            }

            for(j in mapKey.indices){ // ????????? ?????? ????????????
                try{
                    val statusTransJson : JSONArray = nowJson[mapKey[j]] as JSONArray
                    val nowType = statusTransJson[0] as String
                    val nowValue = statusTransJson[1] as Double
                    mapValue[j][nowType] = (mapValue[j][nowType] ?: 0.0) + nowValue
                }catch (ignored: Exception){}
            }

            if(nowJson["?????????"] != null){
                val conditionJson = nowJson["?????????"] as JSONArray
                for(now in conditionJson){
                    combineConditions(code, now as JSONArray)
                }
            }
        }
    }

    private val customElementKey = arrayOf(
        "enchantWeapon", "enchantAccessory",
        "enchantMagic", "enchantTitle", "elementExtra"
    )
    private val customStatKey = arrayOf(
        "enchantArmorStat", "enchantShoulder", "enchantArmorCritical", "enchantSub", "enchantEarring"
    )
    private fun loadCustomData(){
        applyStat = 80000.0
        applyAtk = 10000.0
        titlePetPercent = 0.0
        pet2ndPassive = 0.0
        customElement = arrayOf(0.0, 0.0, 0.0, 0.0)
        if(isBuffer){
            applyStat = basicStatSoloBuffer
            applyAtk = basicAtkSolo
        }else{
            if(((jsonSave["party"] ?: "??????(??????O)") as String) == "??????(??????O)"){
                applyStat = basicStatBuff
                applyAtk = basicAtkBuff
            }else{
                applyStat = basicStatSolo
                applyAtk = basicAtkSolo
            }
        }
        println("applyStat = : $applyStat")
        println("applyAtk = : $applyAtk")

        when((jsonSave["title"] ?: "?????? 15%") as String){
            "?????? 15%"->titlePetPercent+=0.15
            "?????? 10%"->titlePetPercent+=0.1
            "?????? 32"->{
                for(i in 0 until 4) customElement[i] += 32.0
            }
        }
        when((jsonSave["creature"] ?: "?????? 18%") as String){
            "?????? 18%"->{
                titlePetPercent+=0.18
                pet2ndPassive+=1.0
            }
            "?????? 15%"->titlePetPercent+=0.15

        }

        for(key in customElementKey){
            val nowString = (jsonSave[key] ?: continue) as String
            if(nowString == "") continue
            val strArray = nowString.split(" ")
            if(strArray.size==1){
                for(i in 0 until 4) customElement[i] += strArray[0].toDouble()
            }else{
                val value = strArray[1].toDouble()
                val indexArray = ArrayList<Int>()
                if("???" in strArray[0]) indexArray.add(0)
                if("???" in strArray[0]) indexArray.add(1)
                if("???" in strArray[0]) indexArray.add(2)
                if("???" in strArray[0]) indexArray.add(3)
                if("??????" == strArray[0]) {
                    indexArray.add(0)
                    indexArray.add(1)
                    indexArray.add(2)
                    indexArray.add(3)
                }
                var multiValue = 1.0
                when(key){  // ?????? 2??? ?????????
                    "enchantAccessory" ->{
                        multiValue += 2.0 // ????????? ??????????????? 3??????
                        if(arrayEquipment.contains("21052")) multiValue += 1.0
                        if(arrayEquipment.contains("22052")) multiValue += 1.0
                        if(arrayEquipment.contains("23052")) multiValue += 1.0
                    }
                    "enchantMagic" -> if(arrayEquipment.contains("32052")) multiValue += 1.0
                }
                for(i in indexArray){
                    customElement[i] += value * multiValue
                }
            }

        }
        customStat = arrayOf( //?????? ??? ??????
            0.0, 0.0, 1.0)
        for(key in customStatKey){
            val nowCustomStat = arrayOf(0.0, 0.0, 1.0)
            val nowString = (jsonSave[key] ?: continue) as String
            if("/" in nowString){
                val strArray = nowString.split("/")
                nowCustomStat[1] += strArray[0].toDouble()
                nowCustomStat[0] += strArray[1].toDouble()
            }else{
                val strArray = nowString.split(" ")
                if(strArray[0] == "??????") nowCustomStat[0] += strArray[1].toDouble()
                if(strArray[0] == "???") nowCustomStat[1] += strArray[1].toDouble()
                if(strArray[0] == "??????") {
                    when(strArray[1]){
                        "2%" -> {
                            nowCustomStat[2] *= 1.02
                            nowCustomStat[1] += 10.0
                            nowCustomStat[0] += 40.0
                        }
                        "1%" -> {
                            nowCustomStat[2] *= 1.01
                            nowCustomStat[1] += 30.0
                        }
                    }
                }
            }
            var multiValue = 1.0
            when(key){
                "enchantArmorStat" -> {  // ?????????  2??????
                    multiValue += 1
                    if(arrayEquipment.contains("11052")) multiValue += 1.0
                    if(arrayEquipment.contains("12052")) multiValue += 1.0
                }
                "enchantShoulder" -> {
                    if(arrayEquipment.contains("13052")) multiValue += 1.0
                }
                "enchantArmorCritical" -> {  // ?????? 2??????
                    multiValue += 1
                    if(arrayEquipment.contains("14052")) multiValue += 1.0
                    if(arrayEquipment.contains("15052")) multiValue += 1.0
                }
                "enchantSub" -> {
                    if(arrayEquipment.contains("31052")) multiValue += 1.0
                }
                "enchantEarring" -> {
                    if(arrayEquipment.contains("33052")) multiValue += 1.0
                }
            }
            customStat[0] += nowCustomStat[0] * multiValue
            customStat[1] += nowCustomStat[1] * multiValue
            customStat[2] *= nowCustomStat[2].pow(multiValue)
        }
        this.skillDamage *= customStat[2]
    }

    private val simpleSumOptions = HashMap<String, Double>()
    private val simpleSumKeys = arrayOf(
        "??? ????????????", "??? ????????????", "??? ????????????", "??? ????????????",
        "??? ????????????", "??? ????????????", "??? ????????????", "??? ????????????",
        "??????", "??????", "??????", "??????", "??????", "??????", "??????",
        "HP MAX", "MP MAX", "HP???", "MP???", "??????", "??????", "??????", "MP?????????", "HP??????", "MP??????",
        "?????????", "??????", "?????????"
    )

    private val statusTrans = HashMap<String, Double>()  // ????????????
    private val statusTotalDamage = HashMap<String, Double>()  // ????????????
    private val statusDamage = HashMap<String, Double>()  // ???????????????
    private val statusAntiResist = HashMap<String, Double>()  // ???????????????
    private val statusResist = HashMap<String, Double>()  // ????????????
    private val mapKey = arrayOf(
        "????????????", "????????????", "???????????????", "??????????????????", "????????????"
    )
    private val mapValue = arrayOf(
        statusTrans, statusTotalDamage, statusDamage, statusAntiResist, statusResist
    )

    private fun loadEquipmentData(){
        if(arrayEquipment.contains("21212")) isHPAlwaysLow = true
        for(code in arrayEquipment){
            val nowJson : JSONObject = (equipmentData[code] ?: continue) as JSONObject

            if(code.length != 6 && code.substring(code.length-1) == "1") isMythExist = true
            if(code.length == 8) is100Exist = true

            val upDamage : JSONArray = nowJson["????????????"] as JSONArray
            for(i in 0 until upDamage.size){
                arrayUpDamage[i] += upDamage[i] as Double
            }

            val skillDamage = nowJson["??????"] as Double
            this.skillDamage = this.skillDamage * (skillDamage + 1)

            for(j in simpleSumKeys.indices){
                try{
                    val nowKey = simpleSumKeys[j]
                    val nowValue = nowJson[nowKey] as Double
                    simpleSumOptions[nowKey] = (simpleSumOptions[nowKey] ?: 0.0) + nowValue
                }catch (ignored: Exception){}
            }

            for(j in mapKey.indices){ // ????????? ?????? ????????????
                try{
                    val statusTransJson : JSONArray = nowJson[mapKey[j]] as JSONArray
                    val nowType = statusTransJson[0] as String
                    val nowValue = statusTransJson[1] as Double
                    mapValue[j][nowType] = (mapValue[j][nowType] ?: 0.0) + nowValue
                }catch (ignored: Exception){}
            }

            if(nowJson["?????????"] != null){
                val conditionJson = nowJson["?????????"] as JSONArray
                for(now in conditionJson){
                    combineConditions(code, now as JSONArray)
                }
            }
        }
        for(key in simpleSumKeys){
            if(simpleSumOptions[key] == null){
                simpleSumOptions[key] = 0.0
            }
        }
        arrayCubeUse = damageCondition.calculateCubeUse(arrayEquipment)
        // println(simpleSumOptions.toString())
    }

    private val levelIndex = arrayOf(
        "1", "5", "10", "15", "20", "25", "30", "35", "40", "45", "48", "50",
        "60", "70", "75", "80", "85", "95", "100"
    )

    private val listArrayActiveLeveling = ArrayList<JSONArray>()
    private val listArrayLeveling = ArrayList<JSONArray>()
    private val listArrayStatusResist = ArrayList<JSONArray>()
    private val listArraySpeed = ArrayList<JSONArray>()
    private val listArrayCoolDown = ArrayList<JSONArray>()
    private val listArrayCoolRecover = ArrayList<JSONArray>()
    private val listArraySkillDamage = ArrayList<JSONArray>()
    private val listArrayElement = ArrayList<JSONArray>()
    private val jsonConditionToggle = JSONArray()
    private val jsonConditionArray = JSONArray()
    private val jsonConditionGauge = JSONArray()
    private var isHPAlwaysLow = false
    private var isMythExist = false
    private var is100Exist = false

    private fun combineConditions(code: String, json: JSONArray){
        var reqType = json[0]
        val reqValue = json[1]
        val reqMulti = json[2]
        val upType = (json[3] ?: "") as String
        val upValue = (json[4] ?: 0.0) as Double

        var parsedUpValue : Any? = null
        var applyType : String = ""
        var isCondition = true

        if(upType=="????????????"){
            applyType = "????????????"
            parsedUpValue = upValue
        }else if(upType=="???????????????"){
            applyType = "???????????????"
            parsedUpValue = upValue
        }else if(upType=="??????" || upType=="?????????" || upType=="?????????%" || upType=="HP MAX" || upType=="MP MAX"
            || upType=="HP MAX%" || upType=="MP MAX%" || upType=="?????????" || upType=="?????????"
        ){
            applyType = upType
            parsedUpValue = upValue
        }else if(upType.contains("??????")){
            applyType = "????????????"
            parsedUpValue = JSONArray()
            damageCondition.parseStatusResistOption(upType, upValue).forEach { v -> (parsedUpValue as JSONArray).add(v)}
        }else if(upType.contains("??????")){
            applyType = "??????"
            parsedUpValue = JSONArray()
            damageCondition.parseSpeedOption(upType, upValue).forEach { v -> (parsedUpValue as JSONArray).add(v)}
        }else if(upType.contains("??????") || upType.contains("?????????") ||
            upType.contains("??????") || upType.contains("??????") || upType.contains("??????")){
            val strArray = upType.split(" ")
            parsedUpValue = JSONArray()
            applyType = strArray[strArray.size-1]
            if(applyType=="??????") applyType = "??????"
            damageCondition.parseLevelArrayOption(upType, upValue, arrayCubeUse).forEach { v -> (parsedUpValue as JSONArray).add(v)}
            if(applyType.contains("(???)")) applyType = applyType.replace("(???)", "")
            if(applyType.contains("(???X)")) applyType = applyType.replace("(???X)", "")
            // println("$code ,applyType = $applyType")
        }else if(upType.contains("????????????")){
            parsedUpValue = JSONArray()
            applyType = "????????????"
            damageCondition.parseElementOption(upType, upValue).forEach { v -> (parsedUpValue as JSONArray).add(v)}
        }else if(upType.contains("????????????")){
            parsedUpValue = JSONArray()
            applyType = "????????????"
            damageCondition.parseElementOption(upType, upValue).forEach { v -> (parsedUpValue as JSONArray).add(v)}
        }else if(upType.contains("?????????")){
            parsedUpValue = JSONArray()
            applyType = "???????????????"
            parsedUpValue.add(upType.split(" ")[0])
            parsedUpValue.add(upValue)
        }else if(upType.contains("????????? ??????")){
            applyType = "??????"
            val nowArray = arrayOf(
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.02, 0.02, 0.02, 0.0, 0.0, 0.02, 0.02,
                0.05, 0.05, 0.0, 0.05, 0.0
            )
            parsedUpValue = JSONArray()
            nowArray.forEach { v -> parsedUpValue.add(v * (1.0 + upValue)) }
        }else{
            isCondition = false
        }

        if(reqType == null){  // ???????????? ?????? ????????? ??????
            // ?????? ?????? ?????? ????????? ????????????
            try{
                when (applyType) {
                    "??????" -> {
                        simpleSumOptions["??????"] = ((simpleSumOptions["??????"] ?: 0.0) + (parsedUpValue ?: 0.0) as Double)
                        simpleSumOptions["??????"] = ((simpleSumOptions["??????"] ?: 0.0) + (parsedUpValue ?: 0.0) as Double)
                    }
                    "?????????" -> {
                        simpleSumOptions["??????"] = ((simpleSumOptions["??????"] ?: 0.0) + (parsedUpValue ?: 0.0) as Double)
                        simpleSumOptions["??????"] = ((simpleSumOptions["??????"] ?: 0.0) + (parsedUpValue ?: 0.0) as Double)
                    }
                    "?????????" -> simpleSumOptions["??????"] = ((simpleSumOptions["??????"] ?: 0.0) + (parsedUpValue ?: 0.0) as Double)
                    "?????????" -> simpleSumOptions["??????"] = ((simpleSumOptions["??????"] ?: 0.0) + (parsedUpValue ?: 0.0) as Double)
                    "HP MAX", "MP MAX" -> {
                        simpleSumOptions[applyType] = ((simpleSumOptions[applyType] ?: 0.0)
                                + (parsedUpValue ?: 0.0) as Double)
                    }
                    "HP MAX%", "MP MAX%", "?????????%" -> {
                        simpleSumOptions[applyType] = (
                                (1+(simpleSumOptions[applyType] ?: 0.0))*(1+(parsedUpValue ?: 0.0) as Double)-1.0
                                )
                    }
                    "????????????" -> {
                        simpleSumOptions["?????? ????????????"] = (
                                (simpleSumOptions["?????? ????????????"] ?: 0.0) + (parsedUpValue as JSONArray)[4] as Double)
                    }
                    "????????????" -> totalDamage += (parsedUpValue ?: 0.0) as Double
                    "???????????????" -> totalMpConsumptionIncrease += (parsedUpValue ?: 0.0) as Double
                    "????????????" -> listArrayStatusResist.add(parsedUpValue as JSONArray)
                    "??????" -> listArrayLeveling.add(parsedUpValue as JSONArray)
                    "???????????????" -> listArrayActiveLeveling.add(parsedUpValue as JSONArray)
                    "?????????" -> listArrayCoolRecover.add(parsedUpValue as JSONArray)
                    "??????" -> listArrayCoolDown.add(parsedUpValue as JSONArray)
                    "??????" -> listArraySkillDamage.add(parsedUpValue as JSONArray)
                    "??????" -> listArraySpeed.add(parsedUpValue as JSONArray)
                }
            }catch (ignored: Exception){}
        }else{
            reqType = reqType as String
            if(reqValue=="tg"){
                val nowJson = JSONObject()
                nowJson["code"] = code
                nowJson["reqType"] = reqType
                nowJson["applyType"] = applyType
                nowJson["apply"] = parsedUpValue
                if(isCondition){
                    jsonConditionToggle.add(nowJson)
                }
            }else if(reqValue=="arr"){

            }else{
                val nowJson = JSONObject()
                nowJson["code"] = code
                nowJson["applyType"] = applyType
                nowJson["apply"] = parsedUpValue

                val applyValueArray = JSONArray()  // ??? ??? ???
                val reqValueArray = JSONArray()  // ??? ??? ???
                if(reqType.contains("HP n%") || reqType.contains("MP n%")){
                    // println("isHPAlwaysLow = $isHPAlwaysLow")
                    reqValueArray.add("1")
                    if(!(reqType.contains("HP n%") && isHPAlwaysLow)){
                        reqValueArray.add("61")
                        reqValueArray.add("100")
                    }
                    applyValueArray.add(0.0)
                    applyValueArray.add(0.0)
                    applyValueArray.add(0.0)
                    val strArray = reqType.split(" ")
                    nowJson["reqType"] = strArray[0] + " n%"

                    val multi = ((reqMulti ?: 0.0) as Double)
                    val reg = strArray[strArray.size-1]
                    if(reg=="??????" || reg=="??????" || reg=="??????"){
                        applyValueArray[0] = multi
                        for(i in 0 .. multi.toInt()){
                            val nowReqValue = 100 - (i * (reqValue ?: 0.0) as Double)
                            if(nowReqValue <= 61){
                                applyValueArray[1] = (i - 1).toDouble()
                                break
                            }
                        }
                        applyValueArray[2] = 0.0
                    }else if(reg=="??????" || reg=="??????" || reg=="??????"){
                        applyValueArray[0] = 0.0
                        for(i in 0 .. multi.toInt()){
                            val nowReqValue = i * (reqValue ?: 0.0) as Double
                            if(nowReqValue >= 61){
                                applyValueArray[1] = i.toDouble()
                                break
                            }
                        }
                        applyValueArray[2] = multi
                    }else if(reg=="??????"){
                        applyValueArray[1] = 1.0
                    }
                    nowJson["applyGauge"] = applyValueArray
                    nowJson["reqGauge"] = reqValueArray

                }else{
                    nowJson["reqType"] = reqType
                    val multi = ((reqMulti ?: 1.0) as Double).toInt()
                    val value = (reqValue ?: 1.0) as Double
                    if(multi == -1){  //????????????
                        reqValueArray.add("${value.toInt()-1}")
                        applyValueArray.add(1.0)
                        reqValueArray.add("${value.toInt()}")
                        applyValueArray.add(0.0)
                    }else{
                        for(j in 0..multi){
                            reqValueArray.add("${(j*value).toInt()}")
                            applyValueArray.add(j.toDouble())
                        }
                    }

                    nowJson["applyGauge"] = applyValueArray
                    nowJson["reqGauge"] = reqValueArray
                }
                if(isCondition){
                    jsonConditionGauge.add(nowJson)
                    //println("add jsonConditionGauge nowJson = ${nowJson.toJSONString()}")
                }

            }
        }
    }

    fun getConditionJson(): JSONObject{
        // println("start getConditionJson jsonConditionGauge = ${jsonConditionGauge.toJSONString()}")
        val returnJson = JSONObject()
        for(i in jsonConditionToggle.indices){
            val nowJson = jsonConditionToggle[i] as JSONObject
            returnJson[nowJson["reqType"] as String] = "tg"
        }
        for(i in jsonConditionGauge.indices){

            val nowJson = jsonConditionGauge[i] as JSONObject
            // println("$i getConditionJson nowJson = ${nowJson.toJSONString()}")
            val reqType = nowJson["reqType"] as String
            val reqGauge = (nowJson["reqGauge"] as JSONArray).clone() as JSONArray
            if(returnJson[reqType]!= null){
                val preArray = returnJson[reqType] as JSONArray
                // println(preArray)
                // println(reqGauge)
                for(v in reqGauge){
                    if(!preArray.contains(v)){
                        preArray.add(v)
                    }
                }
                val sortArray = ArrayList<Double>();
                preArray.forEach { v -> sortArray.add(v.toString().toDouble()) }
                sortArray.sort()
                val addArray = JSONArray()
                sortArray.forEach { u -> addArray.add(u.toInt().toString()) }
                returnJson[reqType] = addArray
            }else{
                returnJson[reqType] = reqGauge
            }
            // println("returnJson : ${returnJson.toJSONString()}")
            // println("$i getConditionJson jsonConditionGauge = ${jsonConditionGauge.toJSONString()}")
            // println()
        }
        // println("finish getConditionJson jsonConditionGauge = ${jsonConditionGauge.toJSONString()}")
        return returnJson
    }

    fun applyCondition(condition : HashMap<String, String>){
        //println("start applyCondition jsonConditionGauge = ${jsonConditionGauge.toJSONString()}")
        resetCondition()
        val onConditionJsonList = ArrayList<JSONObject>()
        for(i in jsonConditionToggle.indices){
            val nowJson = jsonConditionToggle[i] as JSONObject
            if("true"==condition[nowJson["reqType"] as String]){
                onConditionJsonList.add(nowJson)
            }
        }
        for(i in jsonConditionGauge.indices){
            val nowJson = jsonConditionGauge[i] as JSONObject
            //println("get jsonConditionGauge nowJson = ${nowJson.toJSONString()}")
            val reqGauge = nowJson["reqGauge"] as JSONArray
            val applyGauge = nowJson["applyGauge"] as JSONArray

            val nowGauge = condition[nowJson["reqType"] as String] as String
            val nowGaugeValue = nowGauge.toDouble()
            //println("nowGaugeValue = $nowGaugeValue")
            //println("reqGauge = ${reqGauge.toJSONString()}")
            var index = -1
            for(j in reqGauge.indices){
                val tempValue = (reqGauge[j] as String).toDouble()
                //println(tempValue)
                if(tempValue > nowGaugeValue) {
                    //println("$tempValue ?????? $nowGaugeValue ??? ???????????? break ???. ?????? index = $index")
                    break
                }
                index++
            }
            //println("nowJson=${nowJson.toJSONString()}")
            val nowMulti = (applyGauge[index] as Double).toInt()
            repeat(nowMulti) { onConditionJsonList.add(nowJson) }
        }

        for(nowJson in onConditionJsonList){
            val applyType = nowJson["applyType"] as String
            when (applyType) {
                "??????", "?????????", "HP MAX", "MP MAX", "?????????", "?????????" -> {
                    mapConditionSimpleSum[applyType] = ((mapConditionSimpleSum[applyType] ?: 0.0)
                            + (nowJson["apply"] as Double))
                }
                "HP MAX%", "MP MAX%", "?????????%" -> {
                    mapConditionSimpleSum[applyType] = (
                            (1+(mapConditionSimpleSum[applyType] ?: 0.0))*(1+(nowJson["apply"] as Double))-1.0
                            )
                }
                "????????????" -> totalDamageCondition += nowJson["apply"] as Double
                "???????????????" -> totalMpConsumptionIncrease += nowJson["apply"] as Double
                "????????????" -> listArrayElementCondition.add(nowJson["apply"] as JSONArray)
                "????????????" -> listArrayElementResistCondition.add(nowJson["apply"] as JSONArray)
                "??????" -> listArraySkillDamageCondition.add(nowJson["apply"] as JSONArray)
                "??????" -> listArrayCoolDownCondition.add(nowJson["apply"] as JSONArray)
                "?????????" -> listArrayCoolRecoverCondition.add(nowJson["apply"] as JSONArray)
                "??????" -> listArrayLevelingCondition.add(nowJson["apply"] as JSONArray)
                "???????????????" -> listArrayActiveLevelingCondition.add(nowJson["apply"] as JSONArray)
                "???????????????" -> {
                    val applyJson = nowJson["apply"] as JSONArray
                    val status = applyJson[0] as String
                    statusDamageCondition[status] = (statusDamageCondition[status] ?: 0.0) + (applyJson[1] as Double)
                }
                "??????" -> listArraySpeedCondition.add(nowJson["apply"] as JSONArray)
                "????????????" -> listArrayStatusResistCondition.add(nowJson["apply"] as JSONArray)
            }
        }
        calculateDamage()
    }

    private var totalDamageCondition = 0.0
    private var totalMpConsumptionIncrease = 0.0
    private var mapConditionSimpleSum = HashMap<String, Double>()
    private val listArraySpeedCondition = ArrayList<JSONArray>()
    private val listArrayLevelingCondition = ArrayList<JSONArray>()
    private val listArrayStatusResistCondition = ArrayList<JSONArray>()
    private val listArrayActiveLevelingCondition = ArrayList<JSONArray>()
    private val listArrayCoolDownCondition = ArrayList<JSONArray>()
    private val listArrayCoolRecoverCondition = ArrayList<JSONArray>()
    private val listArraySkillDamageCondition = ArrayList<JSONArray>()
    private val listArrayElementCondition = ArrayList<JSONArray>()
    private val listArrayElementResistCondition = ArrayList<JSONArray>()
    private val statusDamageCondition = HashMap<String, Double>()

    var arrayLeveling = Array<Double>(19){0.0}
    var arrayActiveLeveling = Array<Double>(19){0.0}
    private var arrayCoolDown = Array<Double>(19){0.0}
    private var arrayCoolRecover = Array<Double>(19){0.0}
    private var arraySkillDamage = Array<Double>(19){1.0}
    private var arrayElement = Array<Double>(5){0.0}

    var arrayTotalLevelDamage = Array<Double>(19){0.0}
    var arrayTotalLevelDamageWithCool = Array<Double>(19){0.0}
    var arrayTotalCoolDown = Array<Double>(19){0.0}

    private var arrayCubeUse = arrayOf(0, 0, 0, 0, 0, 0, 0, 1, 1, 2, 0, 5, 2, 3, 3, 5, 10, 7, 15)
    private var totalDamage = 0.0

    private var optionLevel = 2.6
    private var optionMythLevel = 1.03*1.03*1.03*1.03
    private var optionNot100Level = 1.0
    private var titlePetPercent = 0.33
    private var pet2ndPassive = 0.0
    private val baseElement = 13.0
    private var customElement = arrayOf(
        15.0+35*3+30+6+25+7,
        30.0+25,
        15.0+35*3+30+25,
        30.0+25
    )
    private var customStat = arrayOf(  //?????? ??? ??????
        0.0, 0.0, 1.0
    )

    private val enchantMaxStat = 0.0
    private val enchantMaxAtk = 0.0
    private val basicStatSolo = 15000.0 - 4 * (40*2+50*3+150)
    private val basicAtkSolo = 3000.0 - (70*2+100)
    private val basicStatSoloBuffer = 25000.0 - 4 * (40*2+50*3+150)
    private val basicStatBuff = 80000.0 - 4 * (40*2+50*3+150)
    private val basicAtkBuff = 10000.0 - (70*2+100)
    private var applyStat = 80000.0 - 4 * (40*2+50*3+150)
    private var applyAtk = 10000.0 - (70*2+100)
    var additionalStat = 0.0

    private fun resetArrayData(){
        arrayLeveling = Array<Double>(19){0.0}
        arrayActiveLeveling = Array<Double>(19){0.0}
        arrayCoolDown = Array<Double>(19){0.0}
        arrayCoolRecover = Array<Double>(19){0.0}
        arraySkillDamage = Array<Double>(19){1.0}
        arrayElement = Array<Double>(5){0.0}
        arrayTotalLevelDamage = Array<Double>(19){0.0}
        arrayTotalLevelDamageWithCool = Array<Double>(19){0.0}
        arrayTotalCoolDown = Array<Double>(19){0.0}
    }

    private val levelMax = arrayOf(
        100.0, 56.0, 53.0, 51.0, 48.0, 46.0, 43.0, 41.0, 38.0, 36.0, 23.0, 14.0,
        28.0, 23.0, 21.0, 18.0, 7.0, 11.0, 4.0
    )
    private val levelInterval = arrayOf(
        1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 5,
        2, 2, 2, 2, 5, 2, 5
    )
    private val levelEfficiency = arrayOf(0.0, 0.01, 0.1, 0.15, 0.18, 0.23)

    private var mapSimpleOption = HashMap<String, Double>()
    private var mapStatusResist = HashMap<String, Double>()

    private fun calculateDamage(){
        detailMap.clear()
        resetArrayData()
        mapSimpleOption = simpleSumOptions.clone() as HashMap<String, Double>
        mapStatusResist = statusResist.clone() as HashMap<String, Double>
        //println(jsonConditionGauge.toJSONString())

        for(now in listArrayElement + listArrayElementCondition){
            for(i in now.indices) arrayElement[i] += (now[i] as Double)
        }
        for(now in listArrayElementResistCondition){
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) + now[0] as Double
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) + now[1] as Double
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) + now[2] as Double
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) + now[3] as Double
        }

        for(now in listArrayLeveling + listArrayLevelingCondition){
            for(i in now.indices) arrayLeveling[i] += (now[i] as Double)
        }
        for(now in listArrayActiveLeveling + listArrayActiveLevelingCondition){
            for(i in now.indices) arrayActiveLeveling[i] += (now[i] as Double)
        }
        for(now in listArrayCoolDown + listArrayCoolDownCondition){
            for(i in now.indices) arrayCoolDown[i] = 1-(1-arrayCoolDown[i]) * (1-(now[i] as Double))
        }
        for(now in listArrayCoolRecover+ listArrayCoolRecoverCondition){
            for(i in now.indices) arrayCoolRecover[i] += (now[i] as Double)
        }
        var conditionSkillDamageWithAll = 1.0
        for(now in listArraySkillDamage + listArraySkillDamageCondition){
            if(!now.contains(0.0)){
                // println("0??? ???????????? ?????? ?????? ????????? = $now")
                conditionSkillDamageWithAll *= (1+(now[0] as Double))
            }
            for(i in now.indices) arraySkillDamage[i] *= (1+(now[i] as Double))
        }
        mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + extraAtkSpeed
        for(now in listArraySpeed + listArraySpeedCondition){
            mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + now[0] as Double
            mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + now[1] as Double
            mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + now[2] as Double
        }
        for(now in listArrayStatusResist + listArrayStatusResistCondition){
            for(i in now.indices){
                val key = damageCondition.getStatusIndex(i)
                mapStatusResist[key] = (mapStatusResist[key] ?: 0.0) + (now[i] as Double)
            }
        }
        if(mapStatusResist["??????"] != null){
            for(i in 0..12){
                val key = damageCondition.getStatusIndex(i)
                mapStatusResist[key] = (mapStatusResist[key] ?: 0.0) + mapStatusResist["??????"]!!
            }
        }
        // println("arraySkillDamage: "+arraySkillDamage.contentToString())
        // println("arrayCubeUse: "+arrayCubeUse.contentToString())
        if(arrayEquipment.contains("14062")){
            for(i in arrayCubeUse.indices){
                if(arrayCubeUse[i] >= 30){
                    arraySkillDamage[i] *= (1.2/1.02)
                }else if(arrayCubeUse[i] >= 15){
                    arraySkillDamage[i] *= (1.1/1.02)
                }
            }
        }
        // println("arrayLeveling: "+arrayLeveling.contentToString())
        // println("arrayCoolDown: "+arrayCoolDown.contentToString())
        // println("arrayCoolRecover: "+arrayCoolRecover.contentToString())
        println("arraySkillDamage: "+arraySkillDamage.contentToString())
        //println(jsonConditionToggle.toJSONString())

        var passiveDamage = 1.0
        val summoner60Passive = if(job == "?????????(???) ?????????" && arrayEquipment.contains("31182")){
            10.0}else{0.0} // ??????????????? ?????????
        for(i in arrayLeveling.indices){
            var nowUpLv = (
                    arrayLeveling[i] + if(i==14){pet2ndPassive}else{0.0}+if(i==12){summoner60Passive}else{0.0}
                    )
            if(i!=10 && i!=14 && nowUpLv > 10) nowUpLv = 10.0
            passiveDamage *= (nowUpLv * (jobPassiveArray[i] as Double)) + 1.0
        }
        println("passiveDamage = $passiveDamage")

        var totalSumDamage = totalDamage + totalDamageCondition

        for(now in arrayUpDamage){
            totalSumDamage += now
        }

        val elementArray = Array<Double>(4){baseElement}
        elementArray[0] += (simpleSumOptions["??? ????????????"] ?: 0.0) + arrayElement[0] + customElement[0]
        elementArray[1] += (simpleSumOptions["??? ????????????"] ?: 0.0) + arrayElement[1] + customElement[1]
        elementArray[2] += (simpleSumOptions["??? ????????????"] ?: 0.0) + arrayElement[2] + customElement[2]
        elementArray[3] += (simpleSumOptions["??? ????????????"] ?: 0.0) + arrayElement[3] + customElement[3]
        if(arrayEquipment.contains("33142")){  // ????????? ????????? 4??? ?????????
            var upElement = (elementArray[1] / 8).toInt().toDouble()
            if(upElement > 50.0) upElement = 50.0
            elementArray[0] += upElement
            elementArray[2] += upElement
            elementArray[3] += upElement
            var downElement = (elementArray[1] / 10).toInt().toDouble()
            if(downElement > 30.0) downElement = 30.0
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) - downElement
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) - downElement
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) - downElement
            if(elementArray[1] > 150.0){
                mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + 0.15
                mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + 0.15
            }
        }else if(arrayEquipment.contains("33042")){  // ????????? ????????? 4??? ?????????
            var upElement = (elementArray[0] / 8).toInt().toDouble()
            if(upElement > 50.0) upElement = 50.0
            elementArray[1] += upElement
            elementArray[2] += upElement
            elementArray[3] += upElement
            var downElement = (elementArray[0] / 10).toInt().toDouble()
            if(downElement > 30.0) downElement = 30.0
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) - downElement
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) - downElement
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) - downElement
            if(elementArray[0] > 150.0){
                mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + 0.15
                mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + 0.15
            }
        }else if(arrayEquipment.contains("33152")){  // ????????? ????????? 4??? ?????????
            var upElement = (elementArray[3] / 8).toInt().toDouble()
            if(upElement > 50.0) upElement = 50.0
            elementArray[0] += upElement
            elementArray[1] += upElement
            elementArray[2] += upElement
            var downElement = (elementArray[3] / 10).toInt().toDouble()
            if(downElement > 30.0) downElement = 30.0
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) - downElement
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) - downElement
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) - downElement
            if(elementArray[3] > 150.0){
                mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + 0.15
                mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + 0.15
            }
        }else if(arrayEquipment.contains("33082")){  // ????????? ????????? 4??? ?????????
            var upElement = (elementArray[2] / 8).toInt().toDouble()
            if(upElement > 50.0) upElement = 50.0
            elementArray[0] += upElement
            elementArray[1] += upElement
            elementArray[3] += upElement
            var downElement = (elementArray[2] / 10).toInt().toDouble()
            if(downElement > 30.0) downElement = 30.0
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) - downElement
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) - downElement
            mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) - downElement
            if(elementArray[2] > 150.0){
                mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + 0.15
                mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + 0.15
            }
        }
        if(arrayEquipment.contains("11152")) {  // ?????? ?????????
            if(elementArray[0] > 250.0) mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) + 20.0
        }
        if(arrayEquipment.contains("13152")) {  // ?????? ?????????
            if(elementArray[1] > 250.0) mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) + 20.0
        }
        if(arrayEquipment.contains("14152")) {  // ?????? ?????????
            if(elementArray[2] > 250.0) mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) + 20.0
        }
        if(arrayEquipment.contains("15152")) {  // ?????? ?????????
            if(elementArray[3] > 250.0) mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) + 20.0
        }
        if(arrayEquipment.contains("11182")){  // ????????? ???????????? ??????
            if(elementArray[0] == elementArray[1]
                && elementArray[1] == elementArray[2] && elementArray[2] == elementArray[3]){
                mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) + 25.0
                mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) + 25.0
                mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) + 25.0
                mapSimpleOption["??? ????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0) + 25.0
            }
        }
        val elementResistArray = arrayOf(
            mapSimpleOption["??? ????????????"] ?: 0.0, mapSimpleOption["??? ????????????"] ?: 0.0,
            mapSimpleOption["??? ????????????"] ?: 0.0, mapSimpleOption["??? ????????????"] ?: 0.0
        )
        var maxElement = 0.0
        var maxElementResist = -100.0
        var maxElementIndex = 0
        var maxElementIndexResist = 0
        var minElement = 9999.0
        for(i in 0 until 4){
            val nowElement = elementArray[i]
            val nowElementResist = elementResistArray[i]
            if(nowElement > maxElement){
                maxElement = nowElement
                maxElementIndex = i
            }
            if(nowElementResist > maxElementResist){
                maxElementResist = nowElementResist
                maxElementIndexResist = i
            }
        }
        maxElementResist += (mapSimpleOption["?????? ????????????"] ?: 0.0)
        mapSimpleOption[when(maxElementIndexResist){
            0 -> "???"
            1 -> "???"
            2 -> "???"
            3 -> "???"
            else -> "???"
        }+" ????????????"]=maxElementResist
        maxElement += arrayElement[4]
        elementArray[maxElementIndex] = maxElement
        // println("elementArray = ${elementArray.contentToString()}")
        detailMap["???????????????"] = elementArray[0].toInt().toString()
        detailMap["???????????????"] = elementArray[1].toInt().toString()
        detailMap["???????????????"] = elementArray[2].toInt().toString()
        detailMap["???????????????"] = elementArray[3].toInt().toString()

        if(arrayCustomOption.contains("242")){
            if(elementArray[0]+elementArray[1]+elementArray[2]+elementArray[3] >= 850){
                mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + 0.4
            }
        }
        if(arrayCustomOption.contains("244")){
            if(elementArray[0]+elementArray[1]+elementArray[2]+elementArray[3] >= 850){
                for(k in arrayOf("??????", "??????", "??????", "??????",
                    "??????", "??????", "??????", "??????", "??????", "??????", "??????", "??????", "??????")){
                    mapStatusResist[k] = (mapStatusResist[k] ?: 0.0) + 0.3
                }
            }
        }
        if(arrayEquipment.contains("22232")){  // ????????? ?????? ????????????
            var upCoolRecover = (maxElement/50.0).toInt() * 0.04
            if(upCoolRecover > 0.24) upCoolRecover = 0.24
            println("????????? ????????? upCoolRecover = $upCoolRecover")
            for(i in arrayCoolRecover.indices) arrayCoolRecover[i] += upCoolRecover
            var upSpeed = (maxElement/50.0).toInt() * 0.03
            if(upSpeed > 0.15) upSpeed = 0.15
            mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + upSpeed
            mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + upSpeed
            mapSimpleOption["??????"] = (mapSimpleOption["??????"] ?: 0.0) + upSpeed
        }
        var elementMaxSkillDamage = 1.0
        if(arrayEquipment.contains("23232")){  // ????????? ?????? ???
            if(maxElement >= 300.0){
                totalSumDamage += 2816.0
                elementMaxSkillDamage = 1.07
            }else if(maxElement >= 250){
                totalSumDamage += 1927.0
            }else if(maxElement >= 200){
                totalSumDamage += 1037.0
            }
            println("????????? ?????? ??? elementMaxSkillDamage = $elementMaxSkillDamage")
        }

        val stat = (((simpleSumOptions["??????"] ?: 0.0)+customStat[0] + additionalStat) * 4.08 + applyStat) / applyStat
        val atk = (((simpleSumOptions["?????????"] ?: 0.0)+customStat[1]) + applyAtk) / applyAtk

        val damage100 = (simpleSumOptions["?????????"] ?: 0.0) + titlePetPercent + 1
        val damage105 = (totalSumDamage * optionLevel  * (1+titlePetPercent) / 1000.0)
        detailMap["????????????"] = "${totalSumDamage.toInt()}*${(((optionLevel * (1+titlePetPercent))*10).toInt()/10.0)}"

        // ?????? ?????? ?????? ///////////////////////////////////////////////////////////////////////////////////////////////
        var mpOverSkillDamage = if(arrayEquipment.contains("15172")){  // ???????????? ?????? ????????? ?????? ??????
            ((simpleSumOptions["MP?????????"] ?: 0.0) + totalMpConsumptionIncrease) * 0.05 + 1
        }else{1.0}
        if(mpOverSkillDamage > 1.25) mpOverSkillDamage = 1.25
        detailMap["???????????????"] = "${round(((simpleSumOptions["MP?????????"] ?: 0.0) + totalMpConsumptionIncrease)*100).toInt()}%"
        // println("mpOverSkillDamage = $mpOverSkillDamage")

        if(arrayEquipment.contains("11232")){  // ???????????? ??????
            var totalStatusResist = 0.0
            mapStatusResist.forEach { (t, v) ->
                totalStatusResist += v
            }
            var upStatusDamage = totalStatusResist * 0.05
            if(upStatusDamage > 0.1) upStatusDamage = 0.1
            if(upStatusDamage < 0.0) upStatusDamage = 0.0
            println("???????????? ?????? upStatusDamage = $upStatusDamage")
            statusDamageCondition["??????"] = (statusDamageCondition["??????"] ?: 0.0) + upStatusDamage
            statusDamageCondition["??????"] = (statusDamageCondition["??????"] ?: 0.0) + upStatusDamage
            statusDamageCondition["??????"] = (statusDamageCondition["??????"] ?: 0.0) + upStatusDamage
            statusDamageCondition["??????"] = (statusDamageCondition["??????"] ?: 0.0) + upStatusDamage
        }

        var cyberSkillDamage = 1.0
        if(arrayEquipment.contains("15232")) {  // ???????????? ??????
            if((mapSimpleOption["??????"] ?: 0.0) >= 1.40){
                cyberSkillDamage = 1.3
            }
            println("?????? ???????????? = ${mapSimpleOption["??????"]}")
            println("???????????? ?????? cyberSkillDamage = $cyberSkillDamage")
        }

        val mythOptionLevelDamage = if(isMythExist){1.0}else{optionMythLevel} // ????????? ?????? ??????
        val not100OptionLevelDamage = if(is100Exist){1.0}else{optionNot100Level}
        // println("mythOptionLevelDamage = $mythOptionLevelDamage")

        val totalSkillDamage = (skillDamage * mpOverSkillDamage * mythOptionLevelDamage *
                not100OptionLevelDamage * cyberSkillDamage * elementMaxSkillDamage)

        detailMap["???????????????"] = "${round((totalSkillDamage*conditionSkillDamageWithAll-1)*1000)/10.0}%"

        var sumDamage = ((damage100 + damage105) * totalSkillDamage * stat * atk *
                (1.05 + 0.0045 * maxElement) * passiveDamage
                )

        val statusDamageMap = HashMap<String, Double>()
        var totalDamageRatio = 0.0
        var transRatio = 0.0
        statusTrans.forEach { (key, value) ->
            transRatio += value
            val nowRatio = value * (1 + (statusDamage[key] ?: 0.0) + (statusDamageCondition[key] ?: 0.0))
            detailMap["${key}??????"] = "${round(value*100).toInt()}%"
            detailMap["${key}??????"] = "${round(((statusDamage[key] ?: 0.0) + (statusDamageCondition[key] ?: 0.0))*100).toInt()}%"
            // println("statusDamage[key] = ${statusDamage[key]}")
            // println("statusDamageCondition[key] = ${statusDamageCondition[key]}")
            // println("??????: $key ??? ?????? = $nowRatio")
            totalDamageRatio += nowRatio
            statusDamageMap[key] = sumDamage * nowRatio
        }
        totalDamageRatio += (1 - transRatio)
        statusDamageMap["??????"] = sumDamage * (1 - transRatio)
        // println("?????? ?????? ??? sumDamage: $sumDamage")
        sumDamage *= totalDamageRatio

        // println("??? ??????: $totalSumDamage")
        // println("sumDamage: $sumDamage")

        arrayTotalLevelDamage = Array<Double>(19){0.0}
        arrayTotalLevelDamageWithCool = Array<Double>(19){0.0}
        arrayTotalCoolDown = Array<Double>(19){0.0}

        for(i in 0 until 19){
            arrayTotalLevelDamage[i] = (sumDamage *
                    (1 + (arrayLeveling[i] + +arrayActiveLeveling[i] + levelMax[i]) * levelEfficiency[levelInterval[i]]) /
                    (1 + levelMax[i] * levelEfficiency[levelInterval[i]])
                    ) * arraySkillDamage[i]
            if(levelInterval[i] == 5){
                arrayTotalCoolDown[i] = 1 - (1 - arrayCoolDown[i]) / (1+arrayCoolRecover[i])
            }else if(levelInterval[i] == 3 || levelInterval[i] == 1){
                arrayTotalCoolDown[i] = 0.0
            }else{
                arrayTotalCoolDown[i] = 1 - (1 - arrayCoolDown[i]) / (1+arrayCoolRecover[i])
            }
            if(arrayTotalCoolDown[i] > 0.7) arrayTotalCoolDown[i] = 0.7
            val coolRealEff = (
                    (1.0 / (1 - arrayTotalCoolDown[i]) - 1) /
                    ((arrayTotalCoolDown[i] / 0.7)*(arrayTotalCoolDown[i] / 0.7)*(arrayTotalCoolDown[i] / 0.7) + 1)
                    )
            // println("coolRealEff = $coolRealEff")
            arrayTotalLevelDamageWithCool[i] = arrayTotalLevelDamage[i] * (coolRealEff * 0.5 + 1.0)
        }

        // println("arrayTotalLevelDamage: ${arrayTotalLevelDamage.contentToString()}")
        // println("arrayTotalCoolDown: ${arrayTotalCoolDown.contentToString()}")
        // println("arrayTotalLevelDamageWithCool: ${arrayTotalLevelDamageWithCool.contentToString()}")

        setDetailMap()
    }

    var detailMap = HashMap<String, String>()
    private fun setDetailMap(){
        detailMap["???????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0).toInt().toString()
        detailMap["???????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0).toInt().toString()
        detailMap["???????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0).toInt().toString()
        detailMap["???????????????"] = (mapSimpleOption["??? ????????????"] ?: 0.0).toInt().toString()

        detailMap["????????????"] = "${round((mapSimpleOption["??????"] ?: 0.0)*1000)/10.0}%"
        detailMap["???????????????"] = "${round((mapSimpleOption["??????"] ?: 0.0)*1000)/10.0}%"
        detailMap["????????????"] = "${round((mapSimpleOption["??????"] ?: 0.0)*1000)/10.0}%"

        detailMap["????????????"] = "${round(((mapSimpleOption["??????"] ?: 0.0)+(mapConditionSimpleSum["??????"] ?: 0.0))*1000)/10.0}%"
        detailMap["???????????????"] = "${((mapSimpleOption["??????"] ?: 0.0)+(mapConditionSimpleSum["?????????"] ?: 0.0)).toInt()}"
        detailMap["???????????????"] = "${((mapSimpleOption["??????"] ?: 0.0)+(mapConditionSimpleSum["?????????"] ?: 0.0)).toInt()}"
        detailMap["?????????"] = "${round(((mapSimpleOption["??????"] ?: 0.0)+(mapConditionSimpleSum["?????????"] ?: 0.0))*1000)/10.0}%"
        detailMap["?????????"] = "${round(((mapSimpleOption["??????"] ?: 0.0)+(mapConditionSimpleSum["?????????"] ?: 0.0))*1000)/10.0}%"
        detailMap["HP?????????"] = "${(mapSimpleOption["HP???"] ?: 0.0).toInt()}"
        detailMap["MP?????????"] = "${(mapSimpleOption["MP???"] ?: 0.0).toInt()}"
        detailMap["HP MAX"] = "${((mapSimpleOption["HP MAX"] ?: 0.0)+(mapConditionSimpleSum["HP MAX"] ?: 0.0)).toInt()}"
        detailMap["MP MAX"] = "${((mapSimpleOption["MP MAX"] ?: 0.0)+(mapConditionSimpleSum["MP MAX"] ?: 0.0)).toInt()}"

        mapStatusResist.forEach { (t, v) ->
            // println(t + " = " + v)
            detailMap[t+"??????"] = "${round(v*1000)/10.0}%"
        }

        for(k in arrayOf("??????", "??????", "??????", "??????")){
            if(detailMap[k+"??????"] == null) detailMap[k+"??????"] = "0%"
            if(detailMap[k+"??????"] == null) detailMap[k+"??????"] = "0%"
            if(detailMap[k+"??????"] == null) detailMap[k+"??????"] = "0.0%"
        }

        for(k in arrayOf("??????", "??????", "??????", "??????", "??????", "??????", "??????", "??????", "??????")){
            if(detailMap[k+"??????"] == null) detailMap[k+"??????"] = "0.0%"
        }
    }
}