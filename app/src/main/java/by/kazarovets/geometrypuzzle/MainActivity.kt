package by.kazarovets.geometrypuzzle

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var renderer: OpenGLRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!supportES2()) {
            Toast.makeText(this, "OpenGL ES 2.0 is not supported", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        setContentView(R.layout.activity_main)

        glSurface.setEGLContextClientVersion(2)
        renderer = OpenGLRenderer(this)
        glSurface.setRenderer(renderer)

        fromX.setOnClickListener { renderer.pointCamera(CameraPoint.oyz(3f)) }
        fromY.setOnClickListener { renderer.pointCamera(CameraPoint.ozx(3f)) }
        fromZ.setOnClickListener { renderer.pointCamera(CameraPoint.oxy(3f)) }
    }

    override fun onPause() {
        super.onPause()
        glSurface.onPause()
    }

    override fun onResume() {
        super.onResume()
        glSurface.onResume()
    }

    private fun supportES2(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        return configurationInfo.reqGlEsVersion >= 0x20000
    }

}
