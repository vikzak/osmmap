package ru.gb.osmmap

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.core.app.ActivityCompat
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

private val REQUEST_PERMISSIONS_REQUEST_CODE = 1;
private lateinit var map: MapView;

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        // This won't work unless you have imported this: org.osmdroid.config.Configuration.*
        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, if you abuse osm's
        //tile servers will get you banned based on this string.

        //inflate and create the map
        setContentView(R.layout.activity_main);

        map = findViewById<MapView>(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK);


        //var mlocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map);
        //var mlocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map);
        //this.mlocationOverlay.enableMyLocation();
        //map.overlays.add(mlocationOverlay)
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        val mapController = map.controller
        mapController.setZoom(16.0)
        val startPoint = GeoPoint(55.751442, 37.615569) //55.751442, 37.615569
        mapController.setCenter(startPoint)

//        // Get Overlays
//        var mapOverlays = map.getOverlays()
//        var drawable = this.getResources().getDrawable(R.drawable.ic_baseline_location_on_24);
//        var itemizedOverlay = HelloItemizedOverlay(drawable);
//
////         MyLocationOverlay
//        var myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context),map)
//        myLocationOverlay.enableMyLocation()
//        map.overlays.add(myLocationOverlay)

    }
    override fun onResume() {
        super.onResume()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
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