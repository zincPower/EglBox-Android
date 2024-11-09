package com.jiangpengyong.sample.g_model

import android.content.res.Resources
import android.opengl.GLES20
import android.util.Log
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.utils.allocateFloatBuffer
import com.jiangpengyong.sample.utils.Math3D
import com.jiangpengyong.sample.utils.Vector
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.FloatBuffer

/**
 * @author jiang peng yong
 * @date 2024/9/23 21:24
 * @email 56002982@qq.com
 * @des: 加载 3ds max obj 文件的工具
 * 3ds max obj 文件格式如下：
 * ============================文件开始================================
 * # jiang peng yong
 * #
 * v -19.990179 -34.931675 -18.201921
 * v 20.111662 -34.931675 -18.201921
 * ……
 * v 20.111662 27.748880 21.994425
 * # 8 vertices
 * vt 0.000000 0.000000 0.000000
 * vt 1.000000 0.000000 0.000000
 * ……
 * vt 1.000000 1.000000 0.000000
 * # 12 texture vertices
 * vn 0.000000 0.000000 -1.570796
 * vn 0.000000 0.000000 -1.570796
 * ……
 * vn 0.000000 0.000000 1.570796
 * # 8 vertex normals
 * g (null)
 * f 1/10/1 3/12/3 4/11/4
 * f 4/11/4 2/9/2 1/10/1
 * ……
 * f 5/4/5 7/3/7 3/1/3
 * # 12 faces
 * g
 * ==============================文件结束==============================
 * 符号说明：
 * # -----> 注释
 * v -----> 顶点坐标
 * vt ----> 顶点纹理坐标
 * vn ----> 顶点法向量
 * g -----> g 开始，后续有带字符串表示一组面开始
 *   -----> 如果再出现一个 g ，表示一组的结束
 * f -----> 一个面
 */
object Obj3DModelLoader {
    private const val TAG = "Load3DMaxObjUtils"

    private const val VERTEX = "v"
    private const val NORMAL = "vn"
    private const val TEXTURE = "vt"
    private const val FACE = "f"

    /**
     * 从 assets 加载资源
     * @param resources 资源
     * @param assetPath 文件路径
     * @param textureSize 纹理需要多少个维度。默认为二个维度，即ST。如果需要三个维度，传入3，则可以得到STP。
     * @param textureFlip 纹理坐标是否需要反转，需要的话，T会进行反转
     * @return 成功则返回 [Model3DInfo] 或失败则返回 null
     */
    fun load(
        resources: Resources,
        assetPath: String,
        textureSize: Int = 2,
        textureFlip: Boolean = false
    ): Model3DInfo? {
        val bufferedReader = getBufferReader(resources, assetPath)
        bufferedReader ?: return null
        return bufferedReader.use {
            load(it, textureSize, textureFlip)
        }
    }

    fun load(
        filePath: String,
        textureSize: Int = 2,
        textureFlip: Boolean = false
    ): Model3DInfo? {
        val bufferedReader = getBufferReader(filePath)
        bufferedReader ?: return null
        return load(bufferedReader, textureSize, textureFlip)
    }

    fun load(
        file: File,
        textureSize: Int = 2,
        textureFlip: Boolean = false
    ): Model3DInfo? {
        val bufferedReader = getBufferReader(file)
        bufferedReader ?: return null
        return load(bufferedReader, textureSize, textureFlip)
    }

