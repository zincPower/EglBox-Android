package com.jiangpengyong.sample.j_pre_fragment_tests.clipping_plane

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_core.space3d.ProjectionType
import com.jiangpengyong.eglbox_core.view.FilterCenter
import com.jiangpengyong.eglbox_core.view.GLPreviewView
import com.jiangpengyong.eglbox_sample.R

/**
 * @author jiang peng yong
 * @date 2025/1/25 13:27
 * @email 56002982@qq.com
 * @des 平面裁剪
 */
class ClippingPlaneActivity : AppCompatActivity() {
    private lateinit var glPreviewView: GLPreviewView
    private var filterId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_glpreviewview)

        FilterCenter.registerFilter(ClippingPlaneFilter.TAG, ClippingPlaneFilter::class.java)

        glPreviewView = findViewById(R.id.gl_preview_view)
        glPreviewView.post {
            glPreviewView.setBlank()
            glPreviewView.setLightPoint(0F, 15F, 40F)
            glPreviewView.setViewpoint(0F, 0F, 20F)
            glPreviewView.setProjection(ProjectionType.Perspective, 10F, 200F, 2F)
            filterId = glPreviewView.addFilter(ClippingPlaneFilter.TAG)
            glPreviewView.requestRender()
        }
    }
}