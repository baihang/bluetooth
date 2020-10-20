package com.example.healthy.utils

import android.util.Log
import java.util.concurrent.*

/**
 * 统一处理线程
 */
class ThreadUtil private constructor() {

    private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()

    // Instantiates the queue of Runnables as a LinkedBlockingQueue
    private val workQueue: BlockingQueue<Runnable> =
        LinkedBlockingQueue<Runnable>(5)

    // Sets the Time Unit to seconds
    private val KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS

    // Creates a thread pool manager
    private val threadPoolExecutor: ThreadPoolExecutor = ThreadPoolExecutor(
        NUMBER_OF_CORES,       // Initial pool size
        NUMBER_OF_CORES,       // Max pool size
        KEEP_ALIVE_TIME,
        KEEP_ALIVE_TIME_UNIT,
        workQueue
    )

    companion object {
        // Sets the amount of time an idle thread waits before terminating
        private const val KEEP_ALIVE_TIME = 1L
        private var threadUtil: ThreadUtil? = null

        private const val TAG = "ThreadUtil"


        @Synchronized
        fun getInstance(): ThreadUtil? {
            if (threadUtil == null) {
                threadUtil = ThreadUtil()
            }
            return threadUtil
        }
    }

    fun addThread(runnable: Runnable) {
        threadPoolExecutor.execute(runnable)
    }

    fun addTimeListener(listener: TimeListener) {
        if (timeListeners.size == 0) {
            timeListeners.add(listener)
            addThread(timeThread)
        }
    }

    fun timingSwitch(on: Boolean) {
        timing = on
        if (on) {
//            threadPoolExecutor.remove(timeThread)
            threadPoolExecutor.execute(timeThread)
        }else{
            timeListeners.clear()
        }
    }

    private var timing = true
    private val timeListeners: CopyOnWriteArrayList<TimeListener> = CopyOnWriteArrayList()

    private val timeThread = Runnable {
        while (timing) {
            Thread.sleep(1000)
            Log.e(TAG, "time clock")
            for (listener in timeListeners) {
                listener.onClock()
            }
        }
    }

    interface TimeListener {
        /**
         * 每秒钟调用一次
         */
        fun onClock()
    }

}