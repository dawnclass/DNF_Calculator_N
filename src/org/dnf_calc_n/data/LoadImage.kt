package org.dnf_calc_n.data

import java.io.File
import javax.swing.ImageIcon

class LoadImage {

    init {

    }

    fun loadAllImageExtra(): HashMap<String, ImageIcon> {
        val imageExtraMap = HashMap<String, ImageIcon>()
        val path = File("resources/ext_img")
        val fileList = path.list()
        for (fileName in fileList) {
            imageExtraMap[fileName.substring(0, fileName.length - 4)] = ImageIcon("$path/$fileName")
        }
        return imageExtraMap
    }

    fun loadAllImageItem(): HashMap<String, ImageIcon> {
        val imageItemMap = HashMap<String, ImageIcon>()
        val path = File("resources/icon_item")
        val fileList = path.list()
        for (fileName in fileList) {
            imageItemMap[fileName.substring(0, fileName.length - 4)] = ImageIcon("$path/$fileName")
        }
        return imageItemMap
    }

}