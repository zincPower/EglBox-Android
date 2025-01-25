package com.jiangpengyong.sample.j_pre_fragment_tests.stencil_test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.space3d.ProjectionType
import com.jiangpengyong.eglbox_core.view.FilterCenter
import com.jiangpengyong.eglbox_core.view.GLPreviewView
import com.jiangpengyong.eglbox_sample.R

/**
 * @author jiang peng yong
 * @date 2025/1/20 23:34
 * @email 56002982@qq.com
 * @des 模板测试
 */
class StencilTestActivity : AppCompatActivity() {
    private lateinit var glPreviewView: GLPreviewView
    private var filterId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_glpreviewview)

        FilterCenter.registerFilter(StencilTestFilter.TAG, StencilTestFilter::class.java)

        glPreviewView = findViewById(R.id.gl_preview_view)
        glPreviewView.post {
            glPreviewView.setBlank()
            glPreviewView.setLightPoint(0F, 15F, 40F)
            glPreviewView.setViewpoint(0F, 15F, 40F)
            glPreviewView.setProjection(ProjectionType.Perspective, 10F, 1000F, 1F)
            filterId = glPreviewView.addFilter(StencilTestFilter.TAG)
            glPreviewView.requestRender()
        }
    }
}