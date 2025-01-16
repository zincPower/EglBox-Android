package com.jiangpengyong.sample.i_scene.outline

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.space3d.ProjectionType
import com.jiangpengyong.eglbox_core.view.FilterCenter
import com.jiangpengyong.eglbox_core.view.GLPreviewView
import com.jiangpengyong.eglbox_sample.R

/**
 * @author jiang peng yong
 * @date 2025/1/15 07:56
 * @email 56002982@qq.com
 * @des 描边
 */
class OutlineActivity : AppCompatActivity() {
    private lateinit var glPreviewView: GLPreviewView
    private var filterId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outline)

        FilterCenter.registerFilter(OutlineGroupFilter.TAG, OutlineGroupFilter::class.java)

        glPreviewView = findViewById(R.id.gl_preview_view)
        glPreviewView.post {
            glPreviewView.setBlank()
            glPreviewView.setLightPoint(0F, 15F, 40F)
            glPreviewView.setViewpoint(0F, 15F, 40F)
            glPreviewView.setProjection(ProjectionType.Perspective, 10F, 1000F, 1F)
            filterId = glPreviewView.addFilter(OutlineGroupFilter.TAG)
            glPreviewView.requestRender()
        }
    }
}