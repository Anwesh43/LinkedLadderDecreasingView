package com.anwesh.uiprojects.linkedladderdecreasingview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.ladderdecreasingview.LadderDecreasingView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LadderDecreasingView.create(this)
    }
}
