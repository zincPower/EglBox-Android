package com.jiangpengyong.sample.g_model

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Message
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.processor.preview.PreviewProcessor
import com.jiangpengyong.eglbox_core.view.FilterCenter
import com.jiangpengyong.eglbox_core.view.GLPreviewView
import com.jiangpengyong.eglbox_sample.R
import java.io.File

class TeapotActivity : AppCompatActivity() {
    private lateinit var glPreviewView: GLPreviewView
    private var filterId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FilterCenter.registerFilter(TeapotFilter.TAG, TeapotFilter::class.java)
        setContentView(R.layout.activity_teapot)
        glPreviewView = findViewById(R.id.gl_preview_view)
        glPreviewView.post {
            glPreviewView.setBlank()
            filterId = glPreviewView.addFilter(PreviewProcessor.FilterType.Process, TeapotFilter.TAG, 0)
            filterId?.let {
                glPreviewView.sendMessageToFilter(it, Message.obtain().apply {
                    what = MessageWhat.OBJ_DATA.value
                    obj = File(filesDir, "model/film/film.obj").absolutePath
                })
                glPreviewView.sendMessageToFilter(it, Message.obtain().apply {
                    what = MessageWhat.OBJ_TEXTURE.value
                    obj = (File(filesDir, "model/film/film.jpg").absolutePath.let {
                        BitmapFactory.decodeFile(it)
                    })
                })
            }
        }

        findViewById<TextView>(R.id.render).setOnClickListener {
            glPreviewView.requestRender()
        }
    }
}