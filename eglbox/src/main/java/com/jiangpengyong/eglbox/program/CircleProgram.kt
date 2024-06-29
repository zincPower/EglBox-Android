//package com.jiangpengyong.eglbox.program
//
//import com.jiangpengyong.eglbox.gles.GLProgram
//
///**
// * @author jiang peng yong
// * @date 2024/6/28 13:08
// * @email 56002982@qq.com
// * @des 绘制圆
// */
//class CircleProgram(
//    private val r: Float,    // 半径
//    private val n: Int,      // 份数
//): GLProgram() {
//    override fun onInit() {
//
//    }
//
//    override fun onDraw() {
//        TODO("Not yet implemented")
//    }
//
//    override fun onRelease() {
//        TODO("Not yet implemented")
//    }
//
//    override fun getVertexShaderSource(): String {
//        TODO("Not yet implemented")
//    }
//
//    override fun getFragmentShaderSource(): String {
//        TODO("Not yet implemented")
//    }
//
//    fun initVertexData() {
//        // 每一份的跨度
//        val span: Float = CIRCLE_ANGLE / n
//
//        // 顶点个数，共有n个三角形，每个三角形都有三个顶点
//        vCount = 3 * n
//
//        // 顶点坐标数组
//        val vertices = FloatArray(vCount * 3)
//        // 顶点纹理坐标数组
//        val textures = FloatArray(vCount * 2)
//        // 顶点坐标的计数器
//        var count = 0
//        // 顶点纹理坐标的计数器
//        var stCount = 0
//
//        var curAngle = 0f
//
//        while (ceil(curAngle.toDouble()) < 360) {
//            //当前弧度
//            val radian = Math.toRadians(curAngle.toDouble())
//            //下一弧度
//            val radianNext = Math.toRadians(curAngle + span.toDouble())
//
//            /**
//             * 顶点
//             *                       ^  y
//             *          y = r*cos(α)｜------------ P3（x, y） 半径r
//             *                   P1 ｜          /|
//             *                      ｜        /  |
//             *                      ｜      /    |
//             *                      ｜ α  /      |
//             *                      ｜  /        |
//             *                      ｜/          |
//             * ---------------------------------------------> x
//             *                    P2｜           x = r*sin(α)
//             *                      ｜
//             *                      ｜
//             *                      ｜
//             *                      ｜
//             *                      ｜
//             */
//
//            /**
//             * 纹理
//             *                                              (1, 0)
//             * ---------------------------------------------> S
//             * ｜                     ｜
//             * ｜       y = r*cos(α)  ｜----------  P3 (s, t)
//             * ｜                 P1  ｜        /｜
//             * ｜                     ｜      /  ｜
//             * ｜                     ｜ α  /    ｜
//             * ｜                     ｜  /      ｜
//             * ｜                     ｜/        ｜
//             * ｜----------------------------------------------
//             * ｜                 P2  ｜(0.5, 0.5)
//             * ｜                     ｜
//             * ｜                     ｜
//             * ｜                     ｜
//             * ｜                     ｜
//             * ｜                     ｜
//             * ｜(0, 1)               ｜
//             * v
//             *
//             * T
//             */
//            // ========================= 生成每个三角形的顶点数据 ================================
//            // ========================= 第一个点P1 ================================
//            // 当前弧度对应的边缘顶点坐标
//            // x
//            vertices[count++] = (r * sin(radian)).toFloat()
//            // y
//            vertices[count++] = (r * cos(radian)).toFloat()
//            // z
//            vertices[count++] = 0f
//            // 当前弧度对应的边缘顶点纹理坐标
//            // S 坐标
//            textures[stCount++] = (0.5f + 0.5f * sin(radian)).toFloat()
//            // T 坐标
//            /**
//             * 公式解析：
//             * 1. 顶点坐标y轴 和 纹理坐标y轴 正好是相反的，所以需要进行取反
//             * 2. 顶点坐标的中心点在原点(0, 0)，而纹理坐标的原点在(0.5, 0.5)，需要进行偏移，每个轴增加 0.5
//             */
//            textures[stCount++] = (0.5f - 0.5f * cos(radian)).toFloat()
//
//            // ========================= 第二个点P2 ================================
//            // 圆面中心点的顶点坐标
//            // x
//            vertices[count++] = 0f
//            // y
//            vertices[count++] = 0f
//            // z
//            vertices[count++] = 0f
//            // 圆面中心点的顶点纹理坐标
//            // S 坐标
//            textures[stCount++] = 0.5f
//            // T 坐标
//            textures[stCount++] = 0.5f
//
//            // ========================= 第三个点P3 ================================
//            // 下一弧度对应的边缘顶点坐标
//            // x
//            vertices[count++] = (r * sin(radianNext)).toFloat()
//            // y
//            vertices[count++] = (r * cos(radianNext)).toFloat()
//            // z
//            vertices[count++] = 0f
//            // 下一弧度对应的边缘顶点纹理坐标
//            // S 坐标
//            textures[stCount++] = (0.5f + 0.5f * sin(radianNext)).toFloat()
//            // T 坐标
//            textures[stCount++] = (0.5f - 0.5f * cos(radianNext)).toFloat()
//            curAngle += span
//        }
//        mVertexBuffer = allocateFloatBuffer(vertices)
//
//        //法向量数据初始化
//        val normals = FloatArray(vertices.size)
//        var i = 0
//        while (i < normals.size) {
//            normals[i] = 0f
//            normals[i + 1] = 0f
//            normals[i + 2] = 1f
//            i += 3
//        }
//        mNormalBuffer = allocateFloatBuffer(normals)
//
//        // 纹理坐标数据初始化
//        mTexCoorBuffer = allocateFloatBuffer(textures)
//
//    }
//
//}