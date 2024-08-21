package com.jiangpengyong.sample.e_texture

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.e_texture.planet.SolarSystemActivity

/**
 * @author: jiang peng yong
 * @date: 2023/8/31 19:16
 * @email: 56002982@qq.com
 * @desc: 纹理页面
 */
class TextureMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_texture_main)

        findViewById<TextView>(R.id.triangle_texture).setOnClickListener {
            startActivity(Intent(this, TriangleTextureActivity::class.java))
        }

        findViewById<TextView>(R.id.texture_swizzle).setOnClickListener {
            startActivity(Intent(this, TextureSwizzleActivity::class.java))
        }

        findViewById<TextView>(R.id.wrap_mode).setOnClickListener {
            startActivity(Intent(this, TextureWrapActivity::class.java))
        }

        findViewById<TextView>(R.id.sample_mode).setOnClickListener {
            startActivity(Intent(this, TextureSampleActivity::class.java))
        }

        findViewById<TextView>(R.id.texture2d).setOnClickListener {
            startActivity(Intent(this, Texture2DActivity::class.java))
        }

        findViewById<TextView>(R.id.solar_system).setOnClickListener {
            startActivity(Intent(this, SolarSystemActivity::class.java))
        }

        findViewById<TextView>(R.id.etc1_texture).setOnClickListener {
            startActivity(Intent(this, ETC1TextureActivity::class.java))
        }
    }
}