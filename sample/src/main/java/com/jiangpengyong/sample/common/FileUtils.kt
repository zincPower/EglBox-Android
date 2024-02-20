package com.jiangpengyong.sample.common

import android.content.res.AssetManager
import android.util.Log
import java.io.*

/**
 * @author: jiang peng yong
 * @date: 2022/3/10 10:55 上午
 * @email: 56002982@qq.com
 * @desc: 文件工具
 */
object FileUtils {
    fun copyFiles(assetManager: AssetManager, assetDir: String, srcFolder: File): String {
        val folder = File(srcFolder, assetDir)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        try {
            val list = assetManager.list(assetDir)
            list?.forEach {
                it ?: return@forEach
                if (it.contains(".")) {
                    val file = File(folder, it)
                    val assetFileInput = assetManager.open("$assetDir/$it")
//                    Log.i(
//                        "FileUtil", "File ${file.absolutePath}: " +
//                            "file length: ${file.length()} ," +
//                            "asset length: ${assetFileInput.available()}"
//                    )
                    if (file.exists()) {
                        if (file.length() != assetFileInput.available().toLong()) {
                            file.delete()
                        } else {
                            assetFileInput.close()
                            return@forEach
                        }
                    }
                    val fos = FileOutputStream(file)
                    assetFileInput.copyTo(fos, DEFAULT_BUFFER_SIZE)
                    assetFileInput.close()
                    fos.flush()
                    fos.close()
                } else {
                    copyFiles(assetManager, "$assetDir/$it", srcFolder)
                }
            }
        } catch (e: Exception) {
            Log.e("FileUtils", "FileUtil copyFiles ", e)
        }
        return folder.absolutePath
    }
}