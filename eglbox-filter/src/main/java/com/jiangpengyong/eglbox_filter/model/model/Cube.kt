package com.jiangpengyong.eglbox_filter.model.model

import com.jiangpengyong.eglbox_filter.model.FrontFace
import com.jiangpengyong.eglbox_filter.model.Model
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.eglbox_filter.model.NormalVectorType

class Cube : Model() {
    private val sideLength = 1F
    private val halfSideLength = sideLength / 2F

    override fun onCreate(): ModelData {
        return ModelData(
            vertexData = getVertexList(),
            textureData = getTextureList(),
            textureStep = 2,
            normalData = getNormalList(),
            frontFace = FrontFace.CCW,
            normalVectorType = NormalVectorType.Vertex,
        )
    }

    private fun getVertexList() = floatArrayOf(
        /**
         * 正面 向量向屏幕外
         *
         *        ᐱ +y
         *  1-----│-----0
         *  │     │     │
         *  │     │     │
         *  │----原点----│>  +x
         *  │     │     │
         *  │     │     │
         *  2-----│-----3
         *
         */
        halfSideLength, halfSideLength, halfSideLength,     // 0
        -halfSideLength, halfSideLength, halfSideLength,    // 1
        -halfSideLength, -halfSideLength, halfSideLength,   // 2
        halfSideLength, halfSideLength, halfSideLength,     // 0
        -halfSideLength, -halfSideLength, halfSideLength,   // 2
        halfSideLength, -halfSideLength, halfSideLength,    // 3
        /**
         * 后面 向量向屏幕里
         *
         *        ᐱ +y
         *  3-----│-----0
         *  │     │     │
         *  │     │     │
         *  │----原点----│ > +x
         *  │     │     │
         *  │     │     │
         *  2-----------1
         *
         */
        halfSideLength, halfSideLength, -halfSideLength,    // 0
        halfSideLength, -halfSideLength, -halfSideLength,   // 1
        -halfSideLength, -halfSideLength, -halfSideLength,  // 2
        halfSideLength, halfSideLength, -halfSideLength,    // 0
        -halfSideLength, -halfSideLength, -halfSideLength,  // 2
        -halfSideLength, halfSideLength, -halfSideLength,   // 3
        /**
         *  左面 向量向屏幕外
         *
         *        ᐱ +y
         *  1-----│-----0
         *  │     │     │
         *  │     │     │
         *  │----原点----│> +z
         *  │     │     │
         *  │     │     │
         *  2-----│-----3
         *
         */
        -halfSideLength, halfSideLength, halfSideLength,    // 0
        -halfSideLength, halfSideLength, -halfSideLength,   // 1
        -halfSideLength, -halfSideLength, -halfSideLength,  // 2
        -halfSideLength, halfSideLength, halfSideLength,    // 0
        -halfSideLength, -halfSideLength, -halfSideLength,  // 2
        -halfSideLength, -halfSideLength, halfSideLength,   // 3
        /**
         * 右面 向量向屏幕内
         *
         *        ᐱ +y
         *  3-----│-----0
         *  │     │     │
         *  │     │     │
         *  │----原点----│>  +z
         *  │     │     │
         *  │     │     │
         *  2-----│-----1
         *
         */
        halfSideLength, halfSideLength, halfSideLength,     // 0
        halfSideLength, -halfSideLength, halfSideLength,    // 1
        halfSideLength, -halfSideLength, -halfSideLength,   // 2
        halfSideLength, halfSideLength, halfSideLength,     // 0
        halfSideLength, -halfSideLength, -halfSideLength,   // 2
        halfSideLength, halfSideLength, -halfSideLength,    // 3
        /**
         * 上面 向量向屏幕外
         *
         *  3-----│-----0
         *  │     │     │
         *  │     │     │
         *  │----原点----│> +x
         *  │     │     │
         *  │     │     │
         *  2-----│-----1
         *        ᐯ +z
         */
        halfSideLength, halfSideLength, -halfSideLength,    // 0
        -halfSideLength, halfSideLength, halfSideLength,    // 2
        halfSideLength, halfSideLength, halfSideLength,     // 1
        halfSideLength, halfSideLength, -halfSideLength,    // 0
        -halfSideLength, halfSideLength, -halfSideLength,   // 3
        -halfSideLength, halfSideLength, halfSideLength,    // 2
        /**
         *  下面 向量向屏幕内
         *
         *  3-----│-----0
         *  │     │     │
         *  │     │     │
         *  │----原点----│> +x
         *  │     │     │
         *  │     │     │
         *  2-----│-----1
         *        ᐯ +z
         */
        halfSideLength, -halfSideLength, -halfSideLength,    // 0
        halfSideLength, -halfSideLength, halfSideLength,     // 1
        -halfSideLength, -halfSideLength, halfSideLength,    // 2
        halfSideLength, -halfSideLength, -halfSideLength,    // 0
        -halfSideLength, -halfSideLength, halfSideLength,    // 2
        -halfSideLength, -halfSideLength, -halfSideLength,   // 3
    )

    private fun getNormalList() = floatArrayOf(
        // 正面
        0F, 0F, 1F,
        0F, 0F, 1F,
        0F, 0F, 1F,
        0F, 0F, 1F,
        0F, 0F, 1F,
        0F, 0F, 1F,

        // 后面
        0F, 0F, -1F,
        0F, 0F, -1F,
        0F, 0F, -1F,
        0F, 0F, -1F,
        0F, 0F, -1F,
        0F, 0F, -1F,

        // 左面
        -1F, 0F, 0F,
        -1F, 0F, 0F,
        -1F, 0F, 0F,
        -1F, 0F, 0F,
        -1F, 0F, 0F,
        -1F, 0F, 0F,

        // 右面
        1F, 0F, 0F,
        1F, 0F, 0F,
        1F, 0F, 0F,
        1F, 0F, 0F,
        1F, 0F, 0F,
        1F, 0F, 0F,

        // 上面
        0F, 1F, 0F,
        0F, 1F, 0F,
        0F, 1F, 0F,
        0F, 1F, 0F,
        0F, 1F, 0F,
        0F, 1F, 0F,

        // 下面
        0F, -1F, 0F,
        0F, -1F, 0F,
        0F, -1F, 0F,
        0F, -1F, 0F,
        0F, -1F, 0F,
        0F, -1F, 0F
    )

    private fun getTextureList() = floatArrayOf(
        1F, 1F,
        0F, 1F,
        0F, 0F,
        1F, 1F,
        0F, 0F,
        1F, 0F,

        1F, 1F,
        1F, 0F,
        0F, 0F,
        1F, 1F,
        0F, 0F,
        0F, 1F,

        1F, 1F,
        0F, 1F,
        0F, 0F,
        1F, 1F,
        0F, 0F,
        1F, 0F,

        1F, 1F,
        1F, 0F,
        0F, 0F,
        1F, 1F,
        0F, 0F,
        0F, 1F,

        1F, 1F,
        0F, 0F,
        1F, 0F,
        1F, 1F,
        0F, 1F,
        0F, 0F,

        1F, 1F,
        1F, 0F,
        0F, 0F,
        1F, 1F,
        0F, 0F,
        0F, 1F,
    )
}