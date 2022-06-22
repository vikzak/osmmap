package ru.gb.osmmap

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import ru.gb.osmmap.databinding.ActivityMainBinding


private val REQUEST_PERMISSIONS_REQUEST_CODE = 1;
private lateinit var mapView: MapView;
private lateinit var binding: ActivityMainBinding



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        mapView = binding.mapOsmDroid
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        // масштабирование и жесты
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // устновка стартовой точки
        val mapController = mapView.controller
        mapController.setZoom(16.0)
        val startPoint = GeoPoint(55.751442, 37.615569) //55.751442, 37.615569
        mapController.setCenter(startPoint)

        // добавление маркера
        val markerOSM = Marker(mapView)
        markerOSM.position = startPoint
        markerOSM.icon = applicationContext.getDrawable(R.drawable.ic_baseline_place_24)
        markerOSM.title ="Moscow"
        markerOSM.infoWindow
        mapView.overlay.add(markerOSM.icon!!)
        mapView.invalidate()


        //линии сетки (смотрятся ужасно)
        //val overlay = LatLonGridlineOverlay2()
        //map.getOverlays().add(overlay)

        // вращение карты
        val mRotationGestureOverlay = RotationGestureOverlay(this, mapView)
        mRotationGestureOverlay.setEnabled(true)
        mapView.setMultiTouchControls(true)
        mapView.getOverlays().add(mRotationGestureOverlay)

        // шкала масштаба
        val mScaleBarOverlay = ScaleBarOverlay(mapView);
        mScaleBarOverlay.setCentred(true);
        val dm = applicationContext.getResources().getDisplayMetrics()
        //play around with these values to get the location on screen in the right place for your application
        //mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 50);
        mScaleBarOverlay.setAlignBottom(true)
        mScaleBarOverlay.textPaint.color = ContextCompat.getColor(applicationContext,R.color.red)
        mScaleBarOverlay.setTextSize(50f)
        mapView.getOverlays().add(mScaleBarOverlay);


        //mini карта
        val mMinimapOverlay = MinimapOverlay(applicationContext, mapView.getTileRequestCompleteHandler())
        mMinimapOverlay.setWidth(dm.widthPixels / 5)
        mMinimapOverlay.setHeight(dm.heightPixels / 6)
        mapView.getOverlays().add(mMinimapOverlay)


        //програмно ставим метку 55.774472, 37.583050
        val items = ArrayList<OverlayItem>()
        items.add(
            OverlayItem(
                "Тест метки",
                "55.774472, 37.583050",
                GeoPoint(55.774472, 37.583050)//55.751442, 37.615569
            )
        )

        val mOverlay = ItemizedOverlayWithFocus(items,
            object : OnItemGestureListener<OverlayItem?> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    //do something
                    return true
                }

                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                    return false
                }
            }, applicationContext
        )
        mOverlay.setFocusItemsOnTap(true)
        mapView.getOverlays().add(mOverlay)


        //компас
        val mCompassOverlay =
            CompassOverlay(applicationContext, InternalCompassOrientationProvider(applicationContext), mapView)
        mCompassOverlay.enableCompass()
        mapView.getOverlays().add(mCompassOverlay)

        //gps (не работает или не показывается)
        val mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(applicationContext), mapView)
        mLocationOverlay.enableMyLocation()
        mapView.getOverlays().add(mLocationOverlay)


    }
    override fun onResume() {
        super.onResume()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        var prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        mapView.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        val prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().save(this, prefs);
        mapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }


    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
                REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /*private fun requestPermissionsIfNecessary(String[] permissions) {
       ArrayList<String> permissionsToRequest = new ArrayList<>();
       for (String permission : permissions) {
       if (ContextCompat.checkSelfPermission(this, permission)
               != PackageManager.PERMISSION_GRANTED) {
           // Permission is not granted
           permissionsToRequest.add(permission);
       }
   }
       if (permissionsToRequest.size() > 0) {
           ActivityCompat.requestPermissions(
                   this,
                   permissionsToRequest.toArray(new String[0]),
                   REQUEST_PERMISSIONS_REQUEST_CODE);
       }
   }*/
}