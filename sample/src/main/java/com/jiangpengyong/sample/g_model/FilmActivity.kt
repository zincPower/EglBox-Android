package com.jiangpengyong.sample.g_model

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Message
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.processor.preview.PreviewProcessor
import com.jiangpengyong.eglbox_core.view.FilterCenter
import com.jiangpengyong.eglbox_core.view.GLPreviewView
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.g_model.common.Model3DFilter
import com.jiangpengyong.sample.g_model.common.Model3DMessageType
import com.jiangpengyong.sample.g_model.common.Obj3DModelLoader
import com.jiangpengyong.sample.view.TriangleFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @author jiang peng yong
 * @date 2024/10/24 08:16
 * @email 56002982@qq.com
 * @des 胶卷
 */
class FilmActivity : AppCompatActivity() {
    private lateinit var glPreviewView: GLPreviewView
    private var filterId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FilterCenter.registerFilter(Model3DFilter.TAG, Model3DFilter::class.java)
        FilterCenter.registerFilter(TriangleFilter.TAG, TriangleFilter::class.java)

        setContentView(R.layout.activity_model_3d)
        glPreviewView = findViewById(R.id.gl_preview_view)
        glPreviewView.post {
            glPreviewView.setBlank()

            filterId = glPreviewView.addFilter(PreviewProcessor.FilterType.Process, Model3DFilter.TAG, 0)
            filterId?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    val file = File(filesDir, "model/film/film.obj")
                    val model3DInfo = Obj3DModelLoader.load(file, textureFlip = true)
                    if (model3DInfo == null) {
                        Logger.e(TAG, "Obj parser failure. File=${file}")
                        return@launch
                    }
                    Logger.i(TAG, "Model 3D info. Space=${model3DInfo.space} File=${file}")
                    withContext(Dispatchers.Main) {
                        glPreviewView.sendMessageToFilter(it, Message.obtain().apply {
                            what = Model3DMessageType.SET_MODEL_DATA.value
                            obj = model3DInfo
                        })
                        glPreviewView.requestRender()
                    }
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    val bitmap = (File(filesDir, "model/film/film.jpg").absolutePath.let {
                        BitmapFactory.decodeFile(it)
                    })
                    withContext(Dispatchers.Main) {
                        glPreviewView.sendMessageToFilter(it, Message.obtain().apply {
                            what = Model3DMessageType.SET_MODEL_TEXTURE_IMAGE.value
                            obj = bitmap
                        })
                        glPreviewView.requestRender()
                    }
                }
            }
        }

        findViewById<TextView>(R.id.render).setOnClickListener {
            glPreviewView.requestRender()
        }
    }

    companion object {
        private const val TAG = "FilmActivity"
    }
}