package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

//class Tile(var num: Int = 0)

var drawMul = 5
var isTouching = false
//const val playerCoordinateToMapCoordinateMultiplier: Int = 80
const val angleDifference = 0.011

fun manipulateColor(color: Int, factor: Float): Int {
    val a = Color.alpha(color)
    val r = (Color.red(color) * factor).roundToInt()
    val g = (Color.green(color) * factor).roundToInt()
    val b = (Color.blue(color) * factor).roundToInt()
    return Color.argb(
        a,
        r.coerceAtMost(255),
        g.coerceAtMost(255),
        b.coerceAtMost(255)
    )
}

class Game(
    var playerX: Int = 20,
    var playerY: Int = 20,
    var playerHeight: Int = 14,
    var playerWidth: Int = 14,

    var playerAngle: Double = 0.0,
    //val lab: Array<Array<Tile>>,
    public val height: Int = 11,
    public val width: Int  = 11,
    public val myMap: Array<Int> = //Array<Int>((height * width))
        arrayOf(
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1,
            1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1,
            1, 0, 1, 1, 1, 1, 0, 1, 0, 1, 1,
            1, 0, 0, 0, 0, 1, 0, 1, 0, 1, 1,
            1, 0, 0, 0, 0, 1, 0, 1, 0, 1, 1,
            1, 0, 0, 0, 0, 1, 0, 1, 0, 1, 1,
            1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1,
            1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1,
            1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
    ),

    private var raySizes:   Array<Int>    = Array<Int>(100) { 0 },
    private var encounterIsY: Array<Boolean> = Array<Boolean>(100) { false },
    private var xEncounter: Array<Float>  = Array<Float>(100) { 0f },
    private var yEncounter: Array<Float>  = Array<Float>(100) { 0f },
    //private val midAngle: Double = angleDifference * raySizes.size,
    public var squareSize: Float = 20f
) {
    fun drawRays(canvas: Canvas, paint: Paint) {
        paint.color = Color.RED

        for(i in 0..raySizes.size-1) {
            val x = (playerX + playerWidth / 2) * drawMul
            val y = (playerY + playerHeight / 2) * drawMul
            val rSize = raySizes[i] * drawMul

            val startX = x.toFloat()
            val startY = y.toFloat()
            val stopX = x.toFloat() + rSize * cos(playerAngle + angleDifference * i).toFloat()
            val stopY = y.toFloat() + rSize * sin(playerAngle + angleDifference * i).toFloat()

            paint.strokeWidth = 3f
            canvas.drawLine(startX, startY, stopX, stopY, paint)
        }
    }

    fun castRays() {
        for(i in 0..<raySizes.size) {
            var index = 0
            var colliding = false

            var x: Float = (game.playerX + game.playerWidth / 2).toFloat()
            var y: Float = (game.playerY + game.playerHeight / 2).toFloat()

            while (!colliding) {
                x += 1 * cos(playerAngle + angleDifference * i).toFloat()
                y += 1 * sin(playerAngle + angleDifference * i).toFloat()

                val xMap: Int = (x / squareSize).toInt()
                val yMap: Int = (y / squareSize).toInt()

                if (!(yMap < 0 || yMap > height || xMap < 0 || xMap > width)) {
                    if (myMap[yMap * width + xMap] == 1) {
                        colliding = true
                    }
                }

                if (index > 150) {
                    colliding = true
                }

                index++
            }
            xEncounter[i] = x
            yEncounter[i] = y
            var tmpx = xEncounter[i] % squareSize
            var tmpy = yEncounter[i] % squareSize
            tmpy = (squareSize / 2) - abs(tmpy - squareSize / 2)
            tmpx = (squareSize / 2) - abs(tmpx - squareSize / 2)

            encounterIsY[i] = tmpy < tmpx

            raySizes[i] = (index * 1 / cos(angleDifference * i - angleDifference * 25)).toInt()
            //raySizes[i] = index * 1
        }
    }

    fun drawPlayer(canvas: Canvas, paint: Paint) {
        paint.color = Color.BLACK
        val startX: Float = (playerX * drawMul).toFloat()
        val startY: Float = (playerY * drawMul).toFloat()
        //val start_x: Float = 0f
        //val start_y: Float = 0f
        val startW: Float = (playerWidth * drawMul).toFloat()
        val startH: Float = (playerHeight * drawMul).toFloat()
        //val start_w: Float = 50f
        //val start_h: Float = 50f
        canvas.drawRect(
            startX,
            startY,
            startW + (startX),
            startH + (startY),
            paint)
    }

    fun drawLab3D(canvas: Canvas, paint: Paint) {
        paint.color = Color.BLUE
        val offsetX = 0f
        val offsetY = 0f

        for(i in 0..<raySizes.size)  {
            //var size = ray_sizes[i] * cos(playerAngle + angleDifference * ray_sizes.size / 2)
            val size = raySizes[i]
            val wallSize = (squareSize * 6 - size) * 9
            val maxSize = (squareSize * 6) * 9

            val startY = offsetY + 20 * i
            val startX = offsetX + maxSize / 2 - wallSize / 2
            val stopY = offsetY + 20 * (i + 1)
            val stopX = startX + wallSize
            var myDivider = 4


            if(size < squareSize * 6) {
                fun drawWall(x1: Float, y1: Float, x2: Float, y2: Float, c1: Int, c2: Int, doDarken: Boolean) {
                    var nc1 = c1
                    var nc2 = c2

                    val pxSize: Float = 25f
                    var j = 0
                    if(encounterIsY[i] && doDarken) {
                        nc1 = manipulateColor(c1, 0.8f)
                        nc2 = manipulateColor(c2, 0.8f)
                    }

                    if(((xEncounter[i] / myDivider).toInt() % 2 == 0).xor(
                            (yEncounter[i] / myDivider).toInt() % 2 == 0)) {
                        paint.color = nc1
                    } else {
                        paint.color = nc2
                    }

                    canvas.drawRect(x1, y1, x2, y2, paint)
                }

                myDivider = 4
                drawWall(startX, startY, stopX, stopY,
                    Color.argb(255, 0, 127, 255), Color.argb(255, 255, 255, 0), true)
                drawWall(stopX, startY, 1024F, stopY,
                    Color.argb(255, 255, 0, 255), Color.argb(255, 255, 0, 255), false)
                myDivider = 2
                drawWall(offsetX, startY, startX, stopY,
                    Color.argb(255, 0, 0, 255), Color.argb(255, 0, 0, 255), false)
                
            } else {
                paint.color = Color.BLUE
                canvas.drawRect(offsetX, startY, (offsetX + 1024) / 2, stopY, paint)
                paint.color = Color.argb(255, 255, 0, 255)
                canvas.drawRect((offsetX + 1024) / 2, startY, offsetX + 1024, stopY, paint)
            }
        }
    }
    fun drawLab(canvas: Canvas, paint: Paint) {
        paint.color = Color.BLACK

        for(y in 0..<height) {
            for(x in 0..<width) {
                if(myMap[y * width + x] == 0) {
                    paint.color = Color.BLUE
                } else {
                    paint.color = Color.YELLOW
                }
                canvas.drawRect(
                    squareSize * drawMul * x,
                    squareSize * drawMul * y,
                    squareSize * drawMul + (x * squareSize * drawMul) - 3,
                    squareSize * drawMul + (y * squareSize * drawMul) - 3,
                    paint)
            }
        }
    }
}

