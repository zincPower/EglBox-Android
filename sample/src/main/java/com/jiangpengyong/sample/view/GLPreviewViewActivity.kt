package com.jiangpengyong.sample.view

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jiangpengyong.eglbox_core.view.GLPreviewView
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GLPreviewViewActivity : AppCompatActivity() {
    private lateinit var previewView: GLPreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gl_preivew_view)
        previewView = findViewById(R.id.preview_view)

        findViewById<View>(R.id.image1).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                BitmapFactory.decodeFile(File(App.context.filesDir, "images/test_image/test_image_horizontal.png").absolutePath).let { bitmap ->
                    previewView.setImage(bitmap, true)
                }
                previewView.requestRender()
            }
        }
    }
}