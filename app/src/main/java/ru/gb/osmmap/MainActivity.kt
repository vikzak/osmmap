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


private val REQUEST_PERMISSIONS_REQUEST_CODE = 1 // константа для запроса
private lateinit var mapView: MapView
private lateinit var binding: ActivityMainBinding
private lateinit var mapController: IMapController

//для shared pref
var setZoomSharedPreferences: String? = null // зададим позже
var setLastLocationLatSharedPreferences: String? = null // зададим позже
var setLastLocationLonSharedPreferences: String? = null // зададим позже
var setStartGeoPoint: GeoPoint? = null // зададим позже

const val KEY_FILE_SETTING = "MAP_SETTING" // названия констант в shared pref
const val KEY_SET_ZOOM = "SET_ZOOM"
const val DEFAULT_ZOOM = "10.0"
const val KEY_LAST_LOCATIONS_LAT = "LAST_LOCATIONS_LAT" // последняя позиция широты
const val KEY_LAST_LOCATIONS_LON = "LAST_LOCATIONS_LON" // последняя позиция долготы

lateinit var sharedPreferences: SharedPreferences
lateinit var sharedPreferencesEditor: SharedPreferences.Editor

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        sharedPreferences = getSharedPreferences(KEY_FILE_SETTING, Context.MODE_PRIVATE)

        // сборка карты и объявление источника
        mapView = binding.mapOsmDroid
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapController = mapView.controller

        // масштабирование и жесты
        mapView.setBuiltInZoomControls(true) // зум
        mapView.setMultiTouchControls(true) // управление касанием

        // функции
        myLocationFun(mapController)
        rotateMap() // вращение
        scaleBarOverlay(true) // строка масштаба
        miniMapOverlay(true) // миникарта
        cpmpasOverlay(true) // компас
        sharedPreferences() // shared pref

        mapView.setTilesScaledToDpi(true) // если убрать, то шрифт будет очень мелкий на карте

        binding.myLocationButton.setOnClickListener {
            myLocationFun(mapController)
        }
    }

    fun sharedPreferences(){
        setZoomSharedPreferences = sharedPreferences.getString(KEY_SET_ZOOM, "")
        if (setZoomSharedPreferences.isNullOrBlank()) { // если значения нет или оно пусто
            sharedPreferencesEditor = sharedPreferences.edit()
            sharedPreferencesEditor.putString(KEY_SET_ZOOM, DEFAULT_ZOOM) // в поле что объявляли выше KEY_SET_ZOOM = "SET_ZOOM"
            // записываем const val DEFAULT_ZOOM = "10.0"
            sharedPreferencesEditor.apply() // применяем
            mapController.setZoom(DEFAULT_ZOOM.toDouble()) // устанавливаем на карте этот зум
        } else { //иначе
            val getSetZoomSharedPreferences = sharedPreferences.getString(KEY_SET_ZOOM, "")
            mapController.setZoom(getSetZoomSharedPreferences!!.toDouble())// берем сохраненное ранее значение
        }
        // координаты
        // тот же принцип что и выше
        setLastLocationLatSharedPreferences = sharedPreferences.getString(KEY_LAST_LOCATIONS_LAT, "")
        setLastLocationLonSharedPreferences = sharedPreferences.getString(KEY_LAST_LOCATIONS_LON, "")
        if (setLastLocationLatSharedPreferences.isNullOrBlank() && setLastLocationLonSharedPreferences.isNullOrBlank()) {
            // если данных нет, то записываем эти данные
            val aLat = 55.755864
            val aLon = 37.617698
            setStartGeoPoint = GeoPoint(aLat, aLon)
            sharedPreferencesEditor = sharedPreferences.edit()
            sharedPreferencesEditor.putString(KEY_LAST_LOCATIONS_LAT, setStartGeoPoint!!.latitude.toString())
            sharedPreferencesEditor.putString(KEY_LAST_LOCATIONS_LON, setStartGeoPoint!!.longitude.toString())
            sharedPreferencesEditor.apply()
            mapController.setCenter(setStartGeoPoint) // и ставим по центру карты
        } else {
            val aLat : Double = sharedPreferences.getString(KEY_LAST_LOCATIONS_LAT,"")!!.toDouble()
            val aLon : Double = sharedPreferences.getString(KEY_LAST_LOCATIONS_LON,"")!!.toDouble()
            val position = GeoPoint(aLat, aLon)
            mapController.setCenter(position)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        sharedPreferencesEditor = sharedPreferences.edit() // когда касаемся экрана записываем изменения в  sharedPref
        sharedPreferencesEditor.putString(KEY_SET_ZOOM, mapView.zoomLevelDouble.toString())
        sharedPreferencesEditor.apply()
        return super.onTouchEvent(event)
    }



    private fun cpmpasOverlay(flag: Boolean) {
        //компас (хрень полная конечно, проще его заменить чем-то своим)
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
        val mScaleBarOverlay = ScaleBarOverlay(mapView)
        mScaleBarOverlay.setCentred(true)
        //поиграйте с этими значениями, чтобы получить местоположение на экране в нужном месте для вашего приложения
        // с основной докементации
        //val dm = applicationContext.getResources().getDisplayMetrics()
        //mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 50);
        mScaleBarOverlay.setAlignBottom(true)
        mScaleBarOverlay.textPaint.color = ContextCompat.getColor(applicationContext, R.color.red)
        mScaleBarOverlay.setTextSize(50f)
        mScaleBarOverlay.isEnabled = flag
        mapView.overlays.add(mScaleBarOverlay)
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
        mLocationOverlay.enableFollowLocation()
        mapController.setCenter(mLocationOverlay.myLocation)
        mapView.overlays.add(mLocationOverlay)
        sharedPreferences()
    }

    override fun onResume() {
        super.onResume()
        //это обновит конфигурацию приложения при возобновлении работы
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        sharedPreferences()
        mapView.onResume() //требуется для компаса v6.0.0 и выше
    }

    override fun onPause() {
        super.onPause()
        // тоже самое что и в onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        Configuration.getInstance().save(this, prefs)
        sharedPreferences()
        mapView.onPause()  //тоже самое что и в onResume()
    }

    override fun onDestroy() {
        sharedPreferences()// сохраняются последние значения
        super.onDestroy()
    }

    @SuppressLint("MissingSuperCall")// обработка результатов запроса на разрещения
    //основные данные здесь несет массив grantResults, в котором находится информация
    // получены разрешения или нет.
    // Каждому i-му элементу permissions соответствует i-ый элемент из grantResults.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val permissionsToRequest = ArrayList<String>()
        var i = 0
        while (i < grantResults.size) {
            permissionsToRequest.add(permissions[i])
            i++
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
}
