package org.dnf_calc_n.calculate

import org.dnf_calc_n.Common
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.lang.IndexOutOfBoundsException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Damage(private var equipmentData: JSONObject) {

    private lateinit var job : String
    private lateinit var arrayEquipment : Array<String>
    private val common = Common()
    //private var jobSkillData : JSONObject = common.loadJsonObject("")
    private var mapResult = HashMap<String, String>()

    private lateinit var arrayUpDamage : Array<Double>
    private var skillDamage = 1.0

    val damageCondition = DamageCondition()


    private fun resetData(){
        mapResult.clear()
        arrayEquipment = Array(13){""}
        arrayUpDamage = Array(4){0.0}
        listArrayLeveling.clear()
        listArrayCoolDown.clear()
        listArrayCoolRecover.clear()
        listArraySkillDamage.clear()
        listArrayElement.clear()
        arrayLeveling = Array<Double>(19){0.0}
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
        isCubeForced = false
        isHPAlwaysLow = false
    }

    private fun resetCondition(){
        listArrayLevelingCondition.clear()
        listArrayCoolDownCondition.clear()
        listArrayCoolRecoverCondition.clear()
        listArraySkillDamageCondition.clear()
        listArrayElementCondition.clear()
        statusDamageCondition.clear()
        totalDamageCondition = 0.0
    }

    fun startDamageCalculate(mapEquipment: HashMap<String,String>) : Boolean {
        resetData()
        val jsonSave = common.loadJsonObject("cache/selected.json")
        job = (jsonSave["job"] ?: return false) as String
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
        return true
    }

    private val simpleSumOptions = HashMap<String, Double>()
    private val simpleSumKeys = arrayOf(
        "화 속성강화", "수 속성강화", "명 속성강화", "암 속성강화",
        "화 속성저항", "수 속성저항", "명 속성저항", "암 속성저항",
        "공속", "캐속", "이속", "물크", "마크", "적중", "회피",
        "HP MAX", "MP MAX", "HP젠", "MP젠", "물방", "마방", "피감", "MP소모량", "HP회복", "MP회복",
        "단리옵", "스탯", "공격력"
    )
    private val elementKey = arrayOf(
        "화 속성강화", "수 속성강화", "명 속성강화", "암 속성강화"
    )

    private val statusTrans = HashMap<String, Double>()  // 상변전환
    private val statusTotalDamage = HashMap<String, Double>()  // 상변피증
    private val statusDamage = HashMap<String, Double>()  // 상변데미지
    private val statusAntiResist = HashMap<String, Double>()  // 상변내성무
    private val statusResist = HashMap<String, Double>()  // 상변내성
    private val mapKey = arrayOf(
        "상변전환", "상변피증", "상변데미지", "상변내성무시", "상변내성"
    )
    private val mapValue = arrayOf(
        statusTrans, statusTotalDamage, statusDamage, statusAntiResist, statusResist
    )

    private fun loadEquipmentData(){
        for(code in arrayEquipment){
            val nowJson : JSONObject = (equipmentData[code] ?: continue) as JSONObject

            if(code == "14062") isCubeForced = true
            if(code == "21212") isHPAlwaysLow = true

            val upDamage : JSONArray = nowJson["옵션피증"] as JSONArray
            for(i in 0 until upDamage.size){
                arrayUpDamage[i] += upDamage[i] as Double
            }

            val skillDamage = nowJson["스증"] as Double
            this.skillDamage = this.skillDamage * (skillDamage + 1)

            for(j in simpleSumKeys.indices){
                try{
                    val nowKey = simpleSumKeys[j]
                    val nowValue = nowJson[nowKey] as Double
                    simpleSumOptions[nowKey] = (simpleSumOptions[nowKey] ?: 0.0) + nowValue
                }catch (ignored: Exception){}
            }

            for(j in mapKey.indices){ // 태그가 있는 상변류들
                try{
                    val statusTransJson : JSONArray = nowJson[mapKey[j]] as JSONArray
                    val nowType = statusTransJson[0] as String
                    val nowValue = statusTransJson[1] as Double
                    mapValue[j][nowType] = (mapValue[j][nowType] ?: 0.0) + nowValue
                }catch (ignored: Exception){}
            }

            if(nowJson["조건부"] != null){
                val conditionJson = nowJson["조건부"] as JSONArray
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
        // println(simpleSumOptions.toString())
    }

    private val levelIndex = arrayOf(
        "1", "5", "10", "15", "20", "25", "30", "35", "40", "45", "48", "50",
        "60", "70", "75", "80", "85", "95", "100"
    )

    private val listArrayLeveling = ArrayList<JSONArray>()
    private val listArrayCoolDown = ArrayList<JSONArray>()
    private val listArrayCoolRecover = ArrayList<JSONArray>()
    private val listArraySkillDamage = ArrayList<JSONArray>()
    private val listArrayElement = ArrayList<JSONArray>()
    private val jsonConditionToggle = JSONArray()
    private val jsonConditionArray = JSONArray()
    private val jsonConditionGauge = JSONArray()
    private var isCubeForced = false
    private var isHPAlwaysLow = false

    private fun combineConditions(code: String, json: JSONArray){
        var reqType = json[0]
        val reqValue = json[1]
        val reqMulti = json[2]
        val upType = (json[3] ?: "") as String
        val upValue = (json[4] ?: 0.0) as Double

        var parsedUpValue : Any? = null
        var applyType : String = ""
        var isCondition = true

        if(upType=="피해증가"){
            applyType = "피해증가"
            parsedUpValue = upValue
        }else if(upType.contains("레벨") || upType.contains("쿨회복") ||
            upType.contains("쿨감") || upType.contains("스증") || upType.contains("방무")){
            val strArray = upType.split(" ")
            parsedUpValue = JSONArray()
            applyType = strArray[strArray.size-1]
            if(applyType=="방무") applyType = "스증"
            damageCondition.parseLevelArrayOption(upType, upValue, isCubeForced).forEach { v -> (parsedUpValue as JSONArray).add(v)}
        }else if(upType.contains("속성강화")){
            parsedUpValue = JSONArray()
            applyType = "속성강화"
            damageCondition.parseElementOption(upType, upValue).forEach { v -> (parsedUpValue as JSONArray).add(v)}
        }else if(upType.contains("데미지")){
            parsedUpValue = JSONArray()
            applyType = "상변데미지"
            parsedUpValue.add(upType.split(" ")[0])
            parsedUpValue.add(upValue)
        }else if(upType.contains("커맨드 효과")){
            applyType = "쿨감"
            val nowArray = arrayOf(
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.02, 0.02, 0.02, 0.0, 0.0, 0.02, 0.02,
                0.05, 0.05, 0.0, 0.05, 0.0
            )
            parsedUpValue = JSONArray()
            nowArray.forEach { v -> parsedUpValue.add(v * upValue) }
        }else{
            isCondition = false
        }

        if(reqType == null){  // 조건부가 없는 표기일 경우
            // 레벨 쿨감 스증 쿨회복 피해증가
            if(upType=="피해증가"){
                totalDamage += (parsedUpValue ?: 0.0) as Double
            }
            try{
                if(applyType=="레벨"){
                    listArrayLeveling.add(parsedUpValue as JSONArray)
                }else if(applyType=="쿨회복"){
                    listArrayCoolDown.add(parsedUpValue as JSONArray)
                }else if(applyType=="쿨감"){
                    listArrayCoolRecover.add(parsedUpValue as JSONArray)
                }else if(applyType=="스증"){
                    listArraySkillDamage.add(parsedUpValue as JSONArray)
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

                val applyValueArray = JSONArray()  // 저 중 고
                val reqValueArray = JSONArray()  // 저 중 고
                if(reqType.contains("HP n%") || reqType.contains("MP n%")){
                    reqValueArray.add("1")
                    if(!isHPAlwaysLow){
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
                    if(reg=="감소"){
                        applyValueArray[0] = multi
                        applyValueArray[1] = (multi/2).toInt().toDouble()
                        applyValueArray[2] = 0.0
                    }else if(reg=="마다"){
                        applyValueArray[0] = 0.0
                        applyValueArray[1] = (multi/2).toInt().toDouble()
                        applyValueArray[2] = multi
                    }else if(reg=="미만" || reg=="이하"){
                        applyValueArray[0] = 1.0
                    }else if(reg=="이상" || reg=="초과"){
                        applyValueArray[2] = 1.0
                    }else if(reg=="구간"){
                        applyValueArray[1] = 1.0
                    }
                    nowJson["applyGauge"] = applyValueArray
                    nowJson["reqGauge"] = reqValueArray

                }else{
                    nowJson["reqType"] = reqType
                    val multi = ((reqMulti ?: 1.0) as Double).toInt()
                    val value = (reqValue ?: 1.0) as Double
                    if(multi == -1){  //미만일때
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
                    //println("$tempValue 일때 $nowGaugeValue 를 초과하여 break 함. 이때 index = $index")
                    break
                }
                index++
            }
            //println("nowJson=${nowJson.toJSONString()}")
            val nowMulti = (applyGauge[index] as Double).toInt()
            repeat(nowMulti) { onConditionJsonList.add(nowJson) }
        }

        for(nowJson in onConditionJsonList){
            when(nowJson["applyType"] as String){
                "피해증가" -> totalDamageCondition += nowJson["apply"] as Double
                "속성강화" -> listArrayElementCondition.add(nowJson["apply"] as JSONArray)
                "스증" -> listArraySkillDamageCondition.add(nowJson["apply"] as JSONArray)
                "쿨감" -> listArrayCoolDownCondition.add(nowJson["apply"] as JSONArray)
                "쿨회복" -> listArrayCoolRecoverCondition.add(nowJson["apply"] as JSONArray)
                "레벨" -> listArrayLevelingCondition.add(nowJson["apply"] as JSONArray)
                "상변데미지" -> {
                    val applyJson = nowJson["apply"] as JSONArray
                    val status = applyJson[0] as String
                    statusDamageCondition[status] = (statusDamageCondition[status] ?: 0.0) + (applyJson[1] as Double)
                }
            }
        }
        calculateDamage()
    }

    private var totalDamageCondition = 0.0
    private val listArrayLevelingCondition = ArrayList<JSONArray>()
    private val listArrayCoolDownCondition = ArrayList<JSONArray>()
    private val listArrayCoolRecoverCondition = ArrayList<JSONArray>()
    private val listArraySkillDamageCondition = ArrayList<JSONArray>()
    private val listArrayElementCondition = ArrayList<JSONArray>()
    private val statusDamageCondition = HashMap<String, Double>()

    private var arrayLeveling = Array<Double>(19){0.0}
    private var arrayCoolDown = Array<Double>(19){0.0}
    private var arrayCoolRecover = Array<Double>(19){0.0}
    private var arraySkillDamage = Array<Double>(19){1.0}
    private var arrayElement = Array<Double>(5){0.0}

    var arrayTotalLevelDamage = Array<Double>(19){0.0}
    var arrayTotalLevelDamageWithCool = Array<Double>(19){0.0}
    var arrayTotalCoolDown = Array<Double>(19){0.0}


    private var totalDamage = 0.0

    private var optionLevel = 2.6
    private var titlePetPercent = 0.33
    private val baseElement = 13.0
    private var customElement = arrayOf(
        15.0+35*3+30+6+25+7,
        30.0+25,
        15.0+35*3+30+25,
        30.0+25
    )

    private fun resetArrayData(){
        arrayLeveling = Array<Double>(19){0.0}
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

    private fun calculateDamage(){

        resetArrayData()
        //println(jsonConditionGauge.toJSONString())

        for(now in listArrayElement + listArrayElementCondition){
            for(i in now.indices) arrayElement[i] += (now[i] as Double)
        }
        for(now in listArrayLeveling + listArrayLevelingCondition){
            for(i in now.indices) arrayLeveling[i] += (now[i] as Double)
        }
        for(now in listArrayCoolDown + listArrayCoolDownCondition){
            for(i in now.indices) arrayCoolDown[i] = 1-(1-arrayCoolDown[i]) * (1-(now[i] as Double))
        }
        for(now in listArrayCoolRecover+ listArrayCoolRecoverCondition){
            for(i in now.indices) arrayCoolRecover[i] += (now[i] as Double)
        }
        for(now in listArraySkillDamage + listArraySkillDamageCondition){
            for(i in now.indices) arraySkillDamage[i] *= (1+(now[i] as Double))
        }
        //println("arrayLeveling: "+arrayLeveling.contentToString())
        //println("arrayCoolDown: "+arrayCoolDown.contentToString())
        //println("arrayCoolRecover: "+arrayCoolRecover.contentToString())
        //println("arraySkillDamage: "+arraySkillDamage.contentToString())
        //println(jsonConditionToggle.toJSONString())

        var totalSumDamage = totalDamage + totalDamageCondition

        for(d in arrayUpDamage){
            totalSumDamage += (d * optionLevel)
        }

        val elementArray = Array<Double>(4){baseElement}
        elementArray[0] += (simpleSumOptions["화 속성강화"] ?: 0.0) + arrayElement[0] + customElement[0]
        elementArray[1] += (simpleSumOptions["수 속성강화"] ?: 0.0) + arrayElement[1] + customElement[1]
        elementArray[2] += (simpleSumOptions["명 속성강화"] ?: 0.0) + arrayElement[2] + customElement[2]
        elementArray[3] += (simpleSumOptions["암 속성강화"] ?: 0.0) + arrayElement[3] + customElement[3]
        var maxElement = 0.0
        var minElement = 9999.0
        for(i in 0 until 4){
            val nowElement = elementArray[i]
            if(nowElement > maxElement){
                maxElement = nowElement
            }
            if(nowElement < minElement){
                minElement = nowElement
            }
        }

        val stat = ((simpleSumOptions["스탯"] ?: 0.0) * 4.01 + 80250.0) / 80250.0
        val atk = ((simpleSumOptions["공격력"] ?: 0.0) + 10000.0) / 10000.0

        val damage100 = (simpleSumOptions["단리옵"] ?: 0.0) + titlePetPercent + 1
        val damage105 = (totalSumDamage / 1000.0) * (1+titlePetPercent)

        val sumDamage = ((damage100 + damage105) * skillDamage * stat * atk *
                (1.05 + 0.0045 * maxElement)
                )

        val statusDamageMap = HashMap<String, Double>()
        var transRatio = 0.0
        statusTrans.forEach { (key, value) ->
            transRatio += value
            statusDamageMap[key] = sumDamage * value * (
                    1 + (statusDamage[key] ?: 0.0) + (statusDamageCondition[key] ?: 0.0)
                    )
        }

        statusDamageMap["본뎀"] = sumDamage * (1 - transRatio)


        println("총 피증: $totalSumDamage")
        println("sumDamage: $sumDamage")

        arrayTotalLevelDamage = Array<Double>(19){0.0}
        arrayTotalLevelDamageWithCool = Array<Double>(19){0.0}
        arrayTotalCoolDown = Array<Double>(19){0.0}

        for(i in 0 until 19){
            arrayTotalLevelDamage[i] = (sumDamage *
                    (1 + (arrayLeveling[i] + levelMax[i]) * levelEfficiency[levelInterval[i]]) /
                    (1 + levelMax[i] * levelEfficiency[levelInterval[i]])
                    ) * arraySkillDamage[i]
            if(levelInterval[i] == 5){
                if(arrayCoolDown[i] > 0) arrayCoolDown[i] = 0.0
                if(arrayCoolRecover[i] > 0) arrayCoolRecover[i] = 0.0
                arrayTotalCoolDown[i] = 1 - (1 - arrayCoolDown[i]) / (1+arrayCoolRecover[i])
            }else if(levelInterval[i] == 3 || levelInterval[i] == 1){
                arrayTotalCoolDown[i] = 0.0
            }else{
                arrayTotalCoolDown[i] = 1 - (1 - arrayCoolDown[i]) / (1+arrayCoolRecover[i])
            }

            arrayTotalLevelDamageWithCool[i] = arrayTotalLevelDamage[i]* (((1 / (1-arrayTotalCoolDown[i]))-1)*0.5+1)
        }

        println("arrayTotalLevelDamage: ${arrayTotalLevelDamage.contentToString()}")
        println("arrayTotalCoolDown: ${arrayTotalCoolDown.contentToString()}")
        println("arrayTotalLevelDamageWithCool: ${arrayTotalLevelDamageWithCool.contentToString()}")

    }

}