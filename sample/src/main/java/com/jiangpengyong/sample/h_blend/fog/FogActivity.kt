package com.jiangpengyong.sample.h_blend.fog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.view.GLPreviewView
import com.jiangpengyong.eglbox_sample.R

/**
 * @author jiang peng yong
 * @date 2024/12/3 22:36
 * @email 56002982@qq.com
 * @des 雾
 */
class FogActivity : AppCompatActivity() {
    private lateinit var previewView: GLPreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blend)

        previewView = findViewById(R.id.gl_preview_view)

    }
}