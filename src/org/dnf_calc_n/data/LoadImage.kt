package org.dnf_calc_n.data

import java.io.*
import java.util.zip.ZipInputStream
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import kotlin.collections.set


class LoadImage {

    init {

    }

    fun loadAllImageExtra(): HashMap<String, ImageIcon> {
        val imageExtraMap = HashMap<String, ImageIcon>()
        val path = "resources/ext_img"
        val fileList = File(path).list()
        // println(fileList.contentToString())
        for (fileName in fileList) {
            imageExtraMap[fileName.substring(0, fileName.length - 4)] =
                ImageIcon("$path/$fileName")
            //
        // imageExtraMap[fileName.substring(0, fileName.length - 4)] = ImageIcon("$path/$fileName")
        }
        return imageExtraMap
    }

    fun loadAllImageItem(): HashMap<String, ImageIcon> {
        val imageItemMap = HashMap<String, ImageIcon>()
        val path = "resources/icon_item"
        val fileList = File(path).list()
        // println(fileList.contentToString())
        for (fileName in fileList) {
            imageItemMap[fileName.substring(0, fileName.length - 4)] =
                ImageIcon("$path/$fileName")
            // imageItemMap[fileName.substring(0, fileName.length - 4)] = ImageIcon("$path/$fileName")
        }
        return imageItemMap
    }

}