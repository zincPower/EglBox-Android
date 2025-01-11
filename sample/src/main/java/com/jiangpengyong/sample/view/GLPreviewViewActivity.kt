package com.jiangpengyong.sample.view

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jiangpengyong.eglbox_core.view.FilterCenter
import com.jiangpengyong.eglbox_core.view.GLPreviewView
import com.jiangpengyong.eglbox_filter.TriangleFilter
import com.jiangpengyong.eglbox_filter.filter.BallFilter
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * @author jiang peng yong
 * @date 2024/11/30 10:01
 * @email 56002982@qq.com
 * @des GLPreviewView 例子
 */
class GLPreviewViewActivity : AppCompatActivity() {
    private lateinit var previewView: GLPreviewView
    private var triangleFilter: String? = null
    private var ballFilter: String? = null
    private var starFilter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gl_preivew_view)
        previewView = findViewById(R.id.preview_view)

        FilterCenter.registerFilter("TriangleFilter", TriangleFilter::class.java)
        FilterCenter.registerFilter("BallFilter", BallFilter::class.java)
        FilterCenter.registerFilter("StarFilter", StarFilter::class.java)

        findViewById<View>(R.id.image_horizontal).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                BitmapFactory.decodeFile(File(App.context.filesDir, "images/test_image/test_image_horizontal.jpg").absolutePath).let { bitmap ->
                    previewView.setImage(bitmap, true)
                }
            }
        }

        findViewById<View>(R.id.image_vertical).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                BitmapFactory.decodeFile(File(App.context.filesDir, "images/test_image/test_image_vertical.jpg").absolutePath).let { bitmap ->
                    previewView.setImage(bitmap, true)
                }
            }
        }

        findViewById<View>(R.id.blank).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                previewView.setBlank()
            }
        }

        findViewById<View>(R.id.add_filter_triangle).setOnClickListener {
            if (triangleFilter != null) return@setOnClickListener
            triangleFilter = previewView.addFilter("TriangleFilter")
        }

        findViewById<View>(R.id.add_filter_ball).setOnClickListener {
            if (ballFilter != null) return@setOnClickListener
            ballFilter = previewView.addFilter("BallFilter")
        }

        findViewById<View>(R.id.add_filter_star).setOnClickListener {
            if (starFilter != null) return@setOnClickListener
            starFilter = previewView.addFilter("StarFilter", order = 1)
        }

        findViewById<View>(R.id.remove_filter_triangle).setOnClickListener {
            previewView.removeFilter(triangleFilter ?: return@setOnClickListener)
            triangleFilter = null
        }

        findViewById<View>(R.id.remove_filter_ball).setOnClickListener {
            previewView.removeFilter(ballFilter ?: return@setOnClickListener)
            ballFilter = null
        }

        findViewById<View>(R.id.remove_filter_star).setOnClickListener {
            previewView.removeFilter(starFilter ?: return@setOnClickListener)
            starFilter = null
        }

        findViewById<View>(R.id.export).setOnClickListener {
            previewView.exportImage(
                bitmap = BitmapFactory.decodeFile(File(App.context.filesDir, "images/test_image/test_image_horizontal.jpg").absolutePath),
                data = hashMapOf(),
            ) { bitmap ->
                runOnUiThread {
                    findViewById<ImageView>(R.id.result_image)
                        .setImageBitmap(bitmap)
                }
            }
        }
    }
}