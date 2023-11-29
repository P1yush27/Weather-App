package com.example.weatherapp

import android.annotation.SuppressLint
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

const val apiId = "1382141824810491043129"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchWeatherData("Jaipur")
        searchCity()
    }

    private fun searchCity() {
        val searchView = binding.svLocation
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    val cityName = query.trim().lowercase().toCamelCase()
                    Log.d("CityName", "onQueryTextSubmit: $cityName")
                    fetchWeatherData(cityName)
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        Log.d("fetch", "fetchWeatherData: start")
        binding.infoCard.visibility = View.GONE
        binding.loadingAnimation.visibility = View.VISIBLE
        binding.loadingCard.visibility = View.VISIBLE
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(APIInterface::class.java)

        val response =
            retrofit.getWeatherData(cityName, apiId , "metric")
        response.enqueue(object : Callback<WeatherData> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val temp = responseBody.main.temp.roundToInt()
                    val tempMin = floor(responseBody.main.temp_min).roundToInt()
                    val tempMax = ceil(responseBody.main.temp_max).roundToInt()
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val sunrise = responseBody.sys.sunrise.toLong()
                    val sunset = responseBody.sys.sunset.toLong()
                    val sea = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main ?: "unknown"
                    val realFeel = responseBody.main.feels_like
                    val day = dayName()
                    val date = date()

                    binding.tvTemp.text = "$temp"
                    binding.tvTempRange.text = "$tempMin° — $tempMax°"
                    binding.tvHumidity.text = "$humidity"
                    binding.tvRealFeel.text = "$realFeel"
                    binding.tvSea.text = "$sea"
                    binding.tvSunset.text = time(sunset)
                    binding.tvSunrise.text = time(sunrise)
                    binding.tvWeatherDescription.text = condition
                    binding.tvWindSpeed.text = "$windSpeed"
                    binding.tvLocation.text = cityName
                    binding.tvDayDate.text = "$day — $date"

                    updateAnimation(condition)
                    binding.loadingCard.visibility = View.GONE
                    binding.loadingAnimation.visibility = View.GONE
                    binding.infoCard.visibility = View.VISIBLE
                    Log.d("fetch", "fetchWeatherData: end")
                }
            }

            override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                Toast.makeText(this@MainActivity, "You are offline", Toast.LENGTH_LONG).show()
            }
        })

    }

    private fun updateAnimation(condition: String) {
        when (condition) {
            "Haze", "Mist", "Dust", "Fog", "Sand", "Ash", "Squall", "Tornado", "Smoke"-> {
                binding.weatherAnimation.setAnimation(R.raw.windy_animation)
            }
            "Rain" -> {
                binding.weatherAnimation.setAnimation(R.raw.sunny_rainy_animation)
            }
            "Snow" -> {
                binding.weatherAnimation.setAnimation(R.raw.snowy_animation)
            }
            "Thunderstorm" -> {
                binding.weatherAnimation.setAnimation(R.raw.thunder_rain_animation)
            }
            "Clear" -> {
                binding.weatherAnimation.setAnimation(R.raw.sunny_animation)
            }
            "Drizzle" -> {
                binding.weatherAnimation.setAnimation(R.raw.sunny_rainy_animation)
            }
            "Clouds" -> {
                binding.weatherAnimation.setAnimation(R.raw.cloudy_animation)
            }
            else -> {
                binding.weatherAnimation.setAnimation(R.raw.sunny_animation)
            }
        }
        binding.weatherAnimation.playAnimation()
    }

    fun dayName(): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }

    fun time(timestamp: Long): String{
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timestamp*1000)))
    }

    fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM", Locale.getDefault())
        return sdf.format((Date()))
    }

    fun String.toCamelCase(delimiter: String = " "): String {
        return split(delimiter).joinToString(delimiter) { word ->
            word.replaceFirstChar(Char::titlecaseChar)
        }
    }
}