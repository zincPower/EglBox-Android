package com.jiangpengyong.sample.font

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox.R
import com.jiangpengyong.sample.EglBoxSample
import java.io.File

class FontActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_font)

        findViewById<TextView>(R.id.obtain_font_info).setOnClickListener {
            EglBoxSample.getFreeType(File(filesDir, "fonts/WenQuanZhengHei-1.ttf").absolutePath)
        }
    }
}