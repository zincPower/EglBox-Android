package com.jiangpengyong.sample.g_model.teapot

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.processor.preview.PreviewProcessor
import com.jiangpengyong.eglbox_core.view.FilterCenter
import com.jiangpengyong.eglbox_core.view.GLPreviewView
import com.jiangpengyong.eglbox_filter.TriangleFilter
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.eglbox_filter.BallFilter
import com.jiangpengyong.sample.g_model.common.Model3DFilter

class TeapotActivity : AppCompatActivity() {
    private lateinit var glPreviewView: GLPreviewView
    private var filterId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_3d)

        FilterCenter.registerFilter(BallFilter.TAG, BallFilter::class.java)
        FilterCenter.registerFilter(TriangleFilter.TAG, TriangleFilter::class.java)
        FilterCenter.registerFilter(Model3DFilter.TAG, Model3DFilter::class.java)

        glPreviewView = findViewById(R.id.gl_preview_view)
        glPreviewView.post {
            glPreviewView.setBlank()
            filterId = glPreviewView.addFilter(PreviewProcessor.FilterType.Process, Model3DFilter.TAG, 0)
        }

        findViewById<TextView>(R.id.reset_rotation).setOnClickListener {
            glPreviewView.resetRotation()
        }

        findViewById<TextView>(R.id.render).setOnClickListener {
            glPreviewView.requestRender()
        }
    }

    companion object {
        private const val TAG = "TeapotActivity"
    }
}