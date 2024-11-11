package com.jiangpengyong.sample.d_light

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.d_light.full_light.FullLightActivity

/**
 * @author jiang peng yong
 * @date 2024/7/14 12:11
 * @email 56002982@qq.com
 * @des 光效主入口
 */
class LightMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_light_main)

        findViewById<View>(R.id.ball).setOnClickListener {
            startActivity(Intent(this, BallActivity::class.java))
        }

        findViewById<View>(R.id.ambient_light).setOnClickListener {
            startActivity(Intent(this, AmbientLightActivity::class.java))
        }

        findViewById<View>(R.id.diffuse_light).setOnClickListener {
            startActivity(Intent(this, DiffuseLightActivity::class.java))
        }

        findViewById<View>(R.id.specular_light).setOnClickListener {
            startActivity(Intent(this, SpecularLightActivity::class.java))
        }

        findViewById<View>(R.id.full_light).setOnClickListener {
            startActivity(Intent(this, FullLightActivity::class.java))
        }

        findViewById<View>(R.id.light_source_type).setOnClickListener {
            startActivity(Intent(this, LightSourceTypeActivity::class.java))
        }

        findViewById<View>(R.id.normal_type).setOnClickListener {
            startActivity(Intent(this, NormalTypeActivity::class.java))
        }

        findViewById<View>(R.id.light_calculate_type).setOnClickListener {
            startActivity(Intent(this, LightCalculateTypeActivity::class.java))
        }
    }
}