package com.anwesh.uiprojects.ladderdecreasingview

/**
 * Created by anweshmishra on 23/08/18.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.content.Context
import android.graphics.Color
import android.graphics.RectF

val nodes : Int = 5

fun Canvas.drawLDNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val hGap : Float = h / nodes
    val yStep : Float = hGap * i + (-h * 0.1f - hGap * i) * scale
    paint.color = Color.parseColor("#03A9F4")
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(w / 2, 0f)
    paint.strokeWidth = Math.min(w, h) / 30
    drawLine(-hGap / 2, yStep, hGap/2, yStep, paint)
    paint.strokeWidth = Math.min(w, h) / 60
    for (j in 0..1) {
        save()
        translate(-hGap / 2 + hGap * j, hGap * i)
        drawLine(0f, 0f, 0f,  hGap * (1 - scale), paint)
        restore()
    }
    restore()
}

class LadderDecreasingView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += 0.1f * this.dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LDNode(var i : Int, val state : State = State()) {
        var next : LDNode? = null
        var prev : LDNode? = null
        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = LDNode(i + 1)
                next?.prev = this
            }
        }
        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawLDNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit)   {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LDNode {
            var curr : LDNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }
}