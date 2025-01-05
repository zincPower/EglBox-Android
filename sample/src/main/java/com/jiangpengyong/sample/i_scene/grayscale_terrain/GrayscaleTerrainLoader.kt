package com.jiangpengyong.sample.i_scene.grayscale_terrain

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.get
import com.jiangpengyong.eglbox_core.space3d.Vector
import com.jiangpengyong.eglbox_core.utils.Math3D
import com.jiangpengyong.eglbox_filter.model.FrontFace
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.eglbox_filter.model.NormalVectorType

/**
 * @author jiang peng yong
 * @date 2024/12/22 22:29
 * @email 56002982@qq.com
 * @des 灰度地形图加载
 */
object GrayscaleTerrainLoader {
    fun load(
        grayscaleTerrainBitmap: Bitmap, // 灰度地形图
        meanSeaLevel: Float,            // 海平面，即零米基准
        heightDelta: Float,             // 最高和最低的差值
        textureRepeatCount: Int,        // 纹理重复次数
    ): ModelData {
        val altitude = handleAltitude(grayscaleTerrainBitmap, meanSeaLevel, heightDelta)
        val (vertex, normal) = handleVertex(grayscaleTerrainBitmap, altitude)
        val texture = handleTexture(grayscaleTerrainBitmap, textureRepeatCount)
        Log.i("GrayscaleTerrainLoader", "vertex=${vertex.size} normal=${normal.size} texture=${texture.size}")
        return ModelData(
            vertexData = vertex.toFloatArray(),
            textureData = texture.toFloatArray(),
            textureStep = 2,
            normalData = normal.toFloatArray(),
            frontFace = FrontFace.CCW,
            normalVectorType = NormalVectorType.Vertex,
        )
    }

    private fun handleAltitude(
        grayscaleTerrainBitmap: Bitmap,
        meanSeaLevel: Float,
        heightDelta: Float,
    ): Array<Array<Float>> {
        val width = grayscaleTerrainBitmap.width
        val height = grayscaleTerrainBitmap.height
        val altitudeList = Array(width) { Array(height) { 0F } }
        for (w in 0 until width) {
            for (h in 0 until height) {
                val color = grayscaleTerrainBitmap[w, h]
                // 灰度图，只读取一个色值就可以
                val red = Color.red(color)
                altitudeList[w][h] = red * heightDelta / 255 + meanSeaLevel
            }
        }
        return altitudeList
    }

    private fun handleVertex(
        grayscaleTerrainBitmap: Bitmap,
        altitudeList: Array<Array<Float>>,
    ): Pair<ArrayList<Float>, ArrayList<Float>> {
        val width = grayscaleTerrainBitmap.width
        val height = grayscaleTerrainBitmap.height
        val vertexList = ArrayList<Float>()
        val normalList = ArrayList<Float>()
        for (w in 0 until width - 1) {
            for (h in 0 until height - 1) {
                /**
                 *  3-----│-----0
                 *  │     │     │
                 *  │     │     │
                 *  │----原点----│> +x
                 *  │     │     │
                 *  │     │     │
                 *  2-----│-----1
                 *        ᐯ +z
                 */
                val x3 = -width / 2F + w
                val y3 = altitudeList[w][h]
                val z3 = -height / 2F + h

                val x2 = x3
                val y2 = altitudeList[w][h + 1]
                val z2 = z3 + 1

                val x1 = x3 + 1
                val y1 = altitudeList[w + 1][h + 1]
                val z1 = z3 + 1

                val x0 = x3 + 1
                val y0 = altitudeList[w + 1][h]
                val z0 = z3

                vertexList.add(x0)
                vertexList.add(y0)
                vertexList.add(z0)
                vertexList.add(x3)
                vertexList.add(y3)
                vertexList.add(z3)
                vertexList.add(x2)
                vertexList.add(y2)
                vertexList.add(z2)

                vertexList.add(x2)
                vertexList.add(y2)
                vertexList.add(z2)
                vertexList.add(x1)
                vertexList.add(y1)
                vertexList.add(z1)
                vertexList.add(x0)
                vertexList.add(y0)
                vertexList.add(z0)

                val v1 = Math3D.crossProduct(
                    v1 = Vector(x3 - x0, y3 - y0, z3 - z0),
                    v2 = Vector(x2 - x0, y2 - y0, z2 - z0),
                )
                val v2 = Math3D.crossProduct(
                    v1 = Vector(x1 - x2, y1 - y2, z1 - z2),
                    v2 = Vector(x0 - x2, y0 - y2, z0 - z2),
                )
                normalList.add(v1.x)
                normalList.add(v1.y)
                normalList.add(v1.z)
                normalList.add(v1.x)
                normalList.add(v1.y)
                normalList.add(v1.z)
                normalList.add(v1.x)
                normalList.add(v1.y)
                normalList.add(v1.z)

                normalList.add(v2.x)
                normalList.add(v2.y)
                normalList.add(v2.z)
                normalList.add(v2.x)
                normalList.add(v2.y)
                normalList.add(v2.z)
                normalList.add(v2.x)
                normalList.add(v2.y)
                normalList.add(v2.z)
            }
        }
        return Pair(vertexList, normalList)
    }

    private fun handleTexture(
        grayscaleTerrainBitmap: Bitmap,
        textureRepeatCount: Int,
    ): ArrayList<Float> {
        val width = grayscaleTerrainBitmap.width
        val height = grayscaleTerrainBitmap.height
        val textureList = ArrayList<Float>()
        val wStep = textureRepeatCount / width.toFloat()
        val hStep = textureRepeatCount / height.toFloat()
        for (w in 0 until width - 1) {
            for (h in 0 until height - 1) {
                /**
                 *  ᐱ t
                 *  │
                 *  3-----------0
                 *  │           │
                 *  │           │
                 *  │           │
                 *  │           │
                 *  │           │
                 *  2-----------1--> s
                 * 原点
                 */

                val s2 = wStep * w
                val t2 = hStep * h

                val s3 = s2
                val t3 = t2 + hStep

                val s1 = s2 + wStep
                val t1 = t2

                val s0 = s2 + wStep
                val t0 = t2 + hStep

                textureList.add(s0)
                textureList.add(t0)
                textureList.add(s3)
                textureList.add(t3)
                textureList.add(s2)
                textureList.add(t2)

                textureList.add(s2)
                textureList.add(t2)
                textureList.add(s1)
                textureList.add(t1)
                textureList.add(s0)
                textureList.add(t0)
            }
        }
        return textureList
    }
}