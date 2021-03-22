package com.example.weatherapp.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.repo.WeatherAppRepository
import com.example.weatherapp.repo.db.room.BookmarkDataBase
import com.example.weatherapp.ui.WeatherAppViewModelFactory
import com.example.weatherapp.ui.WeatherAppViewModel
import com.example.weatherapp.util.Constants.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.example.weatherapp.util.RainChance.Companion.getRainChance
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import kotlin.math.round


class HomeFragment : Fragment(), CityDataAdapter.OnBookmarkClickListener,
    CityDataAdapter.OnItemClickListener {

    private lateinit var weatherAppViewModel: WeatherAppViewModel
    private lateinit var cityDataAdapter: CityDataAdapter
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: FragmentHomeBinding
    private lateinit var repository: WeatherAppRepository
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**
         * setting up the fragment binding
         */
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        /**
         * setting up repository and view model instances
         */
        repository = WeatherAppRepository(BookmarkDataBase.getInstance(requireContext()).bookmarkDao())
        val navBar: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navBar.visibility = View.VISIBLE
        weatherAppViewModel =
            ViewModelProvider(this, WeatherAppViewModelFactory(repository)).get(WeatherAppViewModel::class.java)

        setupRecyclerView()
        /**
         * setting city data observer
         */
        displayBookmarkedCities()
        observeWeatherInfo()
        /**
         * fetching current location info
         */
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        fetchLocation()
        return binding.root
    }

    private fun displayBookmarkedCities() {
        weatherAppViewModel.cities.observe(viewLifecycleOwner, Observer {
            if(it.size>0) {
                binding.cityRV.visibility = View.VISIBLE
                cityDataAdapter.bookmarks = it
                cityDataAdapter.notifyDataSetChanged()
            }else{
                binding.cityRV.visibility = View.GONE
                binding.noCitiesTV.visibility = View.VISIBLE
            }
        })
    }

    private fun observeWeatherInfo() {
        weatherAppViewModel.currentCityInfo.observe(viewLifecycleOwner, Observer {
            binding.cityNameTV.text =
                String.format(getString(R.string.user_location), it.name)
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

    private fun fetchLocation() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(OnCompleteListener {
                    val location = it.result
                    if (location != null) {
                        lifecycleScope.launch {
                            val latLangData: MutableMap<String, String> = mutableMapOf()
                            latLangData.put("lat", location.latitude.toString())
                            latLangData.put("lon", location.longitude.toString())
                            repository.getCurrentCityWeatherInfo(latLangData)
                        }

                    } else {
                        requestNewLocationData()
                    }
                })
            }
            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 5
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        // setting LocationRequest
        // on FusedLocationClient
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()!!
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            fetchLocation()
        }
    }

    private fun setupRecyclerView() = binding.cityRV.apply {
        cityDataAdapter = CityDataAdapter(this@HomeFragment, this@HomeFragment)
        adapter = cityDataAdapter
        layoutManager = LinearLayoutManager(context)
    }

    override fun onBookmarkClick(position: Int) {
        val clickedItem = cityDataAdapter.bookmarks[position]
        when (clickedItem.bookmarked) {
            true -> {
                lifecycleScope.launch {
                    repository.deleteCity(cityDataAdapter.bookmarks[position])
                }
                cityDataAdapter.notifyItemRemoved(position)
            }
            else -> {
                clickedItem.bookmarked = true
                lifecycleScope.launch {
                    repository.updateCity(clickedItem)
                }
                cityDataAdapter.notifyItemChanged(position)
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty()) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocation()
                }
                return
            }
        }
    }

    override fun onItemClick(position: Int) {
        val clickedItem = cityDataAdapter.bookmarks[position]
        val bundle = bundleOf("lat" to clickedItem.latitude,"lon" to clickedItem.longitude)
        findNavController().navigate(R.id.navigation_notifications,bundle)
    }
}