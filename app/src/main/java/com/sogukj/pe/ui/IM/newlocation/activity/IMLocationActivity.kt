package com.sogukj.pe.ui.IM.newlocation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.AMap
import com.amap.api.maps.model.CameraPosition
import com.framework.base.BaseActivity
import com.sogukj.pe.R
import kotlinx.android.synthetic.main.activity_imlocation.*
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.netease.nim.uikit.api.model.location.LocationProvider
import com.sogukj.pe.ui.IM.location.activity.LocationExtras
import com.sogukj.pe.util.Utils
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info


class IMLocationActivity : BaseActivity(), AMap.OnCameraChangeListener, AMapLocationListener, LocationSource, View.OnClickListener {
    private lateinit var map: AMap
    //声明AMapLocationClient对象
    private lateinit var mLocationClient: AMapLocationClient
    //声明AMapLocationClientOption对象
    private lateinit var mLocationOption: AMapLocationClientOption
    //定位蓝点
    private lateinit var myLocationStyle: MyLocationStyle
    private var cacheLatitude = -1.0
    private var cacheLongitude = -1.0

    companion object {
        lateinit var callback: LocationProvider.Callback
        fun start(context: Context, callback: LocationProvider.Callback) {
            this.callback = callback
            context.startActivity(Intent(context, IMLocationActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imlocation)
        Utils.setWindowStatusBarColor(this, R.color.white)
        imMap.onCreate(savedInstanceState)
        initMap()
        initLocation()
        locationBtn.setOnClickListener(this)
    }

    private fun initMap() {
        map = imMap.map
        map.setOnCameraChangeListener(this)
        map.setLocationSource(this)
        map.isMyLocationEnabled = true
        val uiSettings = map.uiSettings
        uiSettings.isZoomControlsEnabled = false
        uiSettings.isMyLocationButtonEnabled = false
        uiSettings.logoPosition = AMapOptions.LOGO_POSITION_BOTTOM_RIGHT
    }

    private fun initLocationStyle(){
        myLocationStyle = MyLocationStyle()
        myLocationStyle.interval(2000)
        myLocationStyle.showMyLocation(true)
//        myLocationStyle.myLocationIcon()
        map.myLocationStyle = myLocationStyle
        map.isMyLocationEnabled = true

    }

    private fun initLocation() {
        mLocationClient = AMapLocationClient(application)
        mLocationClient.setLocationListener(this)
        mLocationOption = AMapLocationClientOption()
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        //单次定位
        mLocationOption.isOnceLocation = true
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.isNeedAddress = true
        //设置是否允许模拟位置,默认为true，允许模拟位置
        mLocationOption.isMockEnable = false
        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.httpTimeOut = 20000
        mLocationClient.setLocationOption(mLocationOption)
        //启动定位
        mLocationClient.startLocation()
    }


    override fun onResume() {
        super.onResume()
        imMap.onResume()
    }

    override fun onPause() {
        super.onPause()
        imMap.onPause()
    }

    override fun onStop() {
        super.onStop()
        mLocationClient.stopLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        imMap.onDestroy()
        mLocationClient.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.locationBtn -> {
                val latlng = LatLng(cacheLatitude, cacheLongitude)
                val camera = CameraUpdateFactory.newCameraPosition(CameraPosition(latlng, map.cameraPosition.zoom, 0f, 0f))
                map.animateCamera(camera)
            }
        }
    }


    override fun onCameraChangeFinish(p0: CameraPosition?) {
    }

    override fun onCameraChange(p0: CameraPosition?) {
    }

    override fun deactivate() {
    }

    override fun activate(p0: LocationSource.OnLocationChangedListener?) {

    }


    /**
     * 获取定位结果
     */
    override fun onLocationChanged(location: AMapLocation?) {
        if (location != null) {
            if (location.errorCode == 0) {
                info {
                    "定位结果来源:${location.locationType}\n" +
                            "经度:${location.longitude}\n" +
                            "纬度:${location.latitude}\n" +
                            "精度信息:${location.accuracy}\n" +
                            "地址:${location.address}\n" +
                            "国家:${location.country}\n" +
                            "省:${location.province}\n" +
                            "城市:${location.city}\n" +
                            "AOI信息:${location.aoiName}\n" +
                            "建筑物Id:${location.buildingId}\n" +
                            "楼层:${location.floor}\n" +
                            "GPS的当前状态:${location.gpsAccuracyStatus}"

                }
                cacheLatitude = location.latitude
                cacheLongitude = location.longitude

                val intent = intent
                val zoomLevel = intent.getIntExtra(LocationExtras.ZOOM_LEVEL, LocationExtras.DEFAULT_ZOOM_LEVEL).toFloat()
                val latlng = LatLng(location.latitude, location.longitude)
                val camera = CameraUpdateFactory.newCameraPosition(CameraPosition(latlng, zoomLevel, 0f, 0f))
                map.animateCamera(camera)
            } else {
                AnkoLogger("AmapError").error {
                    "location Error, ErrCode: ${location.errorCode}, errInfo:${location.errorInfo}"
                }
            }
        }
    }

}
