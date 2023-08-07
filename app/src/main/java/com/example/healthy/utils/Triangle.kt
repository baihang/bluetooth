package com.example.healthy.utils

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Triangle(program: Int) {

    val vertexCount = 3
    lateinit var vertexBuffer: FloatBuffer
    lateinit var colorBuffer: FloatBuffer
    val mMatrix = FloatArray(16)

    companion object {
        val projMatrix = FloatArray(16) //投影用 4 * 4 矩阵
        val cameraMatrix = FloatArray(16) //相机位置朝向
    }

    var angle = 0f
    var positionHandle: Int = -1
    var colorHandle: Int = -1
    var matrixHandle: Int = -1

    init {
        //完成顶点坐标初始化
        val UNIT_SIZE = 0.2f
        val vertices: FloatArray = floatArrayOf(
            -4f * UNIT_SIZE, 0f, 0f,
            0f, -4f * UNIT_SIZE, 0f,
            4 * UNIT_SIZE, 0f, 0f
        )
        val byteBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        vertexBuffer = byteBuffer.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)

        //颜色初始化
        val colors = floatArrayOf(
            1f, 1f, 1f, 0f,
            0f, 0f, 1f, 0f,
            0f, 1f, 0f, 0f
        )
        val colorByteBuffer = ByteBuffer.allocateDirect(colors.size * 4)
        colorByteBuffer.order(ByteOrder.nativeOrder())
        colorBuffer = colorByteBuffer.asFloatBuffer()
        colorBuffer.put(colors)
        colorBuffer.position(0)

        //
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        colorHandle = GLES20.glGetAttribLocation(program, "aColor")
        matrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
    }

    fun drawSelf() {
        Matrix.setRotateM(mMatrix, 0, 0f, 0f, 1f, 0f)
        Matrix.translateM(mMatrix, 0, 0f, 0f, 1f)
        Matrix.rotateM(mMatrix, 0, angle, 1f, 0f, 0f)

        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, getFinalMatrix(mMatrix), 0)
        //顶点数据
        GLES20.glVertexAttribPointer(
            positionHandle, 3, GLES20.GL_FLOAT,
            false, 3 * 4, vertexBuffer
        )

        GLES20.glVertexAttribPointer(
            colorHandle, 4, GLES20.GL_FLOAT,
            false, 4 * 4, colorBuffer
        )
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(colorHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

    }

    private fun getFinalMatrix(rhs: FloatArray): FloatArray {
        val matrix = FloatArray(16)
        Matrix.multiplyMM(matrix, 0, cameraMatrix, 0, rhs, 0)
        Matrix.multiplyMM(matrix, 0, projMatrix, 0, matrix, 0)
        return matrix
    }

}