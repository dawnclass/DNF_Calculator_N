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