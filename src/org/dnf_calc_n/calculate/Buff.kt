package org.dnf_calc_n.calculate

import org.dnf_calc_n.Common
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.lang.Math.round
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class Buff(private var equipmentData: JSONObject, private var customData: JSONObject) {

    var isBuff = false
    fun getIsBuff(): Boolean{
        return isBuff
    }

    private lateinit var job : String
    private lateinit var arrayEquipment : Array<String>
    private var arrayCustomOption = ArrayList<String>()
    private val common = Common()
    private var buffSkillData : JSONObject = common.loadJsonObject("resources/data/buff_data.json")
    lateinit var mapResult : HashMap<String, String>

    private lateinit var arrayUpBuff : Array<Double>
    private var basicBuff = 0.0

    private fun resetData(){
        isBuff = false
        mapSkillData.clear()
        mapResult = HashMap()
        basicBuff = 0.0
        arrayUpBuff = Array(4){0.0}
        arrayEquipment = Array(13){""}
        arrayCustomOption.clear()
        basicBuff = 0.0
        mapValueSum.clear()
    }

    private val mapSkillData = HashMap<String, JSONArray>()
    private var blessEff = 0.0
    private var blessX = 4350.0
    private var blessY = 3500.0
    private var blessZ = 0.0000379
    private var cruxX = 5250.0
    private var cruxY = 5000.0
    private var cruxZ = 0.000025
    private lateinit var jsonSave: JSONObject

    private var blessAria = 1.0
    fun startBuffCalculate(mapEquipment: HashMap<String,String>) : Boolean{
        resetData()
        jsonSave = common.loadJsonObject("cache/selected.json")
        job = "${(jsonSave["jobType"] ?: return false) as String} ${(jsonSave["job"] ?: return false) as String}"
        mapSkillData["각성스탯"] = buffSkillData["Ult1Stat"] as JSONArray
        mapSkillData["진각패"] = buffSkillData["PasNeo"] as JSONArray
        when(job){
            "프리스트(남) 크루세이더" -> {
                blessAria = 1.0
                blessEff = 620.0
                blessZ = 0.0000357
                mapSkillData["축스탯"] = buffSkillData["SaintBlessStat"] as JSONArray
                mapSkillData["축공"] = buffSkillData["SaintBlessAtk"] as JSONArray
                mapSkillData["전직패"] = buffSkillData["SaintPas0"] as JSONArray
                mapSkillData["보징"] = buffSkillData["SaintProtection"] as JSONArray
                mapSkillData["크크"] = buffSkillData["SaintCrossAtk"] as JSONArray
                mapSkillData["1각패"] = buffSkillData["SaintPas1BuffStat"] as JSONArray
                mapSkillData["1각패오라"] = buffSkillData["SaintPas1AuraStat"] as JSONArray
                mapSkillData["2각"] = buffSkillData["SaintUlt2"] as JSONArray
            }
            "프리스트(여) 크루세이더" -> {
                blessAria = 1.15
                blessEff = 665.0
                blessZ = 0.0000379
                mapSkillData["축스탯"] = buffSkillData["SeraphimBlessStat"] as JSONArray
                mapSkillData["축공"] = buffSkillData["SeraphimBlessAtk"] as JSONArray
                mapSkillData["전직패"] = buffSkillData["SeraphimPas0"] as JSONArray
                mapSkillData["그크"] = buffSkillData["SeraphimGrandStat"] as JSONArray
                mapSkillData["1각패"] = buffSkillData["SeraphimPas1"] as JSONArray
                mapSkillData["1각패오라"] = mapSkillData["1각패"] as JSONArray
                mapSkillData["2각패"] = buffSkillData["SeraphimPas2"] as JSONArray
            }
            "마법사(여) 인챈트리스" -> {
                blessEff = 665.0
                blessAria = 1.15 * 1.25
                blessZ = 0.0000379
                mapSkillData["축스탯"] = buffSkillData["HecateBlessStat"] as JSONArray
                mapSkillData["축공"] = buffSkillData["HecateBlessAtk"] as JSONArray
                mapSkillData["전직패"] = buffSkillData["HecatePas0"] as JSONArray
                mapSkillData["1각패"] = buffSkillData["HecatePas1"] as JSONArray
                mapSkillData["1각패오라"] = mapSkillData["1각패"] as JSONArray
                mapSkillData["2각패"] = buffSkillData["HecatePas2"] as JSONArray
            }
            else -> return false
        }
        val optionLevelString = (jsonSave["optionLv"] ?: "60") as String
        println("optionLevelString = $optionLevelString")
        optionLevel = when(optionLevelString){
            "20" -> 1.5497
            "40" -> 2.0885
            "60" -> 2.6088
            "80" -> 3.1106
            else -> 2.6088
        }
        println("optionLevel = $optionLevel")

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
        loadCustomData()
        loadCustomOptionData()
        loadEquipmentData()
        isBuff = true
        return true
    }

    private val customStatKey = arrayOf(
        "enchantBuffArmor12", "enchantBuffArmor345",
        "enchantBuffAccessory", "enchantBuffSub", "enchantBuffMagic",
        "enchantBuffEarring", "buffAmp"
    )
    private fun loadCustomData(){
        customStatInt = 0.0
        customStatMental = 0.0
        customBlessLv = 0
        customCruxLv = 0
        for(key in customStatKey){
            val nowString = (jsonSave[key] ?: continue) as String
            if(nowString == "") continue
            if(key=="buffAmp"){
                var nowStat = 0.0
                when(nowString){
                    "0증" -> nowStat = 0.0
                    "8증" -> nowStat = 61.0 * 12.0
                    "10증" -> nowStat = 91.0 * 12.0
                    "11증" -> nowStat = 108.0 * 12.0
                    "12증" -> nowStat = 130.0 * 12.0
                }
                customStatInt += nowStat
                customStatMental += nowStat
                continue
            }
            var nowInt = 0.0
            var nowMental = 0.0
            if("/" in nowString){
                val strArray = nowString.split("/")
                nowInt += strArray[0].toDouble()
                nowMental += strArray[1].toDouble()
            }else{
                val strArray = nowString.split(" ")
                nowInt += strArray[1].toDouble()
                nowMental += strArray[1].toDouble()
            }
            var nowMulti = 1.0
            when(key){
                "enchantBuffArmor12"->{
                    nowMulti+=1.0
                    if(arrayEquipment.contains("11052")) nowMulti += 1.0
                    if(arrayEquipment.contains("12052")) nowMulti += 1.0
                }
                "enchantBuffArmor345"->{
                    nowMulti+=2.0
                    if(arrayEquipment.contains("13052")) nowMulti += 1.0
                    if(arrayEquipment.contains("14052")) nowMulti += 1.0
                    if(arrayEquipment.contains("15052")) nowMulti += 1.0
                }
                "enchantBuffAccessory"->{
                    nowMulti+=2.0
                    if(arrayEquipment.contains("21052")) nowMulti += 1.0
                    if(arrayEquipment.contains("22052")) nowMulti += 1.0
                    if(arrayEquipment.contains("23052")) nowMulti += 1.0
                }
                "enchantBuffSub"-> if(arrayEquipment.contains("31052")) nowMulti += 1.0
                "enchantBuffMagic"->if(arrayEquipment.contains("32052")) nowMulti += 1.0
                "enchantBuffEarring"->if(arrayEquipment.contains("33052")) nowMulti += 1.0
            }
            customStatInt += nowMulti * nowInt
            customStatMental += nowMulti * nowMental
        }
        try{
            customStatInt += ((jsonSave["buffExtraStat"] ?: "0") as String).toDouble()
            customStatMental += ((jsonSave["buffExtraStat"] ?: "0") as String).toDouble()
        }catch (ignored: NumberFormatException){}
        try{
            customBlessLv += ((jsonSave["buffLvBless"] ?: "7") as String).toInt()
        }catch (ignored: NumberFormatException){}
        try{
            customCruxLv += ((jsonSave["buffLvCrux"] ?: "3") as String).toInt()
        }catch (ignored: NumberFormatException){}

        //println("customStatInt = $customStatInt")
        //println("customBlessLv = $customBlessLv")
        //println("customCruxLv = $customCruxLv")
    }

    private val mapValueSum = HashMap<String, Double>()
    private val simpleList = arrayOf("지능", "수은", "계시", "1각+", "전직패", "신념", "신실", "축렙", "각렙")
    private val complexList = arrayOf("축스탯%", "축공%", "1각%")

    private fun loadEquipmentData (){
        for(code in arrayEquipment){
            val nowJson : JSONObject = (equipmentData[code] ?: continue) as JSONObject
            for(key in simpleList){
                mapValueSum[key] = (mapValueSum[key] ?: 0.0) + (nowJson[key] ?: 0.0) as Double
            }
            for(key in complexList){
                mapValueSum[key] = (1 + (mapValueSum[key] ?: 0.0)) * (1 + ((nowJson[key] ?: 0.0) as Double)) - 1.0
            }
            basicBuff += (nowJson["기초버프"] ?: 0.0) as Double
            val upBuff : JSONArray = nowJson["옵션버프"] as JSONArray
            for(i in 0 until upBuff.size){
                arrayUpBuff[i] += upBuff[i] as Double
            }
        }
        // mapValueSum.forEach { t, u -> println("$t : $u") }
        // println("basicBuff= $basicBuff")
        // println("upLvBless= $upLvBless")
        // println("upLvCrux= $upLvCrux")
        // println("arrayUpBuff= ${arrayUpBuff.contentToString()}")
        calculateBuff()
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
            val upBuff : Double = nowJson["버프"] as Double
            println("upBuff = $upBuff")
            arrayUpBuff[0] += upBuff
        }

    }

    private var optionLevel = 2.7

    private val BASIC_STAT_INT = 5644.0
    private val BASIC_STAT_Mental = 5644.0
    private var customStatInt = 0.0
    private var customStatMental = 0.0
    private val DIFFERENCE_STAT = 44

    private val BASIC_BLESS_LV = 10
    private var customBlessLv = 7
    private val BASIC_CRUX_LV = 14
    private var customCruxLv = 3

    private val neoUltLv = 4.0

    private val pas0Lv = 34
    private val protectionLv = 10
    private val crossLv = 46
    private val grandCrossLv = 36
    private val pas1Lv = 23
    private val pas2Lv = 14
    private val ult2Lv = 7
    private val pasNeoLv = 7

    var levelingArray = Array<Double>(19){0.0}

    var additionalDealerStat = 0.0

    fun calculateBuff(){
        // println("버퍼 계산 시작")
        println("levelingArray = ${levelingArray.contentToString()}")
        // mapValueSum.forEach { (t, v) -> println("$t = $v") }
        additionalDealerStat = 0.0
        var upStat = 0.0
        var upStatFix = 0.0
        var auraStat = 0.0
        var auraAtk = 0.0
        val pas0Lv = this.pas0Lv + levelingArray[3].toInt() + (mapValueSum["전직패"] ?: 0.0).toInt()
        val blessLv = (mapValueSum["축렙"] ?: 0.0).toInt() + customBlessLv + BASIC_BLESS_LV + (levelingArray[6].toInt())
        val cruxLv = (mapValueSum["각렙"] ?: 0.0).toInt() + customCruxLv + BASIC_CRUX_LV + (levelingArray[11].toInt())
        val blessStat = mapSkillData["축스탯"]!![blessLv] as Double
        val blessStatPercent = (mapValueSum["축스탯%"] ?: 0.0)
        val blessAtk = mapSkillData["축공"]!![blessLv] as Double
        val blessAtkPercent = (mapValueSum["축공%"] ?: 0.0)
        val cruxStat = mapSkillData["각성스탯"]!![cruxLv] as Double
        val cruxStatFix = (mapValueSum["1각+"] ?: 0.0)
        val cruxStatPercent = (mapValueSum["1각%"] ?: 0.0)

        var basicStat = 0.0

        auraStat+=(mapSkillData["1각패오라"]!![pas1Lv+levelingArray[10].toInt()] as Double)
        upStat += ((mapSkillData["전직패"]!![pas0Lv] as Double) -
                (mapSkillData["전직패"]!![this.pas0Lv] as Double))
        upStat += ((mapSkillData["1각패"]!![pas1Lv+levelingArray[10].toInt()] as Double) -
                (mapSkillData["1각패"]!![pas1Lv] as Double))
        upStat += ((mapSkillData["진각패"]!![pasNeoLv+levelingArray[17].toInt()] as Double) -
                (mapSkillData["진각패"]!![pasNeoLv] as Double))

        when(job){
            "프리스트(남) 크루세이더"->{
                basicStat = BASIC_STAT_Mental + customStatMental
                upStatFix+=(mapValueSum["신념"] ?: 0.0)
                upStat+=((mapSkillData["보징"]!![protectionLv + levelingArray[5].toInt()] as Double) -
                        (mapSkillData["보징"]!![protectionLv] as Double))
                upStat+=((mapSkillData["2각"]!![ult2Lv + levelingArray[16].toInt()] as Double)*24.0 -
                        (mapSkillData["2각"]!![ult2Lv] as Double)*24.0)
                auraAtk+=(mapSkillData["크크"]!![crossLv + levelingArray[5].toInt()] as Double)
                upStatFix+=(mapValueSum["수은"] ?: 0.0)
            }
            "프리스트(여) 크루세이더"->{
                basicStat = BASIC_STAT_INT + customStatInt
                auraStat+=(mapValueSum["신실"] ?: 0.0)
                auraStat+=(mapSkillData["그크"]!![grandCrossLv + levelingArray[9].toInt()] as Double)
                upStat += ((mapSkillData["2각패"]!![pas2Lv+levelingArray[14].toInt()] as Double) -
                        (mapSkillData["2각패"]!![pas2Lv] as Double))
                upStatFix+=(mapValueSum["신실"] ?: 0.0)
                upStatFix+=(mapValueSum["계시"] ?: 0.0)
                upStat+=(mapValueSum["지능"] ?: 0.0)
                additionalDealerStat+=upStat+upStatFix
            }
            "마법사(여) 인챈트리스"->{
                basicStat = BASIC_STAT_INT + customStatInt
                auraStat+=(mapValueSum["신실"] ?: 0.0)
                upStatFix+=(mapValueSum["신실"] ?: 0.0)
                upStatFix+=(mapValueSum["계시"] ?: 0.0)
                upStat+=(mapValueSum["지능"] ?: 0.0)
                upStat += ((mapSkillData["2각패"]!![pas2Lv+levelingArray[14].toInt()] as Double) -
                        (mapSkillData["2각패"]!![pas2Lv] as Double))
                additionalDealerStat+=upStat+upStatFix
            }
        }

        var totalBuff = basicBuff
        for(b in arrayUpBuff){
            totalBuff += (b * optionLevel)
        }
        val bless100Apply = ((basicStat+upStat+upStatFix)/blessEff+1.0)
        val crux100Apply = ((basicStat+DIFFERENCE_STAT+upStat+upStatFix)/750.0+1.0)
        val bless100Stat = (bless100Apply * (1 + blessStatPercent) * blessStat).roundToInt()
        val bless100Atk = (bless100Apply * (1+ blessAtkPercent) * blessAtk).roundToInt()
        val crux100Stat = (crux100Apply * (1 + cruxStatPercent) * (cruxStat+cruxStatFix)).roundToInt()

        val bless105Apply = if(totalBuff==0.0){
            0.0
        }else{
            ((basicStat+upStat+blessX)/blessEff+1.0)*(totalBuff+blessY)*blessZ
        }
        val crux105Apply = if(totalBuff==0.0){
            0.0
        }else{
            ((basicStat+DIFFERENCE_STAT+upStat+cruxX)/750.0+1.0)*(totalBuff+cruxY)*cruxZ
        }
        val bless105Stat = (bless105Apply * blessStat).roundToInt()
        val bless105Atk = (bless105Apply * blessAtk).roundToInt()
        val crux105Stat = (crux105Apply * cruxStat).roundToInt()

        val finalBlessStat = ((bless100Stat + bless105Stat) * blessAria).toInt()
        val finalBlessAtk = ((bless100Atk + bless105Atk) * blessAria).toInt()
        val finalCruxStat = ((crux100Stat + crux105Stat) * (1.08+0.01*neoUltLv)).toInt()

        val finalBuff = ((16250.0+finalCruxStat+finalBlessStat+auraStat)/250.0 *
                (2650.0+finalBlessAtk+auraAtk) / 10.0).toInt()

        println("총 버프력= $totalBuff")
        println("적용 지능 = ${basicStat+upStat}")
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