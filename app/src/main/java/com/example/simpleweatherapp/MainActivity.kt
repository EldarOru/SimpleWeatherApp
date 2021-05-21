package com.example.simpleweatherapp

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.ColorSpace
import android.os.AsyncTask
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val info: String = "CITY_INFO"
    var CITY: String = "saint petersburg, ru"
    private val API: String = "6f111a1a708cd4b1fbcc8e87f9b62795"
    private lateinit var sharedPreferences:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences(info,Context.MODE_PRIVATE)
        CITY = if (sharedPreferences.getString("CITY", " ").toString().isEmpty()){
            "saint petersburg, ru"
        }else {
            sharedPreferences.getString("CITY", " ").toString()
        }
        WeatherTask().execute()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.upd -> {
                WeatherTask().execute()
                return true
            }
            R.id.yourCity -> {
                showInputDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class WeatherTask : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.error_text).visibility = View.GONE
        }

        override fun doInBackground(vararg p0: String?): String? {
            return try {
                URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API")
                        .readText(Charsets.UTF_8)
            } catch (e: Exception) {
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val decimal = DecimalFormat("#.#")
                val jsonObject = JSONObject(result)
                val main = jsonObject.getJSONObject("main")
                val sys = jsonObject.getJSONObject("sys")
                val wind = jsonObject.getJSONObject("wind")
                val weather = jsonObject.getJSONArray("weather").getJSONObject(0)
                val updatedAt: Long = jsonObject.getLong("dt")
                val updatedAtText = getString(R.string.updateAt) + ": " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt * 1000))
                val temp = decimal.format(main.getString("temp").toDouble()) + "°С"
                //val tempMin = getString(R.string.min_temp) + " " + main.getString("temp_min").substring(0,main.getString("temp_min").length-1) + "°С"
                val tempMin = getString(R.string.min_temp) + " " + decimal.format(main.getString("temp_min").toDouble()) + "°С"
                val tempMax = getString(R.string.max_temp) + " " + decimal.format(main.getString("temp_max").toDouble()) + "°С"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")
                val sunrise: Long = sys.getLong("sunrise")
                val sunset: Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")
                val address = jsonObject.getString("name") + ", " + sys.getString("country")

                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.updated_at).text = updatedAtText
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temperature).text = temp
                findViewById<TextView>(R.id.min_temp).text = tempMin
                findViewById<TextView>(R.id.max_temp).text = tempMax
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity
                checkIcon(weather.getString("main").capitalize())

                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE
            } catch (e: Exception) {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.error_text).visibility = View.VISIBLE
            }
        }
    }

    private fun showInputDialog() {
        val builder = AlertDialog.Builder(this,R.style.AppCompatAlertDialogStyle)
        builder.setTitle("Change city")
        val input = EditText(this)
        input.setTextColor(Color.WHITE)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setMessage("Enter your city")
        builder.setNegativeButton("No") { _, _ ->
            Toast.makeText(applicationContext, "Back", Toast.LENGTH_SHORT).show()}
        builder.setPositiveButton("Change") { dialog, which -> CITY = input.text.toString()
        WeatherTask().execute()
        val editor = sharedPreferences.edit()
        editor.putString("CITY",CITY)
        editor.apply()
        builder.create() }
        builder.show()
    }

    private fun checkIcon(string: String){
        when(string){
            "Rain" -> findViewById<ImageView>(R.id.weatherCondition).setImageResource(R.drawable.ic_wi_rain)
            "Clouds" -> findViewById<ImageView>(R.id.weatherCondition).setImageResource(R.drawable.ic_wi_cloud)
            "Snow" -> findViewById<ImageView>(R.id.weatherCondition).setImageResource(R.drawable.ic_wi_snow)
            "Clear" -> findViewById<ImageView>(R.id.weatherCondition).setImageResource(R.drawable.ic_wi_day_sunny)
            "Thunderstorm" -> findViewById<ImageView>(R.id.weatherCondition).setImageResource(R.drawable.ic_wi_thunderstorm)
            "Drizzle" -> findViewById<ImageView>(R.id.weatherCondition).setImageResource(R.drawable.ic_wi_rain_mix)
            else -> {findViewById<ImageView>(R.id.weatherCondition).setImageResource(R.drawable.ic_wi_alien)}
        }
    }


}
