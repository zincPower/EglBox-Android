package com.jiangpengyong.sample.g_model

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Message
import android.widget.RadioGroup
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @author jiang peng yong
 * @date 2024/11/6 21:35
 * @email 56002982@qq.com
 * @des 模型
 */
class Model3DMainActivity : AppCompatActivity() {
    private lateinit var glPreviewView: GLPreviewView
    private var filterId: String? = null
    private var modelInfos: Map<Int, ModelInfo> = hashMapOf(
        R.id.film to ModelInfo("model/film/film.obj", "model/film/film.jpg"),
        R.id.teapot to ModelInfo("model/teapot/teapot.obj", "model/teapot/teapot.png")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_3d_main)

        FilterCenter.registerFilter(Model3DFilter.TAG, Model3DFilter::class.java)

        glPreviewView = findViewById(R.id.gl_preview_view)
        glPreviewView.post {
            glPreviewView.setBlank()
            glPreviewView.setViewpoint(0F, 0F, 100F)
            filterId = glPreviewView.addFilter(PreviewProcessor.FilterType.Process, Model3DFilter.TAG, 0)

            filterId?.let { loadModel(it, modelInfos[R.id.film] ?: return@let) }
        }

        findViewById<TextView>(R.id.reset_rotation).setOnClickListener {
            glPreviewView.resetRotation()
        }

        findViewById<RadioGroup>(R.id.model_type).setOnCheckedChangeListener { group, checkedId ->
            loadModel(filterId ?: return@setOnCheckedChangeListener, modelInfos[checkedId] ?: return@setOnCheckedChangeListener)
        }
    }

    private fun loadModel(filterId: String, modelInfo: ModelInfo) {
        lifecycleScope.launch(Dispatchers.IO) {
            val file = File(filesDir, modelInfo.modelPath)
            val model3DInfo = Obj3DModelLoader.load(file, textureFlip = true)
            if (model3DInfo == null) {
                Logger.e(TAG, "Obj parser failure. File=${file}")
                return@launch
            }
            Logger.i(TAG, "Model 3D info. Space=${model3DInfo.space} File=${file}")
            withContext(Dispatchers.Main) {
                glPreviewView.sendMessageToFilter(filterId, Message.obtain().apply {
                    what = Model3DMessageType.SET_MODEL_DATA.value
                    obj = model3DInfo
                })
                glPreviewView.requestRender()
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = (File(filesDir, modelInfo.texturePath).absolutePath.let {
                BitmapFactory.decodeFile(it)
            })
            withContext(Dispatchers.Main) {
                glPreviewView.sendMessageToFilter(filterId, Message.obtain().apply {
                    what = Model3DMessageType.SET_MODEL_TEXTURE_IMAGE.value
                    obj = bitmap
                })
                glPreviewView.requestRender()
            }
        }
    }

    companion object {
        private const val TAG = "TeapotActivity"
    }
}

data class ModelInfo(val modelPath: String, val texturePath: String?)