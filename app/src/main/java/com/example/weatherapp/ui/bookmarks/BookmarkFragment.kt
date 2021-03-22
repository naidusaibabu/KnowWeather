package com.example.weatherapp.ui.bookmarks

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.repo.WeatherAppRepository
import com.example.weatherapp.repo.db.room.Bookmark
import com.example.weatherapp.repo.db.room.BookmarkDataBase
import com.example.weatherapp.ui.WeatherAppViewModelFactory
import com.example.weatherapp.ui.home.CityDataAdapter
import com.example.weatherapp.ui.WeatherAppViewModel
import com.example.weatherapp.util.RainChance.Companion.getRainChance
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.round


class BookmarkFragment : Fragment() {

    private lateinit var weatherAppViewModel: WeatherAppViewModel
    private lateinit var binding: FragmentHomeBinding
    private lateinit var cityDataAdapter: CityDataAdapter
    private lateinit var repository: WeatherAppRepository
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        /**
         * setting up the fragment binding
         */
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val navBar: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navBar.visibility = View.GONE
        /**
         * setting up repository and view model instances
         */
        repository = WeatherAppRepository(BookmarkDataBase.getInstance(requireContext()).bookmarkDao())
        weatherAppViewModel =
            ViewModelProvider(this, WeatherAppViewModelFactory(repository)).get(WeatherAppViewModel::class.java)
        arguments?.getString("lon")
        setupRecyclerView()
        observeWeatherInfo()
        observeForecastInfo()
        lifecycleScope.launch {
            val latLangData: MutableMap<String, String> = mutableMapOf()
            latLangData.put("lat", arguments?.getString("lat")!!)
            latLangData.put("lon", arguments?.getString("lon")!!)
            repository.getCurrentCityWeatherInfo(latLangData)
        }
        binding.listTitleTV.text = getString(R.string.city_forecast)
        lifecycleScope.launch {
            val latLangData: MutableMap<String, String> = mutableMapOf()
            latLangData.put("lat", arguments?.getString("lat")!!)
            latLangData.put("lon", arguments?.getString("lon")!!)
            repository.getWeatherInfo(latLangData)
        }
        return binding.root
    }

    private fun setupRecyclerView() = binding.cityRV.apply {
        cityDataAdapter = CityDataAdapter(null, null)
        adapter = cityDataAdapter
        layoutManager = LinearLayoutManager(context)
    }

    private fun observeForecastInfo() {
        weatherAppViewModel.weatherInfo.observe(viewLifecycleOwner, Observer {
            val bookmarks: MutableList<Bookmark> = mutableListOf()
            val cal = Calendar.getInstance(Locale.ENGLISH)
            var firstItem = true
            it.daily.forEach { city ->
                run {
                    if (!firstItem) {
                        cal.timeInMillis = city.dt.toLong() * 1000L
                        val date: String = DateFormat.format("EEE MMM dd", cal).toString()
                        val temp = String.format(getString(R.string.temparature), city.temp.day)
                        val feelsLike =
                            String.format(getString(R.string.temparature), city.feels_like.day)
                        bookmarks.add(Bookmark(0, "", "", date, temp + " / " + feelsLike, false))
                    }
                    firstItem = false
                }
            }
            cityDataAdapter.bookmarks = bookmarks
            cityDataAdapter.notifyDataSetChanged()
        })
    }

    private fun observeWeatherInfo() {
        weatherAppViewModel.currentCityInfo.observe(viewLifecycleOwner, Observer {
            binding.cityNameTV.text =
                String.format(getString(R.string.bookmark_location), it.name)
            binding.weatherInfoTV.text =
                String.format(getString(R.string.weather_info), it.weather[0].main)
            binding.temparatureTV.text =
                String.format(getString(R.string.temparature), it.main.temp)
            binding.feelsLikeTV.text =
                String.format(getString(R.string.feels_like), round(it.main.feels_like))
            binding.humidityTV.text =
                String.format(getString(R.string.humidity), it.main.humidity) + "%"
            binding.windTV.text = String.format(getString(R.string.wind), it.wind.speed)
            binding.rainTV.text =
                String.format(getString(R.string.rain), getRainChance(it.clouds.all))
        })
    }

}