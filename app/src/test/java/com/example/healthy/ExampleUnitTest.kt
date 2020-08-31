package com.example.healthy

import com.example.healthy.data.DataAnalyze
import com.example.healthy.data.HeartOneData
import com.example.healthy.data.HeartThreeData
import org.junit.Test

import org.junit.Assert.*
import kotlin.experimental.and

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testHeartData() {
        val heart = HeartOneData()
        for (i in heart.bodyData.indices) {
            heart.bodyData[i] = i.toShort()
        }
        val result = heart.getData()
        assertEquals(result[0].size, 5)
    }

    @Test
    fun testHeartThree() {
        val three = HeartThreeData()
        for (i in three.bodyData.indices) {
            three.bodyData[i] = i.toShort()
        }
        val result = three.getData()
        assertEquals(result[0].size, 2)
    }

    @Test
    fun testParseData() {
        val array: ByteArray = byteArrayOf(
            (170 and 0xff).toByte(),
            (0xAB.toShort() and 0xff).toByte(),
            0x07, 0x0D,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0
        )
        val dataParse = DataAnalyze()
        val result = dataParse.parseData(array)
        assertEquals(result?.get(1)?.get(0), 515)
    }
}