package com.jiangpengyong.sample.c_drawing_mode

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_sample.R

/**
 * @author jiang peng yong
 * @date 2024/7/14 12:11
 * @email 56002982@qq.com
 * @des 绘制模式主入口
 */
class DrawingModeMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing_mode_main)

        findViewById<View>(R.id.drawing_mode_share)
            .setOnClickListener{
                startActivity(Intent(this, DrawingModeActivity::class.java))
            }

        findViewById<View>(R.id.draw_elements)
            .setOnClickListener{
                startActivity(Intent(this, DrawElementsModeActivity::class.java))
            }

        findViewById<View>(R.id.draw_range_elements)
            .setOnClickListener{
                startActivity(Intent(this, DrawRangeElementsModeActivity::class.java))
            }

        findViewById<View>(R.id.draw_range_elements)
            .setOnClickListener{
                startActivity(Intent(this, DrawRangeElementsModeActivity::class.java))
            }

        findViewById<View>(R.id.layout)
            .setOnClickListener{
                startActivity(Intent(this, LayoutGLSLActivity::class.java))
            }

        findViewById<View>(R.id.vertex_attrib)
            .setOnClickListener{
                startActivity(Intent(this, VertexAttribActivity::class.java))
            }
    }
}