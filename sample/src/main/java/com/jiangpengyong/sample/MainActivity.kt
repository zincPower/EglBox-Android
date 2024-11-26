package com.jiangpengyong.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.a_glsl.GLSLMainActivity
import com.jiangpengyong.sample.b_matrix.MatrixMainActivity
import com.jiangpengyong.sample.c_drawing_mode.DrawingModeMainActivity
import com.jiangpengyong.sample.d_light.LightMainActivity
import com.jiangpengyong.sample.e_texture.TextureMainActivity
import com.jiangpengyong.sample.f_geometry.GeometryMainActivity
import com.jiangpengyong.sample.font.FontActivity
import com.jiangpengyong.sample.g_model.Model3DMainActivity
import com.jiangpengyong.sample.h_blend.BlendMainActivity
import com.jiangpengyong.sample.i_scene.Scene3DMainActivity
import com.jiangpengyong.sample.view.GLPreviewViewActivity

/**
 * @author jiang peng yong
 * @date 2024/2/9 15:33
 * @email 56002982@qq.com
 * @des sample 入口
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.glsl).setOnClickListener {
            startActivity(Intent(this, GLSLMainActivity::class.java))
        }

        findViewById<View>(R.id.matrix).setOnClickListener {
            startActivity(Intent(this, MatrixMainActivity::class.java))
        }

        findViewById<View>(R.id.drawing_mode).setOnClickListener {
            startActivity(Intent(this, DrawingModeMainActivity::class.java))
        }

        findViewById<View>(R.id.light).setOnClickListener {
            startActivity(Intent(this, LightMainActivity::class.java))
        }

        findViewById<View>(R.id.texture).setOnClickListener {
            startActivity(Intent(this, TextureMainActivity::class.java))
        }

        findViewById<View>(R.id.geometry).setOnClickListener {
            startActivity(Intent(this, GeometryMainActivity::class.java))
        }

        findViewById<View>(R.id.model).setOnClickListener {
            startActivity(Intent(this, Model3DMainActivity::class.java))
        }

        findViewById<View>(R.id.blend).setOnClickListener {
            startActivity(Intent(this, BlendMainActivity::class.java))
        }

        findViewById<View>(R.id.scene).setOnClickListener {
            startActivity(Intent(this, Scene3DMainActivity::class.java))
        }

        findViewById<View>(R.id.font).setOnClickListener {
            startActivity(Intent(this, FontActivity::class.java))
        }

        findViewById<View>(R.id.preview_view).setOnClickListener {
            startActivity(Intent(this, GLPreviewViewActivity::class.java))
        }
    }
}