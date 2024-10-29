package com.jiangpengyong.sample.g_model

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.g_model.film.FilmActivity
import com.jiangpengyong.sample.g_model.teapot.TeapotActivity

class ModelMainActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_main)

        findViewById<TextView>(R.id.teapot).setOnClickListener{
            startActivity(Intent(this, TeapotActivity::class.java))
        }

        findViewById<TextView>(R.id.teapot_without_a_lid).setOnClickListener{

        }

        findViewById<TextView>(R.id.film).setOnClickListener{
            startActivity(Intent(this, FilmActivity::class.java))
        }
    }
}