    private fun load(
        bufferedReader: BufferedReader,
        textureSize: Int,
        textureFlip: Boolean,
    ): Model3DInfo? {
        var line: String?
        val vertexData = arrayListOf<Float>()
        val normalData = arrayListOf<Float>()
        val textureData = arrayListOf<Float>()

        // 组成面的顶点数据
        val vertexResultData = arrayListOf<Float>()
        // 组成面的纹理数据
        val textureResultData = arrayListOf<Float>()
        // 组成面的法向量数据
        val normalResultData = arrayListOf<Float>()

        var isSuccess = true
        var top = Float.MIN_VALUE
        var bottom = Float.MAX_VALUE
        var left = Float.MAX_VALUE
        var right = Float.MIN_VALUE
        var near = Float.MIN_VALUE
        var far = Float.MAX_VALUE

        loop@ while (bufferedReader.readLine().also { line = it } != null) {

            val conLine = line ?: continue

            // 为注释行或空行，则跳过
            if (conLine.startsWith("#") || conLine.isEmpty()) continue

            // 解析一行的数据，将 conLine 字符串按照一个或多个空格进行分割，返回一个包含分割后子字符串的列表。
            // v -19.990179 -34.931675 -18.201921 会拆解为 [v, -19.990179, -34.931675, -18.201921]
            val temps = conLine.split(" +".toRegex())
            // 如果没有数据则跳过
            if (temps.isEmpty()) continue

            // 按照 temps 第一个元素，判断是什么类型，进行组装
            // TODO 这里可能还需要考虑多个组成
            when (temps[0].trim()) {
                VERTEX -> {     // 顶点数据，例如 "v 20.111662 27.748880 21.994425"
                    // 必须要为 4 个数据（包括类型），例如 [v, 20.111662, 27.748880, 21.994425]
                    if (temps.size < 4) {
                        isSuccess = false
                        break@loop
                    }

                    // 获取顶点数据，并存入
                    try {
                        val x = temps[1].toFloat()
                        val y = temps[2].toFloat()
                        val z = temps[3].toFloat()
                        top = Math.max(y, top)
                        bottom = Math.min(y, bottom)
                        left = Math.min(x, left)
                        right = Math.max(x, right)
                        near = Math.max(z, near)
                        far = Math.min(z, far)
                        vertexData.add(x)
                        vertexData.add(y)
                        vertexData.add(z)
                    } catch (e: Exception) {
                        Logger.e(TAG, "Vertex data parse failure. e=${e.message}")
                        isSuccess = false
                        break@loop
                    }
                }

                NORMAL -> {     // 法向量数据，例如 "vn 0.000000 0.000000 -1.570796"
                    // 必须要为 4 个数据（包括类型），例如 [vn, 0.000000, 0.000000, -1.570796]
                    if (temps.size < 4) {
                        isSuccess = false
                        break@loop
                    }

                    // 获取法向量数据，并存入
                    try {
                        normalData.add(temps[1].toFloat())
                        normalData.add(temps[2].toFloat())
                        normalData.add(temps[3].toFloat())
                    } catch (e: Exception) {
                        Logger.e(TAG, "Normal data parse failure. e=${e.message}")
                        isSuccess = false
                        break@loop
                    }
                }

                TEXTURE -> {    // 纹理数据，一般情况下只有 uv 数据，即只有三个数据（包括类型），但也存在 uvw 数据，即四个数据（包括类型）。例如 例如 "vt 1.000000 1.000000" 或 "vt 1.000000 1.000000 0.000000"
                    if (temps.size < (textureSize + 1)) {
                        isSuccess = false
                        break@loop
                    }

                    try {
                        var index = 1
                        while (index < (textureSize + 1)) {
                            if (index == 2 && textureFlip) {
                                textureData.add(1 - temps[index++].toFloat())
                            } else {
                                textureData.add(temps[index++].toFloat())
                            }
                        }
                    } catch (e: Exception) {
                        Logger.e(TAG, "Texture data parse failure. e=${e.message}")
                        isSuccess = false
                        break@loop
                    }
                }

                FACE -> {       // 面数据，例如 "f 1/10/1 3/12/3 4/11/4"
                    if (temps.size < 4) {
                        isSuccess = false
                        break@loop
                    }

                    // OpenGL es 所有的面都是三角形，每行三组数据，代表 第一个顶点 第一个顶点 第三个顶点
                    // 点数据分别为 v/vt/vn 顶点索引/纹理坐标索引/法线向量索引
                    // 索引通常从 1 开始，而不是 0 ，负索引表示从文件末尾开始计数
                    // TODO 这里不处理负索引情况
                    // v/vt/vn 并不是一定都存在，存在以下情况：
                    // 只有顶点数据：f v1 v2 v3
                    // 只存在顶点数据和法向量数据：f v1//n1 v2//n2 v3//n3
                    // 只存在顶点数据和纹理数据：f v1/t1 v2/t2 v3/t3
                    // 顶点数据、纹理数据和法向量数据都存在：f v1/t1/n1 v2/t2/n2 v3/t3/n3
                    // 第一个顶点  "1/10/1"
                    val p1 = temps[1].split("/".toRegex())
                    // 第一个顶点  "3/12/3"
                    val p2 = temps[2].split("/".toRegex())
                    // 第三个顶点  "4/11/4"
                    val p3 = temps[3].split("/".toRegex())
                    // 因为存在多种数据的可能，保证每组数据长度一样
                    if (p1.size != p2.size || p2.size != p3.size) {
                        isSuccess = false
                        break@loop
                    }

                    val isNeedCalculateNormal = p1.size < 3

                    // ================================= 顶点 start =================================
                    // 第一个点
                    try {
                        // p1 行号是从 1 开始，所以要 减一
                        val index1 = p1[0].toInt() - 1
                        val x1 = vertexData[index1 * 3]
                        val y1 = vertexData[index1 * 3 + 1]
                        val z1 = vertexData[index1 * 3 + 2]
                        vertexResultData.add(x1)
                        vertexResultData.add(y1)
                        vertexResultData.add(z1)

                        // p2 行号是从 1 开始，所以要 减一
                        val index2 = p2[0].toInt() - 1
                        val x2 = vertexData[index2 * 3]
                        val y2 = vertexData[index2 * 3 + 1]
                        val z2 = vertexData[index2 * 3 + 2]
                        vertexResultData.add(x2)
                        vertexResultData.add(y2)
                        vertexResultData.add(z2)

                        // p3 行号是从 1 开始，所以要 减一
                        val index3 = p3[0].toInt() - 1
                        val x3 = vertexData[index3 * 3]
                        val y3 = vertexData[index3 * 3 + 1]
                        val z3 = vertexData[index3 * 3 + 2]
                        vertexResultData.add(x3)
                        vertexResultData.add(y3)
                        vertexResultData.add(z3)

                        if (isNeedCalculateNormal) {
                            val normal = Math3D.vectorNormal(
                                Math3D.crossProduct(
                                    v1 = Vector(x2 - x1, y2 - y1, z2 - z1),
                                    v2 = Vector(x3 - x1, y3 - y1, z3 - z1),
                                )
                            )
                            for (i in 0..2) {
                                normalResultData.add(normal.x)
                                normalResultData.add(normal.y)
                                normalResultData.add(normal.z)
                            }
                        }
                    } catch (e: Exception) {
                        Logger.e(TAG, "Obtain texture failure. e=${e.message}")
                        isSuccess = false
                        break@loop
                    }
                    // ================================= 顶点 end ===================================

                    // 如果点数据小于 2 ，说明只有顶点数据，其他数据没有，则进行下次循环
                    if (p1.size < 2 || p2.size < 2 || p3.size < 2) {
                        continue@loop
                    }

                    // ================================= 纹理 start =================================
                    // 如果 p1 数据长度大于等于 2 ，则说明有纹理数据，但有可能为空，存在这种情况 f v1//n1 v2//n2 v3//n3
                    // 所以不为空则进行获取纹理
                    if (p1[1].isNotEmpty() && p2[1].isNotEmpty() && p3[1].isNotEmpty()) {
                        try {
                            // p1 行号是从 1 开始，所以要 减一
                            val index1 = p1[1].toInt() - 1
                            // s
                            textureResultData.add(textureData[index1 * textureSize])
                            // t
                            textureResultData.add(textureData[index1 * textureSize + 1])
                            if (textureSize > 2) {
                                // p
                                textureResultData.add(textureData[index1 * textureSize + 2])
                            }

                            // p2 行号是从 1 开始，所以要 减一
                            val index2 = p2[1].toInt() - 1
                            // s
                            textureResultData.add(textureData[index2 * textureSize])
                            // t
                            textureResultData.add(textureData[index2 * textureSize + 1])
                            if (textureSize > 2) {
                                // p
                                textureResultData.add(textureData[index2 * textureSize + 2])
                            }

                            // p3 行号是从 1 开始，所以要 减一
                            val index3 = p3[1].toInt() - 1
                            // s
                            textureResultData.add(textureData[index3 * textureSize])
                            // t
                            textureResultData.add(textureData[index3 * textureSize + 1])
                            if (textureSize > 2) {
                                // p
                                textureResultData.add(textureData[index3 * textureSize + 2])
                            }
                        } catch (e: Exception) {
                            Logger.e(TAG, "Obtain texture failure. e=${e.message}")
                            isSuccess = false
                            break@loop
                        }
                    }
                    // ================================= 纹理 end ===================================

                    // 只有三个数据才存在法向量
                    if (p1.size < 3 || p2.size < 3 || p3.size < 3) {
                        continue@loop
                    }

                    // ================================= 法向量 start ===============================
                    try {
                        // p1 行号是从 1 开始，所以要 减一
                        val index1 = p1[2].toInt() - 1
                        // x
                        normalResultData.add(normalData[index1 * 3])
                        // y
                        normalResultData.add(normalData[index1 * 3 + 1])
                        // z
                        normalResultData.add(normalData[index1 * 3 + 2])

                        // p2 行号是从 1 开始，所以要 减一
                        val index2 = p2[2].toInt() - 1
                        // x
                        normalResultData.add(normalData[index2 * 3])
                        // y
                        normalResultData.add(normalData[index2 * 3 + 1])
                        // z
                        normalResultData.add(normalData[index2 * 3 + 2])

                        // p3 行号是从 1 开始，所以要 减一
                        val index3 = p3[2].toInt() - 1
                        // x
                        normalResultData.add(normalData[index3 * 3])
                        // y
                        normalResultData.add(normalData[index3 * 3 + 1])
                        // z
                        normalResultData.add(normalData[index3 * 3 + 2])
                    } catch (e: Exception) {
                        Logger.e(TAG, "Obtain normal failure. e=${e.message}")
                        isSuccess = false
                        break@loop
                    }
                    // ================================= 法向量 end =================================
                }
            }
        }

        return if (isSuccess) {  // 成功
            Model3DInfo(
                vertexData = vertexResultData.toFloatArray(),
                textureData = textureResultData.toFloatArray(),
                textureStep = textureSize,
                normalData = normalResultData.toFloatArray(),
                frontFace = FrontFace.CCW,
                space = Space(top, bottom, left, right, near, far)
            )
        } else {
            null
        }
    }

