package com.jiangpengyong.sample.view

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jiangpengyong.eglbox_core.processor.display.DisplayProcessor
import com.jiangpengyong.eglbox_core.view.FilterCenter
import com.jiangpengyong.eglbox_core.view.GLDisplayView
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.App
import com.jiangpengyong.sample.c_drawing_mode.LayoutGLSLActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class GLDisplayViewActivity : AppCompatActivity() {
    private lateinit var displayView: GLDisplayView
    private var triangleFilter: String? = null
    private var ballFilter: String? = null
    private var starFilter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gl_preivew_view)
        displayView = findViewById(R.id.preview_view)

        FilterCenter.registerFilter("TriangleFilter", TriangleFilter::class.java)
        FilterCenter.registerFilter("BallFilter", BallFilter::class.java)
        FilterCenter.registerFilter("StarFilter", StarFilter::class.java)

        findViewById<View>(R.id.image_horizontal).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                BitmapFactory.decodeFile(File(App.context.filesDir, "images/test_image/test_image_horizontal.png").absolutePath).let { bitmap ->
                    displayView.setImage(bitmap, true)
                }
            }
        }

        findViewById<View>(R.id.image_vertical).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                BitmapFactory.decodeFile(File(App.context.filesDir, "images/test_image/test_image_vertical.png").absolutePath).let { bitmap ->
                    displayView.setImage(bitmap, true)
                }
            }
        }

        findViewById<View>(R.id.blank).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                displayView.setBlank()
            }
        }

        findViewById<View>(R.id.add_filter_triangle).setOnClickListener {
            if (triangleFilter != null) return@setOnClickListener
            triangleFilter = displayView.addFilter(DisplayProcessor.FilterType.Process, "TriangleFilter", 0)
        }

        findViewById<View>(R.id.add_filter_ball).setOnClickListener {
            if (ballFilter != null) return@setOnClickListener
            ballFilter = displayView.addFilter(DisplayProcessor.FilterType.Process, "BallFilter", 0)
        }

        findViewById<View>(R.id.add_filter_star).setOnClickListener {
            if (starFilter != null) return@setOnClickListener
            starFilter = displayView.addFilter(DisplayProcessor.FilterType.Process, "StarFilter", 1)
        }

        findViewById<View>(R.id.remove_filter_triangle).setOnClickListener {
            displayView.removeFilter(triangleFilter ?: return@setOnClickListener)
            triangleFilter = null
        }

        findViewById<View>(R.id.remove_filter_ball).setOnClickListener {
            displayView.removeFilter(ballFilter ?: return@setOnClickListener)
            ballFilter = null
        }

        findViewById<View>(R.id.remove_filter_star).setOnClickListener {
            displayView.removeFilter(starFilter ?: return@setOnClickListener)
            starFilter = null
        }

        findViewById<View>(R.id.export).setOnClickListener {
            displayView.exportImage(
                bitmap = BitmapFactory.decodeFile(File(App.context.filesDir, "images/test_image/test_image_horizontal.png").absolutePath),
                data = hashMapOf(),
            ) { bitmap ->
                runOnUiThread {
                    findViewById<ImageView>(R.id.result_image)
                        .setImageBitmap(bitmap)
                }
            }
        }
    }

    companion object {
        private const val TAG = "GLDisplayViewActivity"
    }
}