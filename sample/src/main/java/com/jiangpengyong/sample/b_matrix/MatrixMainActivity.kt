package com.jiangpengyong.sample.b_matrix

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox.R

class MatrixMainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matrix_main)

        findViewById<View>(R.id.projection_mode).setOnClickListener {
            startActivity(Intent(this, ProjectionActivity::class.java))
        }

        findViewById<View>(R.id.view_mode).setOnClickListener {
            startActivity(Intent(this, ViewMatrixActivity::class.java))
        }

        findViewById<View>(R.id.model_mode).setOnClickListener {
            startActivity(Intent(this, ModelMatrixActivity::class.java))
        }
    }

}