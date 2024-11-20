package com.jiangpengyong.sample.h_blend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.view.FilterCenter
import com.jiangpengyong.eglbox_core.view.GLPreviewView
import com.jiangpengyong.eglbox_sample.R

/**
 * @author jiang peng yong
 * @date 2024/11/19 08:15
 * @email 56002982@qq.com
 * @des 混合
 */
class BlendActivity : AppCompatActivity() {

    private lateinit var glPreviewView: GLPreviewView
    private var filterId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blend)

        FilterCenter.registerFilter(BlendFilter.TAG, BlendFilter::class.java)

        glPreviewView = findViewById(R.id.gl_preview_view)
        glPreviewView.post {
            glPreviewView.setBlank()
            filterId = glPreviewView.addFilter(BlendFilter.TAG)
            glPreviewView.requestRender()
        }
    }

}