package org.dnf_calc_n.calculate

import org.dnf_calc_n.Common
import org.json.simple.JSONArray
import org.json.simple.JSONObject
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
        calculateDamage()
    }

    private val levelIndex = arrayOf(
        "1", "5", "10", "15", "20", "25", "30", "35", "40", "45", "48", "50",
        "60", "70", "75", "80", "85", "95", "100"
    )

    private val listArrayLeveling = ArrayList<Array<Double>>()
    private val listArrayCoolDown = ArrayList<Array<Double>>()
    private val listArrayCoolRecover = ArrayList<Array<Double>>()
    private val listArraySkillDamage = ArrayList<Array<Double>>()
    private val jsonConditionToggle = JSONArray()
    private val jsonConditionArray = JSONArray()
    private val jsonConditionGauge = JSONArray()
    private var isCubeForced = false

    private fun combineConditions(code: String, json: JSONArray){
        var reqType = json[0]
        val reqValue = json[1]
        val reqMulti = json[2]
        val upType = (json[3] ?: "") as String
        val upValue = (json[4] ?: 0.0) as Double

        if(reqType == null){  // 조건부가 없는 표기일 경우
            // 레벨 쿨감 스증 쿨회복 피해증가
            if(upType=="피해증가"){
                totalDamage += upValue
            }
            try{
                val nowArray = damageCondition.parseLevelArrayOption(upType, upValue, isCubeForced)
                if(upType.contains("레벨")){
                    listArrayLeveling.add(nowArray)
                }else if(upType.contains("쿨회복")){
                    listArrayCoolDown.add(nowArray)
                }else if(upType.contains("쿨감")){
                    listArrayCoolRecover.add(nowArray)
                }else if(upType.contains("스증")){
                    listArraySkillDamage.add(nowArray)
                }
            }catch (ignored: Exception){}
        }else{
            reqType = reqType as String
            if(reqValue=="tg"){
                var isCondition = true
                val nowJson = JSONObject()
                nowJson["code"] = code
                nowJson["reqType"] = reqType
                if(upType=="피해증가"){
                    nowJson["applyType"] = "피해증가"
                    nowJson["apply"] = upValue
                }else if(upType.contains("속성강화")){
                    nowJson["applyType"] = "속성강화"
                    val elementArray = JSONArray()
                    damageCondition.parseElementOption(upType, upValue).forEach { v -> elementArray.add(v)}
                    nowJson["apply"] = elementArray
                }else if(upType.contains("레벨")){
                    nowJson["applyType"] = "레벨"
                    val nowArray = damageCondition.parseLevelArrayOption(upType, upValue, isCubeForced)
                    val applyJsonArray = JSONArray()
                    nowArray.forEach { v -> applyJsonArray.add(v) }
                    nowJson["apply"] = applyJsonArray
                }else if(upType.contains("쿨회복")){
                    nowJson["applyType"] = "쿨회복"
                    val nowArray = damageCondition.parseLevelArrayOption(upType, upValue, isCubeForced)
                    val applyJsonArray = JSONArray()
                    nowArray.forEach { v -> applyJsonArray.add(v) }
                    nowJson["apply"] = applyJsonArray
                }else if(upType.contains("쿨감")){
                    nowJson["applyType"] = "쿨감"
                    val nowArray = damageCondition.parseLevelArrayOption(upType, upValue, isCubeForced)
                    val applyJsonArray = JSONArray()
                    nowArray.forEach { v -> applyJsonArray.add(v) }
                    nowJson["apply"] = applyJsonArray
                }else if(upType.contains("스증")){
                    nowJson["applyType"] = "스증"
                    val nowArray = damageCondition.parseLevelArrayOption(upType, upValue, isCubeForced)
                    val applyJsonArray = JSONArray()
                    nowArray.forEach { v -> applyJsonArray.add(v) }
                    nowJson["apply"] = applyJsonArray
                }else if(upType.contains("데미지")){
                    nowJson["applyType"] = "상변데미지"
                    nowJson["statusType"] = upType.split(" ")[0]
                    nowJson["apply"] = upValue
                }else if(upType=="커맨드 효과"){
                    nowJson["applyType"] = "쿨감"
                    val nowArray = arrayOf(
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                        0.02, 0.02, 0.02, 0.0, 0.0, 0.02, 0.02,
                        0.05, 0.05, 0.0, 0.05, 0.0
                    )
                    val applyJsonArray = JSONArray()
                    nowArray.forEach { v -> applyJsonArray.add(v * upValue) }
                    nowJson["apply"] = applyJsonArray
                }else{
                    isCondition = false
                }
                if(isCondition){
                    jsonConditionToggle.add(nowJson)
                }
            }else if(reqValue=="arr"){

            }else{
                if(upType.contains("HP n%") || upType.contains("MP n%")){
                    val strArray = reqType.split(" ")
                    println(strArray.toString())
                    val reg = strArray[strArray.size-1]
                    if(reg=="감소"){

                    }else if(reg=="미만" || reg=="이하"){

                    }else if(reg=="이상" || reg=="초과"){

                    }else if(reg=="구간"){

                    }

                }
            }
        }
    }

    var arrayLeveling = Array<Double>(19){0.0}
    var arrayCoolDown = Array<Double>(19){0.0}
    var arrayCoolRecover = Array<Double>(19){0.0}
    var arraySkillDamage = Array<Double>(19){1.0}


    private var totalDamage = 0.0

    private var optionLevel = 2.6
    private var titlePetPercent = 0.33
    private val baseElement = 13.0
    private var customElement = 15.0+35*3+30+6+25+7

    private fun calculateDamage(){

        for(now in listArrayLeveling){
            for(i in now.indices) arrayLeveling[i] += now[i]
        }
        for(now in listArrayCoolDown){
            for(i in now.indices) arrayCoolDown[i] = 1-(1-arrayCoolDown[i]) * (1-now[i])
        }
        for(now in listArrayCoolRecover){
            for(i in now.indices) arrayCoolRecover[i] += now[i]
        }
        for(now in listArraySkillDamage){
            for(i in now.indices) arraySkillDamage[i] *= (1+now[i])
        }
        println("arrayLeveling: "+arrayLeveling.contentToString())
        println("arrayCoolDown: "+arrayCoolDown.contentToString())
        println("arrayCoolRecover: "+arrayCoolRecover.contentToString())
        println("arraySkillDamage: "+arraySkillDamage.contentToString())
        println(jsonConditionToggle.toJSONString())

        for(d in arrayUpDamage){
            totalDamage += (d * optionLevel)
        }

        var maxElement = 0.0
        var minElement = 9999.0
        for(key in elementKey){
            val nowElement = simpleSumOptions[key] ?: 0.0
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
        val damage105 = (totalDamage / 1000.0) * (1+titlePetPercent)

        val sumDamage = ((damage100 + damage105) * skillDamage * stat * atk *
                (1.05 + 0.0045 * (maxElement+baseElement+customElement))
                )

        val statusDamageMap = HashMap<String, Double>()
        var transRatio = 0.0
        statusTrans.forEach { (key, value) ->
            transRatio += value
            statusDamageMap[key] = sumDamage * value * (1 + (statusDamage[key] ?: 0.0))
        }

        statusDamageMap["본뎀"] = sumDamage * (1 - transRatio)


        println("총 피증: $totalDamage")
        println("sumDamage: $sumDamage")

    }

}