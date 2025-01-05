package com.jiangpengyong.sample.i_scene

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.i_scene.fog.FogActivity
import com.jiangpengyong.sample.i_scene.grayscale_terrain.GrayscaleTerrainActivity

/**
 * @author jiang peng yong
 * @date 2024/11/15 19:53
 * @email 56002982@qq.com
 * @des 3D 场景首页
 */
class Scene3DMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene_main)

        findViewById<View>(R.id.fog).setOnClickListener {
            startActivity(Intent(this, FogActivity::class.java))
        }

        findViewById<View>(R.id.grayscale_terrain).setOnClickListener {
            startActivity(Intent(this, GrayscaleTerrainActivity::class.java))
        }
    }
}