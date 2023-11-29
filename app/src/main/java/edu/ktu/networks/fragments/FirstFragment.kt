package edu.ktu.networks.fragments

import android.content.res.Resources
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.ktu.networks.R
import edu.ktu.networks.databinding.FragmentFirstBinding
import edu.ktu.networks.models.Location
import edu.ktu.networks.models.RSS
import edu.ktu.networks.models.Strength
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import java.lang.Math.sqrt

class FirstFragment : Fragment() {
    private lateinit var taskList: MutableList<Location>
    private lateinit var strengthList: MutableList<Strength>
    private lateinit var binding: FragmentFirstBinding
    private val jsonLocations = mutableSetOf<Pair<Int, Int>>()
    private lateinit var rssList:  MutableList<RSS>
    private lateinit var mata: MutableList<Int>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        taskList = mutableListOf()
        mata = mutableListOf()
        rssList = mutableListOf()
        rssList.addAll(readRSSListFromSharedPreference())
        strengthList = mutableListOf()
        val gridLayout = binding.gridLayout
        createGrid(gridLayout)

        readJsonFileMatavimai(resources)
        readJsonFileStiprumai(resources, strengthList)
        mata = findClosestMatavimai()
        updateGridWithJsonData(gridLayout)
        refreshDataAndGrid()
    }

    private fun createGrid(gridLayout: GridLayout) {
        val totalRows = 48
        val totalCols = 13
        for (row in 0 until totalRows) {
            for (col in 0 until totalCols) {
                val textView = TextView(context).apply {
                    layoutParams = GridLayout.LayoutParams(
                        GridLayout.spec(GridLayout.UNDEFINED, 1f),
                        GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    ).apply {
                        width = 0
                        height = 0
                        setMargins(1, 1, 1, 1)
                    }
                    gravity = Gravity.CENTER
                    text = when {
                        col == 0 && row < totalRows -1 -> (35 - row).toString()  // First column
                        row == totalRows - 1 && col != 0 -> (col - 6).toString() // Last row
                        row == totalRows - 1 && col == 0 -> "" // Last row first column
                        else -> "0"
                    }

                    if (col == 0 || row == totalRows -1){
                        setTypeface(null, Typeface.BOLD)
                    }

                    setTextColor(resources.getColor(R.color.black))
                    setBackgroundResource(R.drawable.grid_item_border)
                }
                gridLayout.addView(textView)
            }
        }
    }

    private fun readJsonFileMatavimai(resources: Resources) {
        val jsonFile = resources.openRawResource(R.raw.matavimai).bufferedReader().use {
            it.readText()
        }

        val jsonArray = JSONArray(jsonFile)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            if (jsonObject.getString("type") == "table") {
                val matavimaiArray = jsonObject.getJSONArray("data")
                for (j in 0 until matavimaiArray.length()) {
                    val matavimasObject = matavimaiArray.getJSONObject(j)
                    val matavimas = matavimasObject.getInt("matavimas")
                    val x = matavimasObject.getInt("x")
                    val y = matavimasObject.getInt("y")
                    val atstumas = matavimasObject.getDouble("atstumas")
                    val location = Location(matavimas, x, y, atstumas)
                    taskList.add(location)
                    jsonLocations.add(Pair(x, y))
                }
            }
        }
    }

    private fun readJsonFileStiprumai(resources: Resources, stiprumaiList: MutableList<Strength>) {
        val jsonFile = resources.openRawResource(R.raw.stiprumai).bufferedReader().use {
            it.readText()
        }

        val jsonArray = JSONArray(jsonFile)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            if (jsonObject.getString("type") == "table" && jsonObject.getString("name") == "stiprumai") {
                val data = jsonObject.getJSONArray("data")
                var j = 0
                while (j < data.length()) {
                    try {
                        val first = data.getJSONObject(j)
                        val second = data.getJSONObject(j + 1)
                        val third = data.getJSONObject(j + 2)

                        val matavimas = first.getInt("matavimas")
                        val s1 = first.getInt("stiprumas")
                        val s2 = second.getInt("stiprumas")
                        val s3 = third.getInt("stiprumas")

                        stiprumaiList.add(Strength(matavimas, s1, s2, s3))
                        j += 3
                    } catch (e: JSONException) {
                        Log.e("JSONError", "Error reading JSON: ${e.message}")
                        break
                    }
                }
            }
        }
    }

    private fun updateGridWithJsonData(gridLayout: GridLayout) {
        for (i in 0 until gridLayout.childCount) {
            val textView = gridLayout.getChildAt(i) as TextView
            val row = i / 13 // assuming 13 columns
            val col = i % 13

            if (col != 0 && row != gridLayout.rowCount - 1) { // Exclude first column and last row
                val gridLocation = Pair(col - 6, 35 - row) // assuming this mapping works for your grid

                if (gridLocation in jsonLocations) {
                    textView.text = "1"
                    textView.setTextColor(resources.getColor(R.color.green))

                    // Check if this location's matavimas is in the closestMatavimai list
                    taskList.find { it.x == gridLocation.first && it.y == gridLocation.second }?.let { location ->
                        if (location.matavimas in mata) {
                            textView.setBackgroundColor(resources.getColor(R.color.blue))
                        }
                    }
                } else {
                    textView.text = "0"
                    textView.setTextColor(resources.getColor(R.color.red))
                }
            }
        }
    }

    private fun findClosestMatavimai(): MutableList<Int> {
        val closestMatavimai = mutableListOf<Int>()

        for (rss in rssList) {
            var closestDistance = Double.MAX_VALUE
            var closestMatavimas = -1

            for (strength in strengthList) {
                val distance = calculateEuclideanDistance(rss, strength)
                if (distance < closestDistance) {
                    closestDistance = distance
                    closestMatavimas = strength.matavimas
                }
            }

            if (closestMatavimas != -1) {
                closestMatavimai.add(closestMatavimas)
            }
        }

        return closestMatavimai
    }

    private fun calculateEuclideanDistance(rss: RSS, strength: Strength): Double {
        val dx = rss.s1 - strength.s1
        val dy = rss.s2 - strength.s2
        val dz = rss.s3 - strength.s3
        return kotlin.math.sqrt((dx * dx + dy * dy + dz * dz).toDouble())
    }

    private fun readRSSListFromSharedPreference(): List<RSS> {
        val sharedPreference = activity?.getSharedPreferences("sharedPreference", 0)
        val json = sharedPreference?.getString("rssList", null)
        if (json.isNullOrEmpty()) return emptyList()

        val gson = Gson()
        val type = object : TypeToken<List<RSS>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun refreshDataAndGrid(){
        rssList.clear()
        rssList.addAll(readRSSListFromSharedPreference())
        mata = findClosestMatavimai()
        updateGridWithJsonData(binding.gridLayout)
    }

    override fun onResume() {
        super.onResume()
        refreshDataAndGrid()
    }

}