package com.jiangpengyong.sample

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox.R
import com.jiangpengyong.sample.page.font.FontActivity
import com.jiangpengyong.sample.page.obj.FrustumActivity
import com.jiangpengyong.sample.page.texture.TextureMainActivity
import com.jiangpengyong.sample.page.triangle.SimpleTriangleActivity

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
        findViewById<TextView>(R.id.simpleTriangle).setOnClickListener {
            startActivity(Intent(this, SimpleTriangleActivity::class.java))
        }

        findViewById<TextView>(R.id.texture).setOnClickListener {
            startActivity(Intent(this, TextureMainActivity::class.java))
        }

        findViewById<TextView>(R.id.font).setOnClickListener {
            startActivity(Intent(this, FontActivity::class.java))
        }

        findViewById<TextView>(R.id.frustum).setOnClickListener {
            startActivity(Intent(this, FrustumActivity::class.java))
        }
    }
}