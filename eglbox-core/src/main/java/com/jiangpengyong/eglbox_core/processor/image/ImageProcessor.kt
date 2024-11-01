package com.jiangpengyong.eglbox_core.processor.image

import android.os.Message
import android.util.Log
import com.jiangpengyong.eglbox_core.GLThread
import com.jiangpengyong.eglbox_core.engine.GLEngine
import com.jiangpengyong.eglbox_core.engine.RenderType
import com.jiangpengyong.eglbox_core.filter.FilterChain
import com.jiangpengyong.eglbox_core.filter.FilterData
import com.jiangpengyong.eglbox_core.filter.GLFilter
import com.jiangpengyong.eglbox_core.filter.GLFilterGroup
import com.jiangpengyong.eglbox_core.processor.GLProcessor
import com.jiangpengyong.eglbox_core.processor.ImageMessageType
import com.jiangpengyong.eglbox_core.view.FilterCenter
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class ImageProcessor : GLProcessor() {
    private val mIsProcessingTask = AtomicBoolean(false)
    private val mIdCreator = AtomicInteger()
    private var mTaskPool: TaskPool? = null
    private val mProcessFilters = GLFilterGroup()

    override fun onLaunch() {
        mTaskPool = TaskPool()
    }

    override fun onDestroy() {
        mTaskPool?.quit()
        mTaskPool?.removeAllTask()
        mTaskPool = null
    }

    override fun createGLEngine(): GLEngine = GLEngine.createPBufferType()

    override fun getRenderType(): RenderType = RenderType.OffScreen

    override fun configFilterChain(filterChain: FilterChain) {
        filterChain.setSourceFilter(SOURCE_FILTER_ID, ImageSourceFilter())
        filterChain.addFilter(PROCESS_FILTER, PROCESS_FILTER, 0, mProcessFilters)
        filterChain.setSinkFilter(SINK_FILTER_ID, ImageSinkFilter())
    }

    override fun onReceiveMessageFromFilter(filterId: String, message: Message) {
        Log.i(TAG, "onReceiveMessageFromFilter filterId=${filterId} message=${message.what}")
        when (message.what) {
            ImageMessageType.RESULT_OUTPUT -> {
                val result = message.obj as? ImageResult
                if (result == null) {
                    Log.e(TAG, "PROCESS_RESULT_OUTPUT data is null.")
                    return
                }
                val pixelBuffer = result.pixelBuffer
                val imageSize = result.imageSize
                val data = result.data
                if (result.isSuccess && pixelBuffer != null && imageSize != null && data != null) {
                    result.callback?.onSuccess(pixelBuffer, imageSize, data)
                } else {
                    result.callback?.onFailure(result.error ?: ImageError.OutputParamsInvalid)
                }
            }
        }
    }

    fun getTaskSize(): Int {
        if (!isLaunched()) {
            Log.e(TAG, "ImageProcessor hasn't launch.")
            return 0
        }
        val size = if (mIsProcessingTask.get()) 1 else 0
        return (mTaskPool?.getSize() ?: 0) + size
    }

    fun process(params: ImageParams, callback: ProcessFinishCallback): Int {
        if (!isLaunched()) {
            Log.e(TAG, "ImageProcessor hasn't launch.")
            return 0
        }
        val id = mIdCreator.incrementAndGet()
        mTaskPool?.saveTask(ImageProcessTask(id, params, callback))
        enqueueEvent {
            mTaskPool?.getTask(id) {
                mIsProcessingTask.set(true)
            }?.let { task ->
                processTask(task)
            }
        }
        return id
    }

    fun isLaunched(): Boolean = mTaskPool?.isRunning()?.get() ?: false

    @GLThread
    private fun processTask(task: ImageProcessTask) {
        Log.i(TAG, "Start process task. task=${task}")

        // 1、恢复滤镜
        mProcessFilters.removeAllFilters()
        val params = task.params
        // 第一层去掉 Filter
        for (item in params.filterData.children) {
            val filter = try {
                restoreFilterChain(item)
            } catch (e: Exception) {
                null
            }
            if (filter == null) {
                Log.e(TAG, "Filter chain restore failure.")
                handleError(task.id, task.callback, ImageError.FilterCreateFailure)
                return
            }
            mProcessFilters.addFilter(filter)
        }
        mProcessFilters.restoreData(params.filterData)
        Log.i(TAG, "processTask=${mProcessFilters}")

        // 2、准备滤镜链上下文
        val filterChain = mGLRenderer?.filterChain
        if (filterChain == null) {
            Log.e(TAG, "Filter chain is nullptr.")
            handleError(task.id, task.callback, ImageError.FilterChainInvalid)
            return
        }
        filterChain.setDeviceOrientation(params.deviceOrientation)
        filterChain.setRenderData(task.params.data)

        // 3、设置源滤镜数据
        Message.obtain().apply {
            what = ImageMessageType.INPUT_PARAMS
            obj = params
            filterChain.sendMessageToFilter(SOURCE_FILTER_ID, this)
            recycle()
        }

        // 4、设置结束滤镜数据
        Message.obtain().apply {
            what = ImageMessageType.INPUT_CALLBACK
            arg1 = task.id
            obj = task.callback
            filterChain.sendMessageToFilter(SINK_FILTER_ID, this)
            recycle()
        }

        // 5、渲染
        val glThread = mGLEngine?.getGLThread()
        if (glThread == null) {
            Log.e(TAG, "GLThread is null.")
            handleError(task.id, task.callback, ImageError.GLThreadInvalid)
            return
        }
        glThread.handleRequestRender()

        // 6、回收
        mProcessFilters.removeAllFilters()
        mIsProcessingTask.set(false)
    }

    private fun restoreFilterChain(filterData: FilterData): GLFilter? {
        val filter = FilterCenter.createFilter(filterData.name)
        if (filter == null) {
            Log.e(TAG, "Can't find filter in FilterCenter. name=${filterData.name}")
            return null
        }
        filter.id = filterData.id
        filter.name = filterData.name
        filter.order = filterData.order
        if (filter is GLFilterGroup) {
            for (item in filterData.children) {
                val childFilter = this.restoreFilterChain(item)
                if (childFilter == null) {
                    Log.e(TAG, "Filter is null filterData=${item}")
                    throw Exception("Restore filter chain failure.")
                }
                filter.addFilter(childFilter)
            }
            Log.i(TAG, "It's a middle filter. filter=${filter}, filterData=${filterData}")
        } else {
            Log.i(TAG, "It's a leaf filter. filterData=${filterData}")
        }
        return filter
    }

    private fun handleError(processId: Int, callback: ProcessFinishCallback, error: ImageError) {
        mProcessFilters.removeAllFilters()
        val imageResult = ImageResult(
            isSuccess = false,
            processId = processId,
            callback = callback,
            error = error,
        )
        val message = Message.obtain().apply {
            what = ImageMessageType.RESULT_OUTPUT
            obj = imageResult
        }
        getFilterChain()?.getContext()?.sendMessage(SINK_FILTER_ID, message)
    }

    companion object {
        private const val TAG = "OffscreenProcessor"
        private const val PROCESS_FILTER = "process-filter-group"
    }
}

enum class ImageError {
    FilterCreateFailure,
    FilterChainInvalid,
    GLThreadInvalid,
    OutputParamsInvalid, // 输出参数异常
    ReadPixelsFailure, // 读取像素失败
    SnapshotFailure, // 截图失败
}