package com.harrysyred.connect4

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.google.android.material.snackbar.Snackbar
import com.harrysyred.logic.ConnectLogic


class GameView: View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
    private var gameWon: Boolean = false
    private var circleDiameter: Float = 0f
    private var circleSpacing: Float = 0f
    private var collumnWidth: Float = 0f
    private var circleSpacingRatio: Float = 0.2f
    private var gameWidth: Float = 0.0f
    private var gameHeight: Float = 0.0f
    private var gameWidthOffset: Float = 0.0f
    private var gameHeightOffset: Float = 0.0f
    private val gridPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLUE
    }
    private val noPlayerPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val player1Paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.RED
    }
    private val player2Paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.YELLOW
    }
    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }
    var game: ConnectLogic = ConnectLogic()
    set(value) {
        field = value
        // After the new value is set, make sure to recalculate sizes and then trigger a redraw
        onSizeChanged(width, height, width, height)
        invalidate()
    }
    private val colCount:Int get() = game.columns
    private val rowCount:Int get() = game.rows
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        var prev = circleDiameter
        var tempDiameter_Width: Float = (w / colCount+1).toFloat() // the temporary diameter of the circles (cirlce+padding) based on the width
        var tempDiameter_Height: Float = (h / rowCount+1).toFloat() // the temporary diameter of the circles (cirlce+padding) based on the height
        collumnWidth = minOf(tempDiameter_Height,tempDiameter_Width)
        circleDiameter = collumnWidth*(1-circleSpacingRatio)
        circleSpacing = collumnWidth*circleSpacingRatio
        gameWidth = colCount * collumnWidth + circleSpacing
        gameHeight = rowCount * collumnWidth + circleSpacing
        gameWidthOffset = (w - gameWidth)/2
        gameHeightOffset = (h - gameHeight)/2
        if(prev != circleDiameter)
            invalidate()
        //super.onSizeChanged(w, h, oldw, oldh)
    }
    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(gameWidthOffset,gameHeightOffset,gameWidth+gameWidthOffset, gameHeight+gameHeightOffset, gridPaint)
        for (row in 0 until rowCount) for (col in 0 until colCount){ val paint = when (game.getToken(col,rowCount-(row+1))) {
                    1 -> player1Paint
                    2 -> player2Paint
                    else -> noPlayerPaint
                }
                val cx = circleSpacing + col*(circleDiameter+circleSpacing)+(circleDiameter/2)+gameWidthOffset
                val cy = circleSpacing + row*(circleDiameter+circleSpacing)+(circleDiameter/2)+gameHeightOffset
                canvas.drawCircle(cx,cy,circleDiameter/2,paint)
            }
            if(gameWon) {
                textPaint.textSize = canvas.width / 7f;
                textPaint.textAlign = Paint.Align.CENTER
                if (game.playerTurn == 1)
                    canvas.drawText(
                        "Player Red Won",
                        canvas.width / 2f,
                        textPaint.textSize,
                        textPaint
                    )
                else if (game.playerTurn == 2)
                    canvas.drawText(
                        "Player Yellow",
                        canvas.width / 2f,
                        textPaint.textSize,
                        textPaint
                    )
                else
                    canvas.drawText(
                        "Player " + game.playerTurn + " Won",
                        canvas.width / 2f,
                        textPaint.textSize,
                        textPaint
                    )
            }
        super.onDraw(canvas)
    }
    private val gestureDetector = GestureDetectorCompat( context, object:
        GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean{
            return true
        }
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // ToDo calculate the touch coordinates and handle it
            if(gameWon){
                gameWon = false
                game.reset()
                invalidate()
                return true
            }else {
                var x = e.x - gameWidthOffset
                var col: Int = Math.round((x - (collumnWidth / 2)) / collumnWidth)
                if ((col < colCount) && (col >= 0)) {
                    if((game.playToken(col, game.playerTurn))) {
                        if (checkIfWon(
                                game.playerTurn
                            )
                        ) {
                            Log.d("game", game.playerTurn.toString() + " won")
                            gameWon = true

                            val mySnackbar = Snackbar.make(findViewById(R.id.gameView), "Press anywhere to restart game", 4000)
                            mySnackbar.show()
                        } else {
                            Log.d("game","game still active")
                            game.updatePlayer()
                        }
                        invalidate()
                    }else{
                        Log.d("game","Failed to place")
                    }
                }
            }
            return super.onSingleTapUp(e)
        }
    });
    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d("Gesture","Touch Detected")
        return gestureDetector.onTouchEvent(event)||super.onTouchEvent(event)
    }
    fun checkIfWon(player:Int):Boolean {
        var target = game.amountToConnect
        var arraySize = Pair(game.columns,game.rows)
        var current = 0
        for (c in 0 until arraySize.first-1){
            for (r in 0 until arraySize.second-1){
                //Log.d("game-checking-hor",(c).toString()+" : "+(r).toString())
                if(game.getToken(c,r) == player)
                    current+=1
                else
                    current = 0
                if(current == target)
                    return true
            }
            current = 0
        }
        for (r in 0 until arraySize.second-1){
            for (c in 0 until arraySize.first-1){
                //Log.d("game-checking-vir",(c).toString()+" : "+(r).toString())
                if(game.getToken(c,r) == player)
                    current+=1
                else
                    current = 0
                if(current == target)
                    return true
            }
            current = 0
        }
        for (r in 0 until arraySize.second-1){
            for (c in 0 until arraySize.first){
                if(game.getToken(c,(r-c)) == player) {
                    current += 1
                }
                else {
                    current = 0
                }
                if(current == target)
                    return true
            }
            current = 0
        }
        for (r:Int in 0 until arraySize.second-1){
            for (c2:Int in 0 until arraySize.first){
                var c:Int = (arraySize.first-1)-c2
                if(game.getToken(c,(r-c2)) == player) {
                    current += 1
                    Log.d("game-checking-diag","Checking Diaganal\nCoordinates: "+(c).toString()+" : "+(r-c2).toString()+ "\nValue: same\nCurrent: " + current.toString())
                }
                else {
                    Log.d("game-checking-diag","Checking Diaganal\nCoordinates: "+(c).toString()+" : "+(r-c2).toString()+ "\nValue: Different\nCurrent: " + current.toString())
                    current = 0
                }
                if(current == target)
                    return true
            }
            current = 0
        }
        return false

    }
}
