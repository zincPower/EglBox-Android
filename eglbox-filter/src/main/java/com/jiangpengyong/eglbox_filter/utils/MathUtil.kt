package com.jiangpengyong.eglbox_filter.utils

object MathUtil {
    /**
     * 通过 doolittle 分解解 n 元一次线性方程组的工具方法
     */
    fun doolittle(a: Array<DoubleArray>): DoubleArray {
        val rowNum = a.size
        val xnum = a[0].size - rowNum

        val AugMatrix = Array(10) { DoubleArray(20) }

        readData(a, rowNum, xnum, AugMatrix)

        for (i in 1..rowNum) {
            prepareChoose(i, rowNum, AugMatrix)
            choose(i, rowNum, xnum, AugMatrix)
            resolve(i, rowNum, xnum, AugMatrix)
        }

        findX(rowNum, xnum, AugMatrix)

        val result = DoubleArray(rowNum)
        for (i in 0 until rowNum) {
            result[i] = AugMatrix[i + 1][rowNum + 1]
        }

        return result
    }

    /**
     * 增广矩阵的拓展
     */
    private fun readData(a: Array<DoubleArray>, rowNum: Int, xnum: Int, AugMatrix: Array<DoubleArray>) {
        for (i in 0..rowNum) {
            AugMatrix[i][0] = 0.0
        }
        for (i in 0..rowNum + xnum) {
            AugMatrix[0][i] = 0.0
        }
        for (i in 1..rowNum) for (j in 1..rowNum + xnum) AugMatrix[i][j] = a[i - 1][j - 1]
    }

    /**
     * 计算准备选主元
     */
    private fun prepareChoose(times: Int, rowNum: Int, AugMatrix: Array<DoubleArray>) {
        for (i in times..rowNum) {
            for (j in times - 1 downTo 1) {
                AugMatrix[i][times] = AugMatrix[i][times] - AugMatrix[i][j] * AugMatrix[j][times]
            }
        }
    }

    /**
     * 选主元
     */
    private fun choose(times: Int, rowNum: Int, xnum: Int, AugMatrix: Array<DoubleArray>) {
        var line = times
        for (i in times + 1..rowNum)  //选最大行
        {
            if (AugMatrix[i][times] * AugMatrix[i][times] > AugMatrix[line][times] * AugMatrix[line][times]) line = i
        }
        if (AugMatrix[line][times] == 0.0) //最大数等于零
        {
            println("doolittle fail !!!")
        }
        if (line != times) //交换
        {
            var temp: Double
            for (i in 1..rowNum + xnum) {
                temp = AugMatrix[times][i]
                AugMatrix[times][i] = AugMatrix[line][i]
                AugMatrix[line][i] = temp
            }
        }
    }

    /**
     * 分解
     */
    private fun resolve(times: Int, rowNum: Int, xnum: Int, AugMatrix: Array<DoubleArray>) {
        for (i in times + 1..rowNum) {
            AugMatrix[i][times] = AugMatrix[i][times] / AugMatrix[times][times]
        }
        for (i in times + 1..rowNum + xnum) {
            for (j in times - 1 downTo 1) {
                AugMatrix[times][i] = AugMatrix[times][i] - AugMatrix[times][j] * AugMatrix[j][i]
            }
        }
    }

    /**
     * 求解
     */
    private fun findX(rowNum: Int, xnum: Int, AugMatrix: Array<DoubleArray>) {
        for (k in 1..xnum) {
            AugMatrix[rowNum][rowNum + k] = AugMatrix[rowNum][rowNum + k] / AugMatrix[rowNum][rowNum]
            for (i in rowNum - 1 downTo 1) {
                for (j in rowNum downTo i + 1) {
                    AugMatrix[i][rowNum + k] = AugMatrix[i][rowNum + k] - AugMatrix[i][j] * AugMatrix[j][rowNum + k]
                }
                AugMatrix[i][rowNum + k] = AugMatrix[i][rowNum + k] / AugMatrix[i][i]
            }
        }
    }
}
