package ru.gb.osmmap

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MinimapOverlay
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import ru.gb.osmmap.databinding.ActivityMainBinding


private val REQUEST_PERMISSIONS_REQUEST_CODE = 1;
private lateinit var mapView: MapView;
private lateinit var binding: ActivityMainBinding
private lateinit var mapController: IMapController

//shared pref
var setZoomSharedPreferences: String? = null
var setLastLocationLatSharedPreferences: String? = null
var setLastLocationLonSharedPreferences: String? = null
var setStartGeoPoint: GeoPoint? = null

const val KEY_FILE_SETTING = "MAP_SETTING"
const val KEY_SET_ZOOM = "SET_ZOOM"
const val KEY_LAST_LOCATIONS_LAT = "LAST_LOCATIONS_LAT"
const val KEY_LAST_LOCATIONS_LON = "LAST_LOCATIONS_LON"

lateinit var sharedPreferences: SharedPreferences
lateinit var sharedPreferencesEditor: SharedPreferences.Editor

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        mapView = binding.mapOsmDroid
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapController = mapView.controller

        // масштабирование и жесты
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // функции
        myLocationFun(mapController)
        rotateMap()
        scaleBarOverlay(true)
        miniMapOverlay(true)
        cpmpasOverlay(true)

        binding.myLocationButton.setOnClickListener {
            myLocationFun(mapController)
        }
        //val startPoint = GeoPoint(55.755864, 37.617698)
    }

    fun sharedPreferences(){
        //init share pref
        sharedPreferences = getSharedPreferences(KEY_FILE_SETTING, Context.MODE_PRIVATE)

        setZoomSharedPreferences = sharedPreferences.getString(KEY_SET_ZOOM, "")
        if ((setZoomSharedPreferences == null) || (setZoomSharedPreferences == "")) {
            sharedPreferencesEditor = sharedPreferences.edit()
            sharedPreferencesEditor.putString(KEY_SET_ZOOM, "4.0")
            sharedPreferencesEditor.apply()
            mapController.setZoom(4.0)

        } else {
            var getSetZoomSharedPreferences = sharedPreferences.getString(KEY_SET_ZOOM, "")
            mapController.setZoom(getSetZoomSharedPreferences!!.toDouble())
        }

        setLastLocationLatSharedPreferences = sharedPreferences.getString(KEY_LAST_LOCATIONS_LAT, "")
        setLastLocationLonSharedPreferences = sharedPreferences.getString(KEY_LAST_LOCATIONS_LON, "")
        if (setLastLocationLatSharedPreferences.isNullOrBlank() && setLastLocationLonSharedPreferences.isNullOrBlank()) {
            val aLat = 55.755864
            val aLon = 37.617698
            setStartGeoPoint = GeoPoint(aLat, aLon)
            sharedPreferencesEditor = sharedPreferences.edit()
            sharedPreferencesEditor.putString(KEY_LAST_LOCATIONS_LAT, setStartGeoPoint!!.latitude.toString())
            sharedPreferencesEditor.putString(KEY_LAST_LOCATIONS_LON, setStartGeoPoint!!.longitude.toString())
            sharedPreferencesEditor.apply()
            mapController.setCenter(setStartGeoPoint)
        } else {
            var aLat :Double? = sharedPreferences.getString(KEY_LAST_LOCATIONS_LAT,"")!!.toDouble()
            var aLon :Double? = sharedPreferences.getString(KEY_LAST_LOCATIONS_LON,"")!!.toDouble()
            var position = GeoPoint(aLat!!.toDouble(), aLon!!.toDouble())
//            val aLat1 = 55.755864
//            val aLon1 = 37.617698

            mapController.setCenter(position)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
//        sharedPreferencesEditor = sharedPreferences.edit()
//        sharedPreferencesEditor.putString(KEY_SET_ZOOM, mapView.zoomLevelDouble.toString())
//        sharedPreferencesEditor.apply()
    }



    private fun cpmpasOverlay(flag: Boolean) {
        //компас
        val mCompassOverlay =
            CompassOverlay(
                applicationContext,
                InternalCompassOrientationProvider(applicationContext),
                mapView
            )
        mCompassOverlay.enableCompass()
        mCompassOverlay.isEnabled = flag
        mapView.overlays.add(mCompassOverlay)
    }

    private fun miniMapOverlay(flag: Boolean) {
        //mini карта
        val dm = applicationContext.getResources().getDisplayMetrics()
        val mMinimapOverlay =
            MinimapOverlay(applicationContext, mapView.getTileRequestCompleteHandler())
        mMinimapOverlay.setWidth(dm.widthPixels / 5)
        mMinimapOverlay.setHeight(dm.heightPixels / 6)
        mMinimapOverlay.isEnabled = flag
        mapView.overlays.add(mMinimapOverlay)
    }

    private fun scaleBarOverlay(flag: Boolean) {
        // шкала масштаба
        val mScaleBarOverlay = ScaleBarOverlay(mapView);
        mScaleBarOverlay.setCentred(true);
        //play around with these values to get the location on screen in the right place for your application
        //val dm = applicationContext.getResources().getDisplayMetrics()
        //mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 50);
        mScaleBarOverlay.setAlignBottom(true)
        mScaleBarOverlay.textPaint.color = ContextCompat.getColor(applicationContext, R.color.red)
        mScaleBarOverlay.setTextSize(50f)
        mScaleBarOverlay.isEnabled = flag
        mapView.overlays.add(mScaleBarOverlay);
    }

    private fun rotateMap() {
        // вращение карты
        val mRotationGestureOverlay = RotationGestureOverlay(this, mapView)
        mRotationGestureOverlay.setEnabled(true)
        mapView.setMultiTouchControls(true)
        mapView.overlays.add(mRotationGestureOverlay)
    }

    private fun myLocationFun(mapController: IMapController) {
        val mLocationOverlay =
            MyLocationNewOverlay(GpsMyLocationProvider(applicationContext), mapView)
        mLocationOverlay.enableMyLocation()
        //mLocationOverlay.isOptionsMenuEnabled = true
        mLocationOverlay.enableFollowLocation()
        mapController.setCenter(mLocationOverlay.myLocation)
        mapView.overlays.add(mLocationOverlay)
    }

    override fun onResume() {
        super.onResume()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        var prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        sharedPreferences()
        mapView.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        val prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().save(this, prefs);
        sharedPreferences()
        mapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onDestroy() {
        sharedPreferences()
        super.onDestroy()
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val permissionsToRequest = ArrayList<String>();
        var i = 0;
        while (i < grantResults.size) {
            permissionsToRequest.add(permissions[i]);
            i++;
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }
    }
}
