package com.example.weatherapp.ui.location

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.R
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.databinding.FragmentDashboardBinding
import com.example.weatherapp.repo.WeatherAppRepository
import com.example.weatherapp.repo.db.room.Bookmark
import com.example.weatherapp.repo.db.room.BookmarkDataBase
import com.example.weatherapp.ui.WeatherAppViewModel
import com.example.weatherapp.ui.WeatherAppViewModelFactory
import com.example.weatherapp.util.Constants.Companion.MAP_KEY
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.MaterialSearchBar.OnSearchActionListener
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class LocationFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var weatherAppViewModel: WeatherAppViewModel
    private var mMap: GoogleMap? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var placesClient: PlacesClient? = null
    private var predictionList: kotlin.collections.List<AutocompletePrediction>? = null

    private var mLastKnownLocation: Location? = null
    private var locationCallback: LocationCallback? = null

    private var mapView: View? = null

    private lateinit var binding: FragmentDashboardBinding
    private val DEFAULT_ZOOM = 15f
    private lateinit var myMarker: Marker
    private lateinit var repository: WeatherAppRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**
         * setting up repository and view model instances
         */
        repository =
            WeatherAppRepository(BookmarkDataBase.getInstance(requireContext()).bookmarkDao())
        val navBar: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navBar.visibility = View.VISIBLE
        weatherAppViewModel =
            ViewModelProvider(
                this,
                WeatherAppViewModelFactory(WeatherApplication(), repository)
            ).get(WeatherAppViewModel::class.java)
        /**
         * setting up the fragment binding
         */
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root = inflater.inflate(
            com.example.weatherapp.R.layout.fragment_dashboard,
            container,
            false
        )
        val mapFragment =
            childFragmentManager.findFragmentById(com.example.weatherapp.R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapView = mapFragment.view

        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        Places.initialize(requireContext(), MAP_KEY)
        placesClient = Places.createClient(requireContext())
        val token = AutocompleteSessionToken.newInstance()
        binding.searchBar.setOnSearchActionListener(object : OnSearchActionListener {
            override fun onSearchStateChanged(enabled: Boolean) {}
            override fun onSearchConfirmed(text: CharSequence) {

            }

            override fun onButtonClicked(buttonCode: Int) {
                if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    Handler().postDelayed({
                        binding.searchBar.clearSuggestions()
                        binding.searchBar.closeSearch()
                    }, 300)
                }
            }
        })
        binding.searchBar.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val predictionsRequest = FindAutocompletePredictionsRequest.builder()
                    .setTypeFilter(TypeFilter.CITIES)
                    .setSessionToken(token)
                    .setQuery(s.toString())
                    .build()
                placesClient!!.findAutocompletePredictions(predictionsRequest)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val predictionsResponse = task.result
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.autocompletePredictions
                                val suggestionsList: MutableList<String> = ArrayList()
                                for (i in (predictionList as MutableList<AutocompletePrediction>).indices) {
                                    val prediction =
                                        (predictionList as MutableList<AutocompletePrediction>).get(
                                            i
                                        )
                                    suggestionsList.add(prediction.getFullText(null).toString())
                                }
                                binding.searchBar.updateLastSuggestions(suggestionsList)
                                if (!binding.searchBar.isSuggestionsVisible()) {
                                    binding.searchBar.showSuggestionsList()
                                }
                            }
                        } else {
                            Log.i("mytag", "prediction fetching task unsuccessful")
                        }
                    }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding.searchBar.setSuggestionsClickListener(object :
            SuggestionsAdapter.OnItemViewClickListener {
            override fun OnItemClickListener(position: Int, v: View) {
                if (position >= predictionList!!.size) {
                    return
                }
                val selectedPrediction = predictionList!![position]
                val suggestion: String =
                    binding.searchBar.getLastSuggestions().get(position).toString()
                binding.searchBar.setText(suggestion)
                Handler().postDelayed({
                    binding.searchBar.clearSuggestions()
                    binding.searchBar.closeSearch()
                }, 300)
                val placeId = selectedPrediction.placeId
                val placeFields: List<Place.Field> =
                    Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME)
                val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()
                placesClient!!.fetchPlace(fetchPlaceRequest)
                    .addOnSuccessListener { fetchPlaceResponse ->
                        val place = fetchPlaceResponse.place
                        Log.i("mytag", "Place found: " + place.name)
                        val latLngOfPlace = place.latLng
                        if (latLngOfPlace != null) {
                            mMap!!.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLngOfPlace,
                                    DEFAULT_ZOOM
                                )
                            )
                            if (::myMarker.isInitialized)
                                myMarker.remove()
                            myMarker = mMap!!.addMarker(
                                MarkerOptions()
                                    .position(latLngOfPlace)
                                    .title(place.name)
                                    .draggable(true)
                            )
                        }
                    }.addOnFailureListener { e ->
                        if (e is ApiException) {
                            val apiException = e as ApiException
                            apiException.printStackTrace()
                            val statusCode = apiException.statusCode
                            Log.i("mytag", "place not found: " + e.message)
                            Log.i("mytag", "status code: $statusCode")
                        }
                    }
            }

            override fun OnItemDeleteListener(position: Int, v: View) {}
        })
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.uiSettings.isCompassEnabled = true
        mMap!!.isMyLocationEnabled = true
        mMap!!.uiSettings.isMyLocationButtonEnabled = true
        mMap!!.setOnMarkerClickListener(this)
        if (mapView != null && mapView!!.findViewById<View?>("1".toInt()) != null) {
            val locationButton =
                (mapView!!.findViewById<View>("1".toInt()).parent as View).findViewById<View>("2".toInt())
            val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
            layoutParams.setMargins(0, 0, 40, 180)
        }

        //check if gps is enabled or not and then request user to enable it
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> =
            settingsClient.checkLocationSettings(builder.build())
        task.addOnSuccessListener(requireActivity(),
            OnSuccessListener<LocationSettingsResponse?> { getDeviceLocation() })
        task.addOnFailureListener(requireActivity(), OnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(requireActivity(), 51)
                } catch (e1: SendIntentException) {
                    e1.printStackTrace()
                }
            }
        })
        mMap!!.setOnMyLocationButtonClickListener(OnMyLocationButtonClickListener {
            if (binding.searchBar.isSuggestionsVisible) binding.searchBar.clearSuggestions()
            if (binding.searchBar.isSearchOpened) binding.searchBar.closeSearch()
            false
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        mFusedLocationProviderClient!!.lastLocation
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mLastKnownLocation = task.result
                    if (mLastKnownLocation != null) {
                        mMap!!.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    mLastKnownLocation!!.getLatitude(),
                                    mLastKnownLocation!!.getLongitude()
                                ), DEFAULT_ZOOM
                            )
                        )
                    } else {
                        val locationRequest = LocationRequest.create()
                        locationRequest.interval = 10000
                        locationRequest.fastestInterval = 5000
                        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        locationCallback = object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                super.onLocationResult(locationResult)
                                if (locationResult == null) {
                                    return
                                }
                                mLastKnownLocation = locationResult.lastLocation
                                mMap!!.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            mLastKnownLocation!!.getLatitude(),
                                            mLastKnownLocation!!.getLongitude()
                                        ), DEFAULT_ZOOM
                                    )
                                )
                                mFusedLocationProviderClient!!.removeLocationUpdates(
                                    locationCallback
                                )
                            }
                        }
                        mFusedLocationProviderClient!!.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            null
                        )
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "unable to get last location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker!!.equals(myMarker)) {
            if (marker.title == null)
                marker.title = ""
            showAlert()
        }
        return false
    }

    fun showAlert() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.bookmark_city)
            .setCancelable(false)
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                lifecycleScope.launch {
                    weatherAppViewModel.insertCity(
                        Bookmark(
                            0,
                            myMarker.position.latitude.toString(),
                            myMarker.position.longitude.toString(),
                            myMarker.title,
                            "",
                            true
                        )
                    )
                }
            })
            .setNegativeButton(
                "No",
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                })
        val alert: AlertDialog = builder.create()
        alert.setTitle(R.string.add_bookmark)
        alert.show()
    }
}