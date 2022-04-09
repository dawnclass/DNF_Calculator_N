package org.dnf_calc_n.calculate

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ScoreFarming(private val equipmentData: JSONObject) {

    private val dungeonTotalScore = HashMap<String, Double>()
    private val mapOptionTotal = HashMap<String, HashMap<String, ArrayList<String>>>()
    // 부위 - 던전 - 옵션빈도리스트
    private val farmingDungeon = arrayOf(
        "나사우", "백색의땅", "베리콜", "퀸팔트", "캐니언", "이터널", "왕의요람", "예언소"
    )
    private val partIndex = arrayOf(
        "11", "12", "13", "14", "15", "21", "22", "23", "31", "32", "33"
    )

    init {
        for(part in partIndex){
            mapOptionTotal[part] = HashMap<String, ArrayList<String>>()
            for(dun in farmingDungeon){
                mapOptionTotal[part]!![dun] = ArrayList()
            }
        }
        for(dun in farmingDungeon) dungeonTotalScore[dun] = 0.0

        calculateOptionArray()
    }

    private fun calculateOptionArray(){
        for(o in equipmentData.keys){
            val code = o as String
            if(code.length != 5) continue
            val nowJson = equipmentData[o] as JSONObject
            val part = code.substring(0, 2)

            val nowOptArray = (nowJson["옵션종류"] ?: continue) as JSONArray
            val nowDrop = (nowJson["드랍"] ?: continue) as JSONArray
            for(oo in nowDrop){
                val drop = oo as String
                try{
                    for(opt in nowOptArray) {
                        mapOptionTotal[part]!![drop]!!.add(opt as String)
                        dungeonTotalScore[drop] = (dungeonTotalScore[drop] ?: 0.0) + 1.0
                    }
                }catch (ignored: NullPointerException){}
            }
        }
    }

    fun calculateOne(equipmentCode: String): HashMap<String, Double>{
        val returnMap = HashMap<String, Double>()
        for(dun in farmingDungeon){
            returnMap[dun] = 0.0
        }
        if(equipmentCode.length != 5) return returnMap
        val part = equipmentCode.substring(0, 2)
        val nowJson = equipmentData[equipmentCode] as JSONObject
        val nowOpt = (nowJson["옵션종류"] ?: return returnMap) as JSONArray
        for(opt in nowOpt){
            for(dun in farmingDungeon){
                returnMap[dun] = (returnMap[dun]!! +
                        Collections.frequency(mapOptionTotal[part]!![dun]!!, opt))
            }
        }
        return returnMap
    }

    fun calculateScore(mapEquipments : HashMap<String, String>): HashMap<String, Double>{
        val returnMap = HashMap<String, Double>()
        for(dun in farmingDungeon){
            returnMap[dun] = 0.0
        }

        for(code in mapEquipments.keys){
            val equipmentCode = mapEquipments[code] ?: continue
            if(equipmentCode.length != 5) continue
            val part = equipmentCode.substring(0, 2)
            val nowJson = equipmentData[equipmentCode] as JSONObject
            val nowOpt = (nowJson["옵션종류"] ?: continue) as JSONArray
            for(opt in nowOpt){
                for(dun in farmingDungeon){
                    returnMap[dun] = (returnMap[dun]!! +
                            Collections.frequency(mapOptionTotal[part]!![dun]!!, opt))
                }
            }
        }
        for(dun in farmingDungeon){
            returnMap[dun] = returnMap[dun]!! / dungeonTotalScore[dun]!! * 100
        }
        return returnMap
    }

}

