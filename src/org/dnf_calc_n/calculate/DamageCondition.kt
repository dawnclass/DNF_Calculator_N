package org.dnf_calc_n.calculate

class DamageCondition {

    private val levelIndex = arrayOf(
        "1", "5", "10", "15", "20", "25", "30", "35", "40", "45", "48", "50",
        "60", "70", "75", "80", "85", "95", "100"
    )

    fun parseElementOption(upType: String, upValue: Double) : Array<Double>{
        val arrayReturn = Array<Double>(5){0.0}
        val strArray = upType.split(" ")
        val addIndex = ArrayList<Int>()
        if(strArray[0].contains("화")) addIndex.add(0)
        if(strArray[0].contains("수")) addIndex.add(1)
        if(strArray[0].contains("명")) addIndex.add(2)
        if(strArray[0].contains("암")) addIndex.add(3)
        if(strArray[0]=="모든") {
            addIndex.add(0)
            addIndex.add(1)
            addIndex.add(2)
            addIndex.add(3)
        }
        if(strArray[0]=="높은") addIndex.add(4)
        for(i in addIndex){
            arrayReturn[i] = upValue
        }
        return arrayReturn
    }

    fun getStatusIndex(i: Int): String {
        when(i){
            0 -> return "출혈"
            1 -> return "중독"
            2 -> return "화상"
            3 -> return "감전"
            4 -> return "빙결"
            5 -> return "둔화"
            6 -> return "기절"
            7 -> return "저주"
            8 -> return "암흑"
            9 -> return "석화"
            10 -> return "수면"
            11 -> return "혼란"
            12 -> return "구속"
        }
        return ""
    }

    fun parseStatusResistOption(upType: String, upValue: Double) : Array<Double>{
        val arrayReturn = Array<Double>(13){0.0}  //출혈 중독 화상 감전 빙결 둔화 기절 저주 암흑 석화 수면 혼란 구속
        val strArray = upType.split(" ")
        when(strArray[0]){
            "출혈" -> arrayReturn[0] = upValue
            "중독" -> arrayReturn[1] = upValue
            "화상" -> arrayReturn[2] = upValue
            "감전" -> arrayReturn[3] = upValue
            "빙결" -> arrayReturn[4] = upValue
            "둔화" -> arrayReturn[5] = upValue
            "기절" -> arrayReturn[6] = upValue
            "저주" -> arrayReturn[7] = upValue
            "암흑" -> arrayReturn[8] = upValue
            "석화" -> arrayReturn[9] = upValue
            "수면" -> arrayReturn[10] = upValue
            "혼란" -> arrayReturn[11] = upValue
            "구속" -> arrayReturn[12] = upValue
            "모든" -> {
                for(i in arrayReturn.indices){
                    arrayReturn[i] = upValue
                }
            }
        }
        return arrayReturn
    }

    fun parseSpeedOption(upType: String, upValue: Double) : Array<Double>{
        val arrayReturn = Array<Double>(3){0.0}  //공캐이
        val strArray = upType.split(" ")
        if(strArray[0] == "모든"){
            arrayReturn[0] = upValue
            arrayReturn[1] = upValue
            arrayReturn[2] = upValue
        }else if(strArray[0] == "공캐"){
            arrayReturn[0] = upValue
            arrayReturn[1] = upValue * 1.5
        }else{
            if(strArray[0].contains("공")){
                arrayReturn[0] = upValue
            }
            if(strArray[0].contains("캐")){
                arrayReturn[1] = upValue
            }
            if(strArray[0].contains("이")){
                arrayReturn[2] = upValue
            }
        }
        return arrayReturn
    }

    fun parseLevelArrayOption(str: String, upValue: Double, arrayCubeUse: Array<Int>) : Array<Double>{
        val arrayReturn = Array<Double>(19){0.0}
        if(str.contains(" ")){
            val strArray = str.split(" ")
            if(strArray[0].contains("~")){
                val levelRange = strArray[0].split("~")
                val start = levelIndex.indexOf(levelRange[0])
                val end = levelIndex.indexOf(levelRange[1])
                for(i in start .. end){
                    arrayReturn[i] += upValue
                }
            }else if(strArray[0].contains("각성")){
                if(strArray[0]=="비각성"){
                    arrayReturn[11] -= upValue
                    arrayReturn[16] -= upValue
                    arrayReturn[18] -= upValue
                    for(i in arrayReturn.indices) arrayReturn[i] += upValue
                }else if(strArray[0]=="각성"){
                    arrayReturn[11] = upValue
                    arrayReturn[16] = upValue
                    arrayReturn[18] = upValue
                }
            }else if(strArray[0].contains("무큐")){
                if(strArray[0]=="비무큐"){
                    for(i in arrayCubeUse.indices){
                        if(arrayCubeUse[i]==0) arrayReturn[i] = upValue
                    }
                }else if(strArray[0]=="무큐"){
                    for(i in arrayCubeUse.indices){
                        if(arrayCubeUse[i]!=0) arrayReturn[i] = upValue
                    }
                }
            }else{
                val i = levelIndex.indexOf(strArray[0])
                arrayReturn[i] = upValue
            }
        }else{
            for(i in arrayReturn.indices){
                arrayReturn[i] = upValue
            }
        }
        var isUlt = true
        if(str.contains("스증") && str.contains("(각X)")){
            isUlt = false
        } else if(str.contains("쿨감") || str.contains("쿨회복")){
            isUlt = false
            if(str.contains("(각)")){
                isUlt = true
            }
        }
        if(!isUlt){
            arrayReturn[11] = 0.0
            arrayReturn[16] = 0.0
            arrayReturn[18] = 0.0
        }
        return arrayReturn
    }

    fun calculateCubeUse(arrayEquipment: Array<String>) : Array<Int>{
        val arrayCubeUse = arrayOf(
            0, 0, 0, 0, 0, 0, 0, 1, 1, 2, 0, 5, 2, 3, 3, 5, 10, 7, 15
        )
        if(arrayEquipment.contains("14062")){
            for(i in arrayCubeUse.indices) if(arrayCubeUse[i]==0) arrayCubeUse[i]+=2
        }
        if(arrayEquipment.contains("33062")){
            for(i in arrayCubeUse.indices) if(arrayCubeUse[i]!=0) arrayCubeUse[i]+=2
        }

        if(arrayEquipment.contains("21062")){
            for(i in arrayCubeUse.indices) arrayCubeUse[i]*=8
        }
        return arrayCubeUse
    }

}