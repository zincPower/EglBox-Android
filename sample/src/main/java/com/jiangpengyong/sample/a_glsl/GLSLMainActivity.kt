package com.jiangpengyong.sample.a_glsl

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox.R

/**
 * @author jiang peng yong
 * @date 2024/6/16 14:33
 * @email 56002982@qq.com
 * @des GLSL 主入口
 */
class GLSLMainActivity :AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glsl_main)

        findViewById<View>(R.id.star).setOnClickListener{
            startActivity(Intent(this, StarActivity::class.java))
        }

        findViewById<View>(R.id.triangle).setOnClickListener{
            startActivity(Intent(this, TriangleActivity::class.java))
        }
    }

}