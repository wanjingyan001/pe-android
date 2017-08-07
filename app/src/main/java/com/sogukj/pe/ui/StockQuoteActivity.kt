package com.sogukj.pe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.framework.base.ToolbarActivity
import com.sogukj.pe.R

class StockQuoteActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_quote)
        setBack(true)
        setTitle("股票行情")
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, StockQuoteActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
