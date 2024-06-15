package com.jiangpengyong.sample.b_projection

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox.R
import com.jiangpengyong.sample.d_texture.Texture2DActivity

class ProjectionMainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projection_main)

        findViewById<View>(R.id.projection_mode).setOnClickListener {
            startActivity(Intent(this, ProjectionActivity::class.java))
        }
    }

}