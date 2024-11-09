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
import com.jiangpengyong.eglbox_core.space3d.Point
import com.jiangpengyong.eglbox_core.view.FilterCenter
import com.jiangpengyong.eglbox_core.view.GLPreviewView
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.g_model.all.Model3DMessageType
import com.jiangpengyong.sample.g_model.onlyVertex.Model3DFilter
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
        R.id.film to ModelInfo("model/film/film.obj", "model/film/film.jpg", Point(0F, 0F, 100F)),
        R.id.teapot_only_vertex to ModelInfo("model/teapot/only_vertex/teapot.obj", null, Point(0F, 0F, 30F)),
        R.id.teapot_without_lid to ModelInfo("model/teapot/without_lid/teapot.obj", null, Point(0F, 0F, 30F)),
        R.id.teapot_all to ModelInfo("model/teapot/all/teapot.obj", "model/teapot/all/teapot.png", Point(0F, 0F, 100F))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_3d_main)

        FilterCenter.registerFilter(Model3DFilter.TAG, Model3DFilter::class.java)

        glPreviewView = findViewById(R.id.gl_preview_view)
        glPreviewView.post {
            glPreviewView.setBlank()
            filterId = glPreviewView.addFilter(PreviewProcessor.FilterType.Process, Model3DFilter.TAG, 0)

            filterId?.let { filterId ->
                modelInfos[R.id.film]?.let { modelInfo ->
                    loadModel(filterId, modelInfo)
                }
            }
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
            glPreviewView.setViewpoint(modelInfo.viewpoint.x, modelInfo.viewpoint.y, modelInfo.viewpoint.z)
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

        val texturePath = modelInfo.texturePath ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = (File(filesDir, texturePath).absolutePath.let {
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

data class ModelInfo(val modelPath: String, val texturePath: String?, val viewpoint: Point)