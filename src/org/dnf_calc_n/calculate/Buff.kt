package org.dnf_calc_n.calculate

import org.dnf_calc_n.Common
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.lang.Math.round
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class Buff(private var equipmentData: JSONObject) {

    private lateinit var job : String
    private lateinit var arrayEquipment : Array<String>
    private val common = Common()
    private var buffSkillData : JSONObject = common.loadJsonObject("resources/data/buff_data.json")
    lateinit var mapResult : HashMap<String, String>

    private lateinit var arrayUpBuff : Array<Double>
    private var basicBuff = 0.0
    private var upLvBless = 0.0
    private var upLvCrux = 0.0

    private fun resetData(){
        mapResult = HashMap()
        basicBuff = 0.0
        arrayUpBuff = Array(4){0.0}
        arrayEquipment = Array(13){""}
        basicBuff = 0.0
        upLvBless = 0.0
        upLvCrux = 0.0
    }

    private lateinit var skillBlessStat: JSONArray
    private lateinit var skillBlessAtk: JSONArray
    private lateinit var skillCruxStat: JSONArray
    private var blessAria = 1.0
    fun startBuffCalculate(mapEquipment: HashMap<String,String>) : Boolean{
        resetData()
        val jsonSave = common.loadJsonObject("cache/selected.json")
        job = (jsonSave["job"] ?: return false) as String
        skillCruxStat = buffSkillData["CruxStat"] as JSONArray
        when(job){
            "크루세이더(남)" -> {
                blessAria = 1.0
                skillBlessStat = buffSkillData["SaintBlessStat"] as JSONArray
                skillBlessAtk = buffSkillData["SaintBlessAtk"] as JSONArray
            }
            "크루세이더(여)" -> {
                blessAria = 1.15
                skillBlessStat = buffSkillData["SeraphimBlessStat"] as JSONArray
                skillBlessAtk = buffSkillData["SeraphimBlessAtk"] as JSONArray
            }
            "인챈트리스" -> {
                blessAria = 1.15 * 1.25
                skillBlessStat = buffSkillData["HecateBlessStat"] as JSONArray
                skillBlessAtk = buffSkillData["HecateBlessAtk"] as JSONArray
            }
            else -> return false
        }
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

    private fun loadEquipmentData (){
        for(code in arrayEquipment){
            val nowJson : JSONObject = (equipmentData[code] ?: continue) as JSONObject
            basicBuff += (nowJson["기초버프"] ?: 0.0) as Double
            upLvBless += (nowJson["축렙"] ?: 0.0) as Double
            upLvCrux += (nowJson["각렙"] ?: 0.0) as Double
            val upBuff : JSONArray = nowJson["옵션버프"] as JSONArray
            for(i in 0 until upBuff.size){
                arrayUpBuff[i] += upBuff[i] as Double
            }
        }
        // println("basicBuff= $basicBuff")
        // println("upLvBless= $upLvBless")
        // println("upLvCrux= $upLvCrux")
        // println("arrayUpBuff= ${arrayUpBuff.contentToString()}")
        calculateBuff()
    }

    private var optionLevel = 2.7

    private val BASIC_STAT_INT = 6684.0
    private val BASIC_BLESS_LV = 10+3+3+1
    private val BASIC_CRUX_LV = 14+2+1
    private val DIFFERENCE_STAT = 44
    private var neoUltLv = 4.0
    private fun calculateBuff(){
        val upStatInt = 0.0
        val upStatIntFix = 0.0
        val blessStat = skillBlessStat[BASIC_BLESS_LV+upLvBless.toInt()] as Double
        val blessStatPercent = 0.0
        val blessAtk = skillBlessAtk[BASIC_BLESS_LV+upLvBless.toInt()] as Double
        val blessAtkPercent = 0.0
        val cruxStat = skillCruxStat[BASIC_CRUX_LV+upLvCrux.toInt()] as Double
        val cruxStatFix = 0.0
        val cruxStatPercent = 0.0

        var totalBuff = basicBuff
        for(b in arrayUpBuff){
            totalBuff += (b * optionLevel)
        }
        val bless100Apply = ((BASIC_STAT_INT+upStatInt+upStatIntFix)/665.0+1.0)
        val crux100Apply = ((BASIC_STAT_INT+DIFFERENCE_STAT+upStatInt+upStatIntFix)/750.0+1.0)
        val bless100Stat = (bless100Apply * (1 + blessStatPercent / 100.0) * blessStat).roundToInt()
        val bless100Atk = (bless100Apply * (1+blessAtkPercent/100.0) * blessAtk).roundToInt()
        val crux100Stat = (crux100Apply * (1 + cruxStatPercent / 100.0) * (cruxStat+cruxStatFix)).roundToInt()

        val bless105Apply = if(totalBuff==0.0){
            0.0
        }else{
            ((BASIC_STAT_INT+upStatInt+4350.0)/665.0+1.0)*(totalBuff+3500.0)*0.0000379
        }
        val crux105Apply = if(totalBuff==0.0){
            0.0
        }else{
            ((BASIC_STAT_INT+DIFFERENCE_STAT+upStatInt+5250.0)/750.0+1.0)*(totalBuff+5000.0)*0.000025
        }
        val bless105Stat = (bless105Apply * blessStat).roundToInt()
        val bless105Atk = (bless105Apply * blessAtk).roundToInt()
        val crux105Stat = (crux105Apply * cruxStat).roundToInt()

        val finalBlessStat = ((bless100Stat + bless105Stat) * blessAria).toInt()
        val finalBlessAtk = ((bless100Atk + bless105Atk) * blessAria).toInt()
        val finalCruxStat = ((crux100Stat + crux105Stat) * (1.08+0.01*neoUltLv)).toInt()

        val finalBuff = ((16250.0+finalCruxStat+finalBlessStat)/250.0 * (2650.0+finalBlessAtk) / 10.0).toInt()

        println("총 버프력= $totalBuff")
        println("축 스탯= $finalBlessStat")
        println("축 앞뎀= $finalBlessAtk")
        println("각 스탯= $finalCruxStat")
        println("최종 버프력= $finalBuff")

        mapResult["total"] = totalBuff.toInt().toString()
        mapResult["blessStat"] = finalBlessStat.toString()
        mapResult["blessAtk"] = finalBlessAtk.toString()
        mapResult["cruxStat"] = finalCruxStat.toString()
        mapResult["score"] = finalBuff.toString()

    }




}