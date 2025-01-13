package com.jiangpengyong.sample.i_scene.watercolor

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jiangpengyong.eglbox_core.space3d.ProjectionType
import com.jiangpengyong.eglbox_core.view.FilterCenter
import com.jiangpengyong.eglbox_core.view.GLPreviewView
import com.jiangpengyong.eglbox_filter.utils.Obj3DModelLoader
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * @author jiang peng yong
 * @date 2025/1/13 08:24
 * @email 56002982@qq.com
 * @des 水彩上色
 */
class WatercolorActivity : AppCompatActivity() {
    private lateinit var glPreviewView: GLPreviewView
    private var filterId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene)

        FilterCenter.registerFilter(WatercolorFilter.TAG, WatercolorFilter::class.java)

        glPreviewView = findViewById(R.id.gl_preview_view)
        glPreviewView.post {
            glPreviewView.setBlank()
            glPreviewView.setLightPoint(0F, 15F, 40F)
            glPreviewView.setViewpoint(0F, 15F, 40F)
            glPreviewView.setProjection(ProjectionType.Perspective, 10F, 1000F, 2F)

            lifecycleScope.launch(Dispatchers.IO) {
                val filterId = glPreviewView.addFilter(WatercolorFilter.TAG)?.also {
                    filterId = it
                } ?: return@launch
                BitmapFactory.decodeFile(File(App.context.filesDir, "images/texture_image/blue_color_chart.jpg").absolutePath).let { bitmap ->
                    val message = Message.obtain()
                    message.what = WatercolorFilter.MessageType.SET_COLOR_CHART.value
                    message.obj = bitmap
                    glPreviewView.sendMessageToFilter(filterId, message)
                }
                Obj3DModelLoader.load(File(App.context.filesDir, "model/film/film.obj")).let { modelData ->
                    val message = Message.obtain()
                    message.what = WatercolorFilter.MessageType.SET_MODEL_DATA.value
                    message.obj = modelData
                    glPreviewView.sendMessageToFilter(filterId, message)
                }

                glPreviewView.requestRender()
            }
        }
    }
}