    private fun getBufferReader(resource: Resources, assetPath: String): BufferedReader? {
        return try {
            val inputStream = resource.assets.open(assetPath)
            val inputStreamReader = InputStreamReader(inputStream)
            BufferedReader(inputStreamReader)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getBufferReader(filePath: String): BufferedReader? {
        return try {
            val file = File(filePath)
            getBufferReader(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getBufferReader(file: File): BufferedReader? {
        return try {
            if (!file.exists()) return null
            val inputStreamReader = InputStreamReader(file.inputStream())
            BufferedReader(inputStreamReader)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * @author jiang peng yong
 * @date 2024/9/23 21:19
 * @email 56002982@qq.com
 * @des 3D 模型信息
 */
data class Model3DInfo(
    val vertexData: FloatArray,            // 顶点数据
    val textureData: FloatArray?,          // 纹理数据
    val textureStep: Int = 0,              // 纹理跨度
    val normalData: FloatArray?,           // 法向量数据
    val frontFace: FrontFace,              // 卷绕方向
    val space: Space,                      // 空间
) {
    /**
     * 顶点数据
     */
    val count: Int
        get() = vertexData.size / 3

    /**
     * 是否有法向量数据
     */
    val hasNormal: Boolean
        get() = (normalData?.size ?: 0) > 0

    /**
     * 是否有纹理数据
     */
    val hasTexture: Boolean
        get() = (textureData?.size ?: 0) > 0

    val vertexBuffer: FloatBuffer
        get() = allocateFloatBuffer(vertexData)

    val textureBuffer: FloatBuffer?
        get() = textureData?.let { allocateFloatBuffer(it) }

    val normalBuffer: FloatBuffer?
        get() = normalData?.let { allocateFloatBuffer(it) }
}

/**
 * @author jiang peng yong
 * @date 2024/10/10 07:49
 * @email 56002982@qq.com
 * @des 卷绕方向
 */
enum class FrontFace(val value: Int) {
    CW(GLES20.GL_CW),
    CCW(GLES20.GL_CCW),
}

/**
 * @author jiang peng yong
 * @date 2024/10/18 22:40
 * @email 56002982@qq.com
 * @des 空间
 */
data class Space(
    val top: Float,
    val bottom: Float,
    val left: Float,
    val right: Float,
    val near: Float,
    val far: Float,
)