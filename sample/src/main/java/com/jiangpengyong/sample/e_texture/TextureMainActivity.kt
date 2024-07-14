package com.jiangpengyong.sample.e_texture

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox.R


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

        findViewById<TextView>(R.id.texture2d).setOnClickListener {
            startActivity(Intent(this, Texture2DActivity::class.java))
        }
    }

}