package com.sogukj.pe.ui.IM.newlocation.activity

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil.setContentView
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.framework.base.BaseActivity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.ui.IM.location.activity.LocationExtras
import kotlinx.android.synthetic.main.activity_location_message.*

class LocationMessageActivity : BaseActivity() {
    private var msgLatitude = -1.0
    private var msgLongitude = -1.0
    private var msgAddress = ""
    private lateinit var map: AMap

    companion object {
        fun start(context: Context, latitude: Double, longitude: Double, address: String) {
            val intent = Intent(context, LocationMessageActivity::class.java)
            intent.putExtra(LocationExtras.LATITUDE, latitude)
            intent.putExtra(LocationExtras.LONGITUDE, longitude)
            intent.putExtra(LocationExtras.ADDRESS, address)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_message)
        msgLatitude = intent.getDoubleExtra(LocationExtras.LATITUDE, -1.0)
        msgLongitude = intent.getDoubleExtra(LocationExtras.LONGITUDE, -1.0)
        msgAddress = intent.getStringExtra(LocationExtras.ADDRESS)
        initMap()
    }

    private fun initMap() {
        map = mAddressMap.map
        val uiSettings = map.uiSettings
        uiSettings.isZoomControlsEnabled = false
        uiSettings.isMyLocationButtonEnabled = false
        uiSettings.logoPosition = AMapOptions.LOGO_POSITION_BOTTOM_RIGHT

        val options = MarkerOptions()
        options.position(LatLng(msgLatitude, msgLongitude))
                .title(msgAddress)
                .draggable(false)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_blue_point))

    }
}
