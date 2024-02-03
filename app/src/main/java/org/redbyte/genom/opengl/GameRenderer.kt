package org.redbyte.genom.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GameRenderer(context: Context) : GLSurfaceView.Renderer {
    private val gameBoard: GameBoard
    private val cellVertexBuffer: FloatBuffer
    private var program: Int = 0
    private var positionHandle: Int = 0
    private var colorHandle: Int = 0
    private val cellSize = 8
    private var matrixHandle: Int = 0
    private var viewMatrixHandle: Int = 0
    private var xHandle = 0
    private var yHandle = 0

    init {
        Log.d("_debug", "init: ");
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        Log.d("_debug", "$screenWidth, $screenHeight: ");
        val cellCountWidth = screenWidth / cellSize
        val cellCountHeight = screenHeight / cellSize

        gameBoard = GameBoard(cellCountWidth, cellCountHeight)

        val cellVertices = floatArrayOf(
            -cellSize / 2.0f, -cellSize / 2.0f,
            cellSize / 2.0f, -cellSize / 2.0f,
            -cellSize / 2.0f, cellSize / 2.0f,
            cellSize / 2.0f, cellSize / 2.0f
        )

        val buffer = ByteBuffer.allocateDirect(cellVertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(cellVertices)
        buffer.position(0)
        cellVertexBuffer = buffer
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d("_debug", "onSurfaceCreated: ");
        GLES20.glClearColor(0.5f, 0.0f, 0.5f, 1.0f)

        val vertexShaderCode = """
        attribute vec4 a_position;
        uniform mat4 u_matrix;
        uniform mat4 u_viewMatrix;
        void main() {
            gl_Position = u_matrix * u_viewMatrix * a_position;
        }

    """.trimIndent()

        val fragmentShaderCode = """
        precision mediump float;
        uniform vec4 u_color;
        void main() {
            gl_FragColor = vec4(0.0, 1.0, 0.0, 1.0);
        }
    """.trimIndent()

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        if (vertexShader == 0) {
            Log.e("_debug", "Failed to compile vertex shader")
            return
        }
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        GLES20.glUseProgram(program)

        viewMatrixHandle = GLES20.glGetUniformLocation(program, "u_viewMatrix")
        matrixHandle = GLES20.glGetUniformLocation(program, "u_matrix")
        positionHandle = GLES20.glGetAttribLocation(program, "a_position")
        colorHandle = GLES20.glGetUniformLocation(program, "u_color")
        xHandle = GLES20.glGetAttribLocation(program, "a_x")
        yHandle = GLES20.glGetAttribLocation(program, "a_y")

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(xHandle)
        GLES20.glEnableVertexAttribArray(yHandle)
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            Log.e("_debug", "onSurfaceCreated OpenGL Error: $error")
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d("_debug", "onDrawFrame: ")
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        gameBoard.update()
        for (i in 0 until gameBoard.height) {
            for (j in 0 until gameBoard.width) {
                if (gameBoard.cells[i][j]) {
                    val x = j * cellSize
                    val y = i * cellSize

                    val matrix = FloatArray(16)
                    Matrix.setIdentityM(matrix, 0)
                    Matrix.translateM(matrix, 0, x.toFloat(), y.toFloat(), 0.0f)
                    GLES20.glUniformMatrix4fv(matrixHandle, 1, false, matrix, 0)

                    GLES20.glVertexAttribPointer(
                        positionHandle,
                        2,
                        GLES20.GL_FLOAT,
                        false,
                        2 * 4,
                        cellVertexBuffer
                    )

                    GLES20.glEnableVertexAttribArray(positionHandle)

                    GLES20.glUniform4f(colorHandle, 1.0f, 0.0f, 0.0f, 1.0f)
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
                }
            }
        }
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("_debug", "onSurfaceChanged: ");
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        val projectionMatrix = FloatArray(16)
        val viewMatrix = FloatArray(16)

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -5f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, projectionMatrix, 0)
        GLES20.glUniformMatrix4fv(viewMatrixHandle, 1, false, viewMatrix, 0)

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -5f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            Log.e("_debug", "onSurfaceChanged OpenGL Error: $error")
        }
    }


    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
}