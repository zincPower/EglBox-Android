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
import com.jiangpengyong.eglbox_filter.TriangleFilter
import com.jiangpengyong.eglbox_sample.R
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
        R.id.film to ModelInfo(
            modelPath = "model/film/film.obj",
            texturePath = "model/film/film.jpg",
            sideRenderingType = SideRenderingType.Single,
            viewpoint = Point(0F, 0F, 100F),
            lightPoint = Point(0F, 0F, 100F),
        ),
        R.id.teapot_only_vertex to ModelInfo(
            modelPath = "model/teapot/only_vertex/teapot.obj",
            texturePath = null,
            sideRenderingType = SideRenderingType.Single,
            viewpoint = Point(0F, 0F, 30F),
            lightPoint = Point(-30F, 30F, 30F),
        ),
        R.id.teapot_without_lid to ModelInfo(
            modelPath = "model/teapot/without_lid/teapot.obj",
            texturePath = null,
            sideRenderingType = SideRenderingType.Double,
            viewpoint = Point(0F, 0F, 30F),
            lightPoint = Point(30F, 30F, 30F),
        ),
        R.id.teapot_all to ModelInfo(
            modelPath = "model/teapot/all/teapot.obj",
            texturePath = "model/teapot/all/teapot.png",
            sideRenderingType = SideRenderingType.Single,
            viewpoint = Point(0F, 0F, 100F),
            lightPoint = Point(0F, 0F, 100F),
        ),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_3d_main)

        FilterCenter.registerFilter(Model3DFilter.TAG, Model3DFilter::class.java)
        FilterCenter.registerFilter(TriangleFilter.TAG, TriangleFilter::class.java)

        glPreviewView = findViewById(R.id.gl_preview_view)
        glPreviewView.post {
            glPreviewView.setBlank()
            filterId = glPreviewView.addFilter(PreviewProcessor.FilterType.Process, Model3DFilter.TAG, 0)
            loadModel()
        }

        findViewById<TextView>(R.id.reset_rotation).setOnClickListener {
            glPreviewView.resetRotation()
        }

        findViewById<RadioGroup>(R.id.model_type).setOnCheckedChangeListener { group, checkedId ->
            loadModel()
        }

        findViewById<RadioGroup>(R.id.vertex_normal_type).setOnCheckedChangeListener { group, checkedId ->
            loadModel()
        }
    }

    private fun loadModel() {
        loadModel(
            filterId = filterId ?: return,
            modelInfo = modelInfos[findViewById<RadioGroup>(R.id.model_type).checkedRadioButtonId] ?: return,
            normalVectorType = if (findViewById<RadioGroup>(R.id.vertex_normal_type).checkedRadioButtonId == R.id.vertex) NormalVectorType.Vertex else NormalVectorType.Surface
        )
    }

    private fun loadModel(filterId: String, modelInfo: ModelInfo, normalVectorType: NormalVectorType) {
        glPreviewView.sendMessageToFilter(filterId, Message.obtain().apply {
            what = Model3DMessageType.RESET_ALL_DATA.value
        })
        glPreviewView.requestRender()
        
        glPreviewView.setViewpoint(modelInfo.viewpoint.x, modelInfo.viewpoint.y, modelInfo.viewpoint.z)
        glPreviewView.setLightPoint(modelInfo.lightPoint.x, modelInfo.lightPoint.y, modelInfo.lightPoint.z)

        lifecycleScope.launch(Dispatchers.IO) {
            val file = File(filesDir, modelInfo.modelPath)
            val model3DInfo = Obj3DModelLoader.load(
                file = file,
                textureFlip = true,
                sideRenderingType = modelInfo.sideRenderingType,
                normalVectorType = normalVectorType,
            )
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

data class ModelInfo(
    val modelPath: String,
    val texturePath: String?,
    val sideRenderingType: SideRenderingType,
    val viewpoint: Point,
    val lightPoint: Point,
)