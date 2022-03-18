package org.dnf_calc_n.calculate

import org.dnf_calc_n.Common
import org.json.simple.JSONArray
import org.json.simple.JSONObject

class Damage(private var equipmentData: JSONObject) {

    private lateinit var job : String
    private lateinit var arrayEquipment : Array<String>
    private val common = Common()
    private var jobSkillData : JSONObject = common.loadJsonObject("")
    lateinit var mapResult : HashMap<String, String>

    private lateinit var arrayUpDamage : Array<Double>
    private var skillDamage = 1.0

    private fun resetData(){
        mapResult = HashMap()
        arrayEquipment = Array(13){""}
        arrayUpDamage = Array(4){0.0}
        skillDamage = 1.0
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

    private fun loadEquipmentData(){
        for(code in arrayEquipment){
            val nowJson : JSONObject = (equipmentData[code] ?: continue) as JSONObject

            val upDamage : JSONArray = nowJson["upDamage"] as JSONArray
            for(i in 0 until upDamage.size){
                arrayUpDamage[i] += upDamage[i] as Double
            }

            val skillDamage = nowJson["skillDamage"] as Double
            this.skillDamage *= (skillDamage / 100.0 + 1)
        }
        calculateDamage()
    }

    private var optionLevel = 2.7

    private fun calculateDamage(){

        var totalDamage = 0.0
        for(d in arrayUpDamage){
            totalDamage += (d * optionLevel)
        }

    }

}