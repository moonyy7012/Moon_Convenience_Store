package com.moon.convenience_store.tab_fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.moon.convenience_store.MainActivity
import com.moon.convenience_store.R


class BottomFragment2 : Fragment(), OnMapReadyCallback {

    private val UPDATE_INTERVAL = 1000
    private val FASTEST_UPDATE_INTERVAL = 500
    private var mMap: GoogleMap? = null
    private var storeMarker: Marker? = null
    private var mFusedLocationClient: FusedLocationProviderClient?=null
    private lateinit var locationRequest: LocationRequest
    private lateinit var mCurrentLocation: Location
    private lateinit var currentPosition: LatLng
    private var firstRendering: Boolean = true
    private lateinit var mapView: MapView
    private var needRequest = false



    // 위치 서비스 실행 관련 필요한 퍼미션 정의
    private val requiredMapPermission = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
    )

    private val requestActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        // 사용자가 GPS 를 켰는지 검사
        if (checkLocationServicesStatus()) {
            startLocationUpdates()
        }
    }

    private val mapPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (checkLocationServicesStatus()) {
            needRequest = true

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_my_location, null)
//        적절한 시스템 설정
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL.toLong()
            smallestDisplacement = 10.0f
            fastestInterval = FASTEST_UPDATE_INTERVAL.toLong()
        }

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        mapView = view.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        //mapView에 구글 지도 띄우기
        mapView.getMapAsync(this)
        return view
    }

    //구글맵이 띄워지면 실행
    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        //권한 없을때 권한 요청 & 실행 시 초기 위치 이동
        setDefaultStoreLocation()
        // 위치 권한을 가지고 있는지 확인
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            requiredMapPermission[0]
        )

        // 권한이 허용되어 있다면 위치 업데이트 시작
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else { //권한이 없다면 권한 요청 진행
            //사용자가 퍼미션 거부를 한 적이 있는 경우에는 다이얼로그를 이용한 권한 요청
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    requiredMapPermission[0]
                )
            ) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("위치 권한 허용")
                    .setMessage("위치 권한 허용이 필요합니다")
                    .setPositiveButton("확인") { _, _ ->
                        mapPermissionResult.launch(requiredMapPermission[0])
                    }
                val alertDialog = builder.create()
                alertDialog.show()
            } else {
                //사용자가 퍼미션 거부를 한 적이 없는 경우에는 권한 요청
                mapPermissionResult.launch(requiredMapPermission[0])
            }
        }

        mMap!!.uiSettings.isMyLocationButtonEnabled = true
    }
    // 위치 정보 업데이트 시 호출되는 Callback 함수
    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                val location = locationList[locationList.size - 1]
                currentPosition = LatLng(location.latitude, location.longitude)


                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location)
                mCurrentLocation = location
            }
        }
    }

    // 권한 확인 및 위치 정보 업데이트
    private fun startLocationUpdates() {

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting()
        } else {
            if (checkMapPermission()) {
                mFusedLocationClient?.requestLocationUpdates(  //
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()!!
                )
                if (mMap != null) mMap!!.isMyLocationEnabled = true
                if (mMap != null) mMap!!.uiSettings.isZoomControlsEnabled = true
            }
        }
    }

    // GPS 켜져있는지 확인
    private fun checkLocationServicesStatus(): Boolean {
        val locationManager = (context as MainActivity).getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        return true
    }

    // 현재 위치 표시, 현재 위치로 카메라 이동
    fun setCurrentLocation(location: Location) {

        val currentLatLng = LatLng(location.latitude, location.longitude)

        val cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng)
        mMap!!.moveCamera(cameraUpdate)

    }

    // 초기 편의점 위치 지정
    private fun setDefaultStoreLocation(){
        val DEFAULT_LOCATION = LatLng(37.61854398017381, 126.91475158957141)
        val location = Location("")
        location.latitude = DEFAULT_LOCATION.latitude
        location.longitude = DEFAULT_LOCATION.longitude
        setCurrentLocation(location)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15f)
        mMap!!.moveCamera(cameraUpdate)
        val bitmap = (ResourcesCompat.getDrawable(resources, R.drawable.location_icon, null) as BitmapDrawable).bitmap
        val resized = Bitmap.createScaledBitmap(bitmap, 100, 100,false)
        val markerOptions = MarkerOptions()
        markerOptions.position(DEFAULT_LOCATION)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resized))
        markerOptions.draggable(true)

        storeMarker = mMap!!.addMarker(markerOptions)



    }

    // 위치 정보 권한 허용 여부 체크
    private fun checkMapPermission(): Boolean {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        return hasFineLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    private val GPS_ENABLE_REQUEST_CODE = 2001

    private fun showDialogForLocationServiceSetting() {
        val builder: androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(context as MainActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage(
            "앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
        )
        builder.setCancelable(true)
        builder.setPositiveButton("설정") { _, _ ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            requestActivity.launch(callGPSSettingIntent)
        }
        builder.setNegativeButton("취소"
        ) { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}