package com.anwesh.uiprojects.ladderdecreasingview

/**
 * Created by anweshmishra on 23/08/18.
 */

import android.app.Activity
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
    val origY : Float = h - (hGap * i + hGap / 2)
    val yStep : Float = origY + (h * 1.1f - origY) * scale
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

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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

    data class DecreasingLadder(var i : Int) {
        private var curr : LDNode = LDNode(0)
        private var dir : Int = 1

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }
    }

    data class Renderer(var view : LadderDecreasingView) {

        private val animator : Animator = Animator(view)
        private val dl : DecreasingLadder = DecreasingLadder(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            dl.draw(canvas, paint)
            animator.animate {
                dl.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            dl.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : LadderDecreasingView {
            val view : LadderDecreasingView = LadderDecreasingView(activity)
            activity.setContentView(view)
            return view
        }
    }
}