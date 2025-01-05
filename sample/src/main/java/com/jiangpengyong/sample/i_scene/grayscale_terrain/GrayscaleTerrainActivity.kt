package com.jiangpengyong.sample.i_scene.grayscale_terrain

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Message
import android.util.Range
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jiangpengyong.eglbox_core.space3d.ProjectionType
import com.jiangpengyong.eglbox_core.view.FilterCenter
import com.jiangpengyong.eglbox_core.view.GLPreviewView
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * @author jiang peng yong
 * @date 2024/12/21 19:31
 * @email 56002982@qq.com
 * @des 灰度地形图
 */
class GrayscaleTerrainActivity : AppCompatActivity() {
    private lateinit var glPreviewView: GLPreviewView
    private var filterId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene)

        FilterCenter.registerFilter(GrayscaleTerrainFilter.TAG, GrayscaleTerrainFilter::class.java)

        glPreviewView = findViewById(R.id.gl_preview_view)
        glPreviewView.post {
            glPreviewView.setBlank()
            glPreviewView.setViewpoint(0F, 100F, 500F)
            glPreviewView.setProjection(ProjectionType.Perspective, 10F, 1000F, 1F)

            lifecycleScope.launch(Dispatchers.IO) {
                val filterId = glPreviewView.addFilter(GrayscaleTerrainFilter.TAG)?.also {
                    filterId = it
                } ?: return@launch
                BitmapFactory.decodeFile(File(App.context.filesDir, "images/texture_image/grass.jpg").absolutePath).let { bitmap ->
                    val message = Message.obtain()
                    message.what = GrayscaleTerrainFilter.Type.UPDATE_GRASS_TEXTURE.value
                    message.obj = bitmap
                    glPreviewView.sendMessageToFilter(filterId, message)
                }
                BitmapFactory.decodeFile(File(App.context.filesDir, "images/texture_image/rock.jpeg").absolutePath).let { bitmap ->
                    val message = Message.obtain()
                    message.what = GrayscaleTerrainFilter.Type.UPDATE_ROCK_TEXTURE.value
                    message.obj = bitmap
                    glPreviewView.sendMessageToFilter(filterId, message)
                }
                BitmapFactory.decodeFile(File(App.context.filesDir, "images/texture_image/grayscale_terrain.png").absolutePath).let { bitmap ->
                    val modelData = GrayscaleTerrainLoader.load(
                        grayscaleTerrainBitmap = bitmap,
                        meanSeaLevel = -10F,
                        heightDelta = 50F,
                        textureRepeatCount = 10,
                    )
                    val grayscaleTerrainData = GrayscaleTerrainFilter.GrayscaleTerrainData(
                        modelData = modelData,
                        boundaryRange = Range(10F, 20F)
                    )
                    val message = Message.obtain()
                    message.what = GrayscaleTerrainFilter.Type.UPDATE_DATA.value
                    message.obj = grayscaleTerrainData
                    glPreviewView.sendMessageToFilter(filterId, message)
                }

                glPreviewView.requestRender()
            }
        }
    }
}