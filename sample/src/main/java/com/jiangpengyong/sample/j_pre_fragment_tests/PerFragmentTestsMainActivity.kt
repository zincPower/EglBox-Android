package com.jiangpengyong.sample.j_pre_fragment_tests

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jiangpengyong.eglbox_sample.R
import com.jiangpengyong.sample.j_pre_fragment_tests.alpha_test.AlphaTestActivity
import com.jiangpengyong.sample.j_pre_fragment_tests.clipping_plane.ClippingPlaneActivity
import com.jiangpengyong.sample.j_pre_fragment_tests.scissor_test.ScissorTestActivity
import com.jiangpengyong.sample.j_pre_fragment_tests.stencil_test.StencilTestActivity

/**
 * @author jiang peng yong
 * @date 2025/1/20 23:45
 * @email 56002982@qq.com
 * @des 逐片段测试
 */
class PerFragmentTestsMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_per_fragment_tests_main)

        findViewById<TextView>(R.id.scissor_test).setOnClickListener {
            startActivity(Intent(this, ScissorTestActivity::class.java))
        }

        findViewById<TextView>(R.id.alpha_test).setOnClickListener {
            startActivity(Intent(this, AlphaTestActivity::class.java))
        }

        findViewById<TextView>(R.id.stencil_test).setOnClickListener {
            startActivity(Intent(this, StencilTestActivity::class.java))
        }

        findViewById<TextView>(R.id.clipping_plane).setOnClickListener {
            startActivity(Intent(this, ClippingPlaneActivity::class.java))
        }
    }
}