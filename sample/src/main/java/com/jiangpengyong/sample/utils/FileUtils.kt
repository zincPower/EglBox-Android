package com.jiangpengyong.sample.utils

import android.content.res.AssetManager
import com.jiangpengyong.eglbox.logger.Logger
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {
    private const val TAG = "FileUtils"

    /**
     * 复制 asset 文件
     */
    fun copyFiles(assetManager: AssetManager, assetDir: String, srcFolder: File): String? {
        val folder = File(srcFolder, assetDir)
        try {
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    Logger.e(TAG, "Create src folder failure.")
                    return null
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Create src folder failure.", e)
            return null
        }
        var assetFileInput: InputStream? = null
        var fileOutputStream: FileOutputStream? = null
        try {
            assetManager.list(assetDir)?.forEach {
                it ?: return@forEach
                if (it.contains(".")) {
                    val file = File(folder, it)
                    assetFileInput = assetManager.open("$assetDir/$it")
                    Logger.i(
                        TAG,
                        "File path: ${file.absolutePath} [" +
                                "file length: ${file.length()} ," +
                                "asset length: ${assetFileInput?.available()}]"
                    )
                    if (file.exists()) {
                        if (file.length() != (assetFileInput?.available()?.toLong() ?: 0)) {
                            file.delete()
                        } else {
                            assetFileInput?.release()
                            return@forEach
                        }
                    }
                    fileOutputStream = FileOutputStream(file).apply {
                        assetFileInput?.copyTo(this, DEFAULT_BUFFER_SIZE)
                    }
                    assetFileInput?.release()
                    fileOutputStream?.release()
                } else {
                    copyFiles(assetManager, "$assetDir/$it", srcFolder)
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Files copy failure.", e)
            assetFileInput?.release()
            fileOutputStream?.release()
            return null
        }
        return folder.absolutePath
    }
}

fun Closeable.release() {
    try {
        this.close()
    } catch (e: java.lang.Exception) {
        Logger.e("Closeable", "Closable close failure.", e)
    }
}