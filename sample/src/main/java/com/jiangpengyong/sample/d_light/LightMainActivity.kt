package com.jiangpengyong.sample.d_light

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_sample.R

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

        findViewById<View>(R.id.scattered_light).setOnClickListener {
            startActivity(Intent(this, ScatteredLightActivity::class.java))
        }

        findViewById<View>(R.id.specular_reflection).setOnClickListener {
            startActivity(Intent(this, AmbientLightActivity::class.java))
        }
    }
}