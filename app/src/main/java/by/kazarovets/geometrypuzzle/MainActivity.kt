package by.kazarovets.geometrypuzzle

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.kazarovets.geometrypuzzle.rendering.OpenGLRenderer
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

        glSurface.setOnTouchListener(object : OnSwipeTouchListener(this@MainActivity) {

            override fun onSwipeBottom() {
                renderer.swipeCamera(SwipeDirection.BOTTOM)
            }

            override fun onSwipeLeft() {
                renderer.swipeCamera(SwipeDirection.LEFT)
            }

            override fun onSwipeRight() {
                renderer.swipeCamera(SwipeDirection.RIGHT)
            }

            override fun onSwipeTop() {
                renderer.swipeCamera(SwipeDirection.UP)
            }
        })
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
