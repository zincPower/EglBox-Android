package com.jiangpengyong.sample

import android.app.Application
import com.jiangpengyong.sample.utils.FileUtils

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FileUtils.copyFiles(
            assetManager = assets,
            assetDir = "fonts",
            srcFolder = filesDir
        )
        FileUtils.copyFiles(
            assetManager = assets,
            assetDir = "images",
            srcFolder = filesDir
        )
    }
}