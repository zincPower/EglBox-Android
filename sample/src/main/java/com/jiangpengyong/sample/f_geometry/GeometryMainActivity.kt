package com.jiangpengyong.sample.f_geometry

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_sample.R

class GeometryMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geometry_main)

        findViewById<View>(R.id.cylinder).setOnClickListener {
            startActivity(Intent(this, CylinderActivity::class.java))
        }

        findViewById<View>(R.id.cone).setOnClickListener {
            startActivity(Intent(this, ConeActivity::class.java))
        }

        findViewById<View>(R.id.torus).setOnClickListener {
            startActivity(Intent(this, TorusActivity::class.java))
        }

        findViewById<View>(R.id.spring).setOnClickListener {
            startActivity(Intent(this, SpringActivity::class.java))
        }
    }

}
