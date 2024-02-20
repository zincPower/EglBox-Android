package com.jiangpengyong.sample

import android.app.Application
import com.jiangpengyong.sample.common.FileUtils

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FileUtils.copyFiles(
            assetManager = assets,
            assetDir = "fonts",
            srcFolder = filesDir
        )
    }
}