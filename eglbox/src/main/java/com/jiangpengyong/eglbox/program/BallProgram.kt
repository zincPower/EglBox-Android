//package com.jiangpengyong.eglbox.program
//
//import com.jiangpengyong.eglbox.gles.GLProgram
//
///**
// * @author jiang peng yong
// * @date 2024/6/19 10:08
// * @email 56002982@qq.com
// * @des 绘制球，两种方式
// * [BallProgram.BallType.Trigonometric] 使用三角函数构建球体
// * [BallProgram.BallType.Geometry] 使用二十面体构建外接球
// */
//class BallProgram(ballType: BallType) : GLProgram() {
//    enum class BallType { Trigonometric, Geometry }
//
//    private val mBallType: BallType = ballType
//    private val mBallProgram: BallBase = when (ballType) {
//        BallType.Trigonometric -> TrigonometricBallProgram()
//        BallType.Geometry -> GeometryBallProgram()
//    }
//
//    override fun onInit() {
//        mBallProgram.init()
//    }
//
//    override fun onDraw() {
//        mBallProgram.draw()
//    }
//
//    override fun onRelease() {
//        mBallProgram.release()
//    }
//
//    override fun getVertexShaderSource(): String = mBallProgram.getVertexShaderSource()
//
//    override fun getFragmentShaderSource(): String = mBallProgram.getFragmentShaderSource()
//}
//
//interface BallBase {
//    fun onInit()
//    fun onDraw()
//    fun onRelease()
//    fun getVertexShaderSource(): String
//    fun getFragmentShaderSource(): String
//}
//
//class TrigonometricBallProgram : BallBase {
//    override fun onInit() {
//        TODO("Not yet implemented")
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
//}
//
//class GeometryBallProgram : BallBase {
//    override fun onInit() {
//
//    }
//
//    override fun onDraw() {
//
//    }
//
//    override fun onRelease() {
//    }
//
//    override fun getVertexShaderSource(): String {
//    }
//
//    override fun getFragmentShaderSource(): String {
//    }
//}