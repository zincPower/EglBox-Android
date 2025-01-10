package com.jiangpengyong.eglbox_filter.utils

import android.content.res.Resources
import android.util.Log
import com.jiangpengyong.eglbox_core.logger.Logger
import com.jiangpengyong.eglbox_core.space3d.Vector
import com.jiangpengyong.eglbox_core.utils.Math3D
import com.jiangpengyong.eglbox_filter.model.FrontFace
import com.jiangpengyong.eglbox_filter.model.ModelData
import com.jiangpengyong.eglbox_filter.model.NormalVectorType
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

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
    /**
     * 从 assets 加载资源
     * @param resources 资源
     * @param assetPath 文件路径
     * @param textureSize 纹理需要多少个维度。默认为二个维度，即ST。如果需要三个维度，传入3，则可以得到STP。
     * @param textureFlip 纹理坐标是否需要反转，需要的话，T会进行反转
     * @param normalVectorType 法向量类型
     * @return 成功则返回 [ModelData] 或失败则返回 null
     */
    fun load(
        resources: Resources,
        assetPath: String,
        textureSize: Int = 2,
        textureFlip: Boolean = false,
        normalVectorType: NormalVectorType = NormalVectorType.Vertex,
    ): ModelData? {
        val bufferedReader = getBufferReader(resources, assetPath)
        bufferedReader ?: return null
        return bufferedReader.use {
            load(it, textureSize, textureFlip, normalVectorType)
        }
    }

    fun load(
        filePath: String,
        textureSize: Int = 2,
        textureFlip: Boolean = false,
        normalVectorType: NormalVectorType = NormalVectorType.Vertex,
    ): ModelData? {
        val bufferedReader = getBufferReader(filePath)
        bufferedReader ?: return null
        return bufferedReader.use {
            load(bufferedReader, textureSize, textureFlip, normalVectorType)
        }
    }

    fun load(
        file: File,
        textureSize: Int = 2,
        textureFlip: Boolean = false,
        normalVectorType: NormalVectorType = NormalVectorType.Vertex,
    ): ModelData? {
        val bufferedReader = getBufferReader(file)
        bufferedReader ?: return null
        return bufferedReader.use {
            load(bufferedReader, textureSize, textureFlip, normalVectorType)
        }
    }

    private fun load(
        bufferedReader: BufferedReader,
        textureSize: Int,
        textureFlip: Boolean,
        normalVectorType: NormalVectorType = NormalVectorType.Vertex,
    ): ModelData? {
        return Obj3DModelParser(
            bufferedReader = bufferedReader,
            textureSize = textureSize,
            textureFlip = textureFlip,
            normalVectorType = normalVectorType,
        ).parse()
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
 * @date 2024/11/16 15:06
 * @email 56002982@qq.com
 * @des 3D 模型解析器
 */
class Obj3DModelParser(
    val bufferedReader: BufferedReader,
    val textureSize: Int,
    val textureFlip: Boolean,
    val normalVectorType: NormalVectorType,
) {
    private val vertexData = arrayListOf<Float>()
    private val normalData = arrayListOf<Float>()
    private val textureData = arrayListOf<Float>()

    // 组成面的顶点数据
    private val vertexResultData = arrayListOf<Float>()

    // 组成面的纹理数据
    private val textureResultData = arrayListOf<Float>()

    // 组成面的法向量数据
    private val normalResultData = arrayListOf<Float>()

    private var isNeedCalculateNormal = false
    private val faceVertexIndex = ArrayList<Int>()
    private val vertexNormalMap = HashMap<Int, Normal>()

    // 记录模型的边界
    private var top = Float.MIN_VALUE
    private var bottom = Float.MAX_VALUE
    private var left = Float.MAX_VALUE
    private var right = Float.MIN_VALUE
    private var near = Float.MIN_VALUE
    private var far = Float.MAX_VALUE

    fun parse(): ModelData? {
        init()
        var line: String?

        var isSuccess = false
        while (bufferedReader.readLine().also { line = it } != null) {
            val conLine = line ?: continue

            // 为注释行或空行，则跳过
            if (conLine.startsWith("#") || conLine.isEmpty()) continue

            // 解析一行的数据，将 conLine 字符串按照一个或多个空格进行分割，返回一个包含分割后子字符串的列表。
            // v -19.990179 -34.931675 -18.201921 会拆解为 [v, -19.990179, -34.931675, -18.201921]
            val values = conLine.split(" +".toRegex())
            // 如果没有数据则跳过
            if (values.isEmpty()) continue

            // 按照 temps 第一个元素，判断是什么类型，进行组装
            // TODO 这里可能还需要考虑多个组成
            isSuccess = when (values[0].trim()) {
                VERTEX -> handleVertex(values)
                NORMAL -> handleNormal(values)
                TEXTURE -> handleTexture(values)
                FACE -> handleFace(values)
                else -> true
            }

            if (!isSuccess) break
        }

        // 如果需要计算法向量并且是点法向量，则需要进行法向量组装
        if (isNeedCalculateNormal && normalVectorType == NormalVectorType.Vertex) {
            normalResultData.clear()
            for (item in faceVertexIndex) {
                val vertexNormal = vertexNormalMap[item]
                if (vertexNormal == null) {
                    Log.e(TAG, "Normal vector is invalid when assemble vertex normal.")
                    return null
                }
                vertexNormal.getAvg().apply {
                    normalResultData.add(x)
                    normalResultData.add(y)
                    normalResultData.add(z)
                }
            }
        }

        return if (isSuccess) {  // 成功
            ModelData(
                vertexData = vertexResultData.toFloatArray(),
                textureData = if (textureResultData.isEmpty()) null else textureResultData.toFloatArray(),
                textureStep = textureSize,
                normalData = if (normalResultData.isEmpty()) null else normalResultData.toFloatArray(),
                frontFace = FrontFace.CCW,
                normalVectorType = normalVectorType,
            )
        } else {
            null
        }
    }

    private fun init() {
        vertexData.clear()
        normalData.clear()
        textureData.clear()

        vertexResultData.clear()
        textureResultData.clear()
        normalResultData.clear()

        top = Float.MIN_VALUE
        bottom = Float.MAX_VALUE
        left = Float.MAX_VALUE
        right = Float.MIN_VALUE
        near = Float.MIN_VALUE
        far = Float.MAX_VALUE

        isNeedCalculateNormal = false
        faceVertexIndex.clear()
        vertexNormalMap.clear()
    }

    /**
     * 处理顶点数据
     */
    private fun handleVertex(values: List<String>): Boolean {
        // 顶点数据，例如 "v 20.111662 27.748880 21.994425"
        // 必须要为 4 个数据（包括类型），例如 [v, 20.111662, 27.748880, 21.994425]
        if (values.size < 4) {
            Logger.e(TAG, "Value size is invalid. size=${values.size}【handleVertex】")
            return false
        }

        // 获取顶点数据，并存入
        try {
            val x = values[1].toFloat()
            val y = values[2].toFloat()
            val z = values[3].toFloat()
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
            Logger.e(TAG, "Vertex data parse failure. e=${e.message}【handleVertex】")
            return false
        }
        return true
    }

    /**
     * 处理法向量数据
     */
    private fun handleNormal(values: List<String>): Boolean {
        // 法向量数据，例如 "vn 0.000000 0.000000 -1.570796"
        // 必须要为 4 个数据（包括类型），例如 [vn, 0.000000, 0.000000, -1.570796]
        if (values.size < 4) {
            Logger.e(TAG, "Value size is invalid. size=${values.size}【handleNormal】")
            return false
        }

        // 获取法向量数据，并存入
        try {
            normalData.add(values[1].toFloat())
            normalData.add(values[2].toFloat())
            normalData.add(values[3].toFloat())
        } catch (e: Exception) {
            Logger.e(TAG, "Normal data parse failure. e=${e.message}【handleNormal】")
            return false
        }
        return true
    }

    /**
     * 处理纹理数据
     */
    private fun handleTexture(values: List<String>): Boolean {
        // 纹理数据，一般情况下只有 uv 数据，即只有三个数据（包括类型），但也存在 uvw 数据，即四个数据（包括类型）。
        // 例如 例如 "vt 1.000000 1.000000" 或 "vt 1.000000 1.000000 0.000000"
        if (values.size < (textureSize + 1)) {
            Logger.e(TAG, "Value size is invalid. size=${values.size}【handleTexture】")
            return false
        }

        try {
            var index = 1
            while (index < (textureSize + 1)) {
                if (index == 2 && textureFlip) {
                    textureData.add(1 - values[index++].toFloat())
                } else {
                    textureData.add(values[index++].toFloat())
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Texture data parse failure. e=${e.message}【handleTexture】")
            return false
        }

        return true
    }

    /**
     * 处理面数据
     */
    private fun handleFace(values: List<String>): Boolean {
        // 面数据，例如 "f 1/10/1 3/12/3 4/11/4"
        if (values.size < 4) {
            Logger.e(TAG, "Value size is invalid. size=${values.size}【handleFace】")
            return false
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
        val p1 = values[1].split("/".toRegex())
        // 第一个顶点  "3/12/3"
        val p2 = values[2].split("/".toRegex())
        // 第三个顶点  "4/11/4"
        val p3 = values[3].split("/".toRegex())
        // 因为存在多种数据的可能，保证每组数据长度一样
        if (p1.size != p2.size || p2.size != p3.size) {
            Logger.e(TAG, "Point size is not equal. p1=${p1.size} p2=${p2.size} p3=${p3.size}【handleFace】")
            return false
        }

        isNeedCalculateNormal = p1.size < 3

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

            faceVertexIndex.add(index1)
            faceVertexIndex.add(index2)
            faceVertexIndex.add(index3)

            if (isNeedCalculateNormal) {
                val normal = Math3D.vectorNormal(
                    Math3D.crossProduct(
                        v1 = Vector(x2 - x1, y2 - y1, z2 - z1),
                        v2 = Vector(x3 - x1, y3 - y1, z3 - z1),
                    )
                )
                when (normalVectorType) {
                    NormalVectorType.Vertex -> {
                        for (i in arrayListOf(index1, index2, index3)) {
                            var curVertexNormal = vertexNormalMap[i]
                            if (curVertexNormal == null) {
                                curVertexNormal = Normal()
                                vertexNormalMap[i] = curVertexNormal
                            }
                            curVertexNormal.add(normal)
                        }
                    }

                    NormalVectorType.Surface -> {
                        for (i in 0..2) {
                            normalResultData.add(normal.x)
                            normalResultData.add(normal.y)
                            normalResultData.add(normal.z)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Obtain texture failure. e=${e.message}【handleFace】")
            return false
        }
        // ================================= 顶点 end ===================================

        // 如果点数据小于 2 ，说明只有顶点数据，其他数据没有，则进行下次循环
        if (p1.size < 2 || p2.size < 2 || p3.size < 2) {
            return true
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
                Logger.e(TAG, "Obtain texture failure. e=${e.message}【handleFace】")
                return false
            }
        }
        // ================================= 纹理 end ===================================

        // 只有三个数据才存在法向量
        if (p1.size < 3 || p2.size < 3 || p3.size < 3) {
            return true
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
            Logger.e(TAG, "Obtain normal failure. e=${e.message}【handleFace】")
            return false
        }
        // ================================= 法向量 end =================================\

        return true
    }

    companion object {
        private const val TAG = "Load3DMaxObjUtils"

        private const val VERTEX = "v"
        private const val NORMAL = "vn"
        private const val TEXTURE = "vt"
        private const val FACE = "f"
    }
}