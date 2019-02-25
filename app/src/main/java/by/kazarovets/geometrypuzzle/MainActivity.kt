package by.kazarovets.geometrypuzzle

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
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
        renderer = OpenGLRenderer()
        glSurface.setRenderer(renderer)

        addTouchListeners()

    }

    private fun addTouchListeners() {

        val onClick: (MotionEvent) -> Unit = {
            if (it.x < (glSurface.width / 2) - glSurface.x) {
                renderer.tryMoveLeft()
            } else {
                renderer.tryMoveRight()
            }
        }

        val onSwipeTouchListener = object : OnSwipeTouchListener(onClick, this@MainActivity) {

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
        }

        glSurface.setOnTouchListener(onSwipeTouchListener)
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


    inner class SingleTapListener(val onClick: (MotionEvent) -> Unit) :
        GestureDetector.SimpleOnGestureListener() {

    }

}
