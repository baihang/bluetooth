package com.example.healthy

import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.content.res.Resources
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.opengl.Matrix
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.healthy.utils.Triangle_1
import java.io.InputStream
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.charset.Charset
import java.util.Arrays
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraActivity : AppCompatActivity() {

    val TAG = "CameraActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        checkPermission()

        val surface = findViewById<GLSurfaceView>(R.id.camera_surface)

        initGLSurface(surface)
/*
        val cameraProvider = ProcessCameraProvider.getInstance(this)
        cameraProvider.addListener(Runnable {
            val camera = cameraProvider.get()
            val preview = Preview.Builder().build()
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
            preview.setSurfaceProvider(Preview.SurfaceProvider { it ->
                it.provideSurface(
                    surface.holder.surface,
                    ContextCompat.getMainExecutor(this)
                ) { result -> Log.e(TAG, "camera surface result = $result") }
            })
            camera.bindToLifecycle(this, cameraSelector, preview)
        }, ContextCompat.getMainExecutor(this))

 */
    }

    private var program: Int = 0
    private val COORDS_PER_VERTEX = 2

    val render = object : GLSurfaceView.Renderer {
        lateinit var triangle: Triangle_1
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            GLES20.glClearColor(0f, 0f, 0f, 1f)
            initProgram()
//            triangle = Triangle(program)
            triangle = Triangle_1(program)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            val ratio: Float = width.toFloat() / height.toFloat()
            Matrix.frustumM(Triangle_1.mProjMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 10f)
            Matrix.setLookAtM(
                Triangle_1.mVMatrix, 0, 0f,0f,3f,
                0f,0f,0f,0f,1.0f,0.0f
            )
        }

        override fun onDrawFrame(gl: GL10?) {
            //清楚顶点和颜色缓冲
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT.or(GLES20.GL_COLOR_BUFFER_BIT))

            //指定shader
            GLES20.glUseProgram(program)
            triangle.drawSelf()

        }


    }

    private fun checkGlError(tag: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "$tag open gl error $error")
        }
    }

    private val bytes = ByteArray(1024 * 1024)

    private fun initGLSurface(surface: GLSurfaceView) {
        surface.setEGLContextClientVersion(2)

        surface.setRenderer(render)
        surface.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    private fun initProgram(){
        var status = IntArray(1)

        val vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        val str = loadShadeFromFile(R.raw.vertex_shader)
        GLES20.glShaderSource(vShader, str)
        GLES20.glCompileShader(vShader)
        GLES20.glGetShaderiv(vShader, GLES20.GL_COMPILE_STATUS, status, 0)
        if(status[0] == 0){
            Log.e(TAG, "vShader = $vShader error ${status[0]} str = $str")
            throw Exception("GL_VERTEX_SHADER error")
        }

        val fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        val fStr = loadShadeFromFile(R.raw.fragment_shader)
        GLES20.glShaderSource(fShader, fStr)
        GLES20.glCompileShader(fShader)
        GLES20.glGetShaderiv(fShader, GLES20.GL_COMPILE_STATUS, status, 0)
        if(status[0] == 0){
            Log.e(TAG, "fShader = $fShader error ${status[0]} fstr = $fStr")
            throw Exception("GL_FRAGMENT_SHADER error")
        }

        program = GLES20.glCreateProgram()
        if(program == 0){
            Log.e(TAG, "glCreateProgram error $program")
            throw Exception("glCreateProgram error")
        }
        GLES20.glAttachShader(program, vShader)
        GLES20.glAttachShader(program, fShader)
        GLES20.glLinkProgram(program)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
        if(status[0] != GLES20.GL_TRUE){
            Log.e(TAG, "glLinkProgram error ${status[0]} vShader = $vShader fShader = $fShader")
        }
    }

    private fun loadShadeFromFile(id: Int): String {
        var fFile: InputStream? = null
        val sb = StringBuilder()

        try {
            fFile = resources.openRawResource(id)
            while (true) {
                val size = fFile.read(bytes)
                if (size <= 0) {
                    break
                }
                sb.append(String(bytes, 0, size))
            }
        } catch (e: Resources.NotFoundException) {

        } finally {
            fFile?.close()
        }

        return sb.toString()
    }


    private fun checkPermission() {
        val permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 0)
        }
    }

    override fun onStart() {
        super.onStart()

    }
}