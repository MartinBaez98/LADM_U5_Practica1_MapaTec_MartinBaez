package mx.edu.ittepic.ladm_u5_practica1_mapatec_martinbaez

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var baseRemota = FirebaseFirestore.getInstance()
    var posicion   = ArrayList<Data>()
    var lugares = ArrayList<String>()
    var long = 0.0
    var lat = 0.0
    lateinit var locacion : LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        baseRemota.collection("tecnologico").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if(firebaseFirestoreException != null){
                ubicacion.setText("Error: " + firebaseFirestoreException.message)
                return@addSnapshotListener
            }

            lugares.clear()
            posicion.clear()
            for(document in querySnapshot!!){
                var data = Data()
                data.nombre = document.getString("nombre").toString()
                data.posicion1 = document.getGeoPoint("posicion1")!!
                data.posicion2 = document.getGeoPoint("posicion2")!!
                data.centro = document.getGeoPoint("puntoc")!!

                posicion.add(data)
                lugares.add(document.getString("nombre").toString())

            }
            var adaptador = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lugares)
            lista.adapter = adaptador
        }

        lista.setOnItemClickListener { parent, view, position, id ->
            var latitud = posicion[position].centro.latitude
            var longitud = posicion[position].centro.longitude
            var intent : Intent = Intent(this, MapsActivity::class.java)
            var lugar = lugares[position]
            intent.putExtra("latitud", latitud)
            intent.putExtra("longitud", longitud)
            intent.putExtra("lugar", lugar)
            startActivity(intent)
        }

        locacion = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var oyente = Oyente(this)
        locacion.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 01f, oyente)
    }
}

class Oyente(puntero: MainActivity) : LocationListener {

    var p = puntero

    override fun onLocationChanged(location: Location) {
        p.long = location.longitude
        p.lat = location.latitude
        var geoPosicionGPS = GeoPoint(location.latitude, location.longitude)

        for(item in p.posicion){
            if(item.estoyEn(geoPosicionGPS)){
                p.ubicacion.setText(item.nombre)
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

}