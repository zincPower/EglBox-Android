package com.jiangpengyong.sample.i_scene.fog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.space3d.ProjectionType
import com.jiangpengyong.eglbox_core.view.FilterCenter
import com.jiangpengyong.eglbox_core.view.GLPreviewView
import com.jiangpengyong.eglbox_sample.R

/**
 * @author jiang peng yong
 * @date 2024/12/3 22:36
 * @email 56002982@qq.com
 * @des 雾
 */
class FogActivity : AppCompatActivity() {
    private lateinit var glPreviewView: GLPreviewView
    private var filterId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene)

        FilterCenter.registerFilter(FogFilter.TAG, FogFilter::class.java)

        glPreviewView = findViewById(R.id.gl_preview_view)
        glPreviewView.post {
            glPreviewView.setBlank()
            glPreviewView.setLightPoint(0F, 15F, 40F)
            glPreviewView.setViewpoint(0F, 15F, 40F)
            glPreviewView.setProjection(ProjectionType.Perspective, 10F, 1000F, 1F)
            filterId = glPreviewView.addFilter(FogFilter.TAG)
            glPreviewView.requestRender()
        }
    }
}