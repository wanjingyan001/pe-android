package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.sogukj.pe.R

class ProjectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)
    }

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, UserActivity::class.java))
        }
    }
}
