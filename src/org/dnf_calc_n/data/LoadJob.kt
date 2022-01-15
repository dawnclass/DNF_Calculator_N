package org.dnf_calc_n.data

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.BufferedReader
import java.io.FileReader
import java.io.InputStreamReader

class LoadJob {

    fun getJobMap(): HashMap<String, Array<String>> {
        val jobMap = HashMap<String, Array<String>>()

        val parser = JSONParser()
        val reader = BufferedReader(FileReader("resources/ui_layout/job.json"))
        val json = parser.parse(reader) as JSONObject
        // println(json.toJSONString())

        val jobTypes = json["jobTypes"] as JSONArray
        val types = Array(jobTypes.size) { "" }
        for(i in types.indices) types[i] = jobTypes[i] as String
        jobMap["types"] = types

        val jsonJob = json["jobs"] as JSONObject
        for(nowType in jsonJob.keys){
            val nowJobs = jsonJob[nowType] as JSONArray
            val arr = Array<String>(nowJobs.size) { "" }
            for(i in arr.indices){
                arr[i] = nowJobs[i] as String
            }
            jobMap[nowType as String] = arr
        }

        return jobMap
    }

}