var game: Game = Game()

class DrawView(context: Context?) : View(context) {
    private var paint = Paint()
    public override fun onDraw(canvas: Canvas) {
        for(i in 0..10) {
            paint.color = Color.BLACK
            //val width = width.toFloat()
            //var height = height.toFloat()
            paint.strokeWidth = 1f

            //game.drawLab(canvas, paint)
            //game.drawRays(canvas, paint)
            //game.drawPlayer(canvas, paint)
            game.drawLab3D(canvas, paint)

            paint.strokeWidth = 0f
            paint.color = Color.CYAN
            paint.color = Color.YELLOW

        }
    }
}

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var gyroscopeSensor: Sensor? = null
    private var x: Float = 0f
    private var y: Float = 0f
    private var z: Float = 0f

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val drawView = DrawView(this)
        drawView.setBackgroundColor(Color.WHITE)
        setContentView(drawView)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val handler = Handler()

        class MyRunnable(handler: Handler, i: Int, dw: DrawView) :
            Runnable {
            private val handler: Handler
            private var i: Int
            private val dw: DrawView

            init {
                this.handler = handler
                this.i = i
                this.dw = dw
            }

            override fun run() {
                //game.playerY = 40
                this.handler.postDelayed(this, 20)
                //game.playerAngle += 0.1f
                game.playerAngle -= x/15

                if(isTouching) {
                    Log.d("z", z.toString())

                    val oldX = game.playerX
                    val oldY = game.playerY

                    game.playerX += (cos(game.playerAngle + angleDifference * 25) * 2).toInt()
                    game.playerY += (sin(game.playerAngle + angleDifference * 25) * 2).toInt()

                    val minx = (game.playerX / game.squareSize).toInt()
                    val miny = (game.playerY / game.squareSize).toInt()
                    val maxx = ((game.playerX + game.playerWidth) / game.squareSize).toInt()
                    val maxy = ((game.playerY + game.playerHeight) / game.squareSize).toInt()

                    if(game.myMap[miny * game.width + minx] == 1 ||
                        game.myMap[maxy * game.height + maxx] == 1
                        ) {
                        game.playerX = oldX
                        game.playerY = oldY
                    }
                }
                if(game.playerX < 0) {
                    game.playerX = 0
                }
                if(game.playerY < 0) {
                    game.playerY = 0
                }

                game.castRays()
                drawView.invalidate()
            }
        }
        handler.post(MyRunnable(handler, 0, drawView))
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        gyroscopeSensor?.also { gyro ->
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        //var x = event.getX()
        //var y = event.getY()
        val eventAction = event.getAction()
        if (eventAction == MotionEvent.ACTION_UP) {
            isTouching = false
        }
        if (eventAction == MotionEvent.ACTION_DOWN) {
            isTouching = true
        }

        return true
    }


    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            x = it.values[0]
            y = it.values[1]
            z = it.values[2]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}
