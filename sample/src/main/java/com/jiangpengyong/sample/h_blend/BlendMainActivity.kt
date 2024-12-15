package com.jiangpengyong.sample.h_blend

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.h_blend.alpha_sniper_scope.AlphaSniperScopeActivity
import com.jiangpengyong.sample.h_blend.sniper_scope.SniperScopeActivity

/**
 * @author jiang peng yong
 * @date 2024/11/15 18:14
 * @email 56002982@qq.com
 * @des 混合模式首页
 */
class BlendMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blend_main)

        findViewById<TextView>(R.id.blend_no_alpha).setOnClickListener {
            startActivity(Intent(this, SniperScopeActivity::class.java))
        }

        findViewById<TextView>(R.id.blend_alpha).setOnClickListener {
            startActivity(Intent(this, AlphaSniperScopeActivity::class.java))
        }
    }
}