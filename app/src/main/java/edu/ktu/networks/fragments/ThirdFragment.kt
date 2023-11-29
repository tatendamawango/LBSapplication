package edu.ktu.networks.fragments

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.ktu.networks.R
import edu.ktu.networks.adapters.RSSAdapter
import edu.ktu.networks.databinding.FragmentThirdBinding
import edu.ktu.networks.models.RSS
import org.json.JSONArray
import org.json.JSONException


class ThirdFragment : Fragment() {

    private lateinit var rssList: MutableList<RSS>
    private lateinit var binding: FragmentThirdBinding
    private lateinit var taskAdapter: RSSAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentThirdBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        rssList.addAll(readRSSListFromSharedPreference())
        readJsonFileVartotojai(resources, rssList)
        taskAdapter.notifyDataSetChanged()

    }

    private fun init() {
        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(context)
        rssList = mutableListOf()
        taskAdapter = RSSAdapter(rssList, this::deleteRSS)
        binding.tasksRecyclerView.adapter = taskAdapter
    }

    private fun deleteRSS(position: Int) {
        val deletedMac = rssList[position].macAddress
        rssList.removeAt(position)
        taskAdapter.notifyItemRemoved(position)
        saveRSSListToSharedPreference(rssList)

        val deletedMacs = readDeletedMacs().toMutableSet()
        deletedMacs.add(deletedMac)
        saveDeletedMacs(deletedMacs)
    }

    private fun readRSSListFromSharedPreference(): List<RSS> {
        val sharedPreference = activity?.getSharedPreferences("sharedPreference", 0)
        val json = sharedPreference?.getString("rssList", null)
        if (json.isNullOrEmpty()) return emptyList()

        val gson = Gson()
        val type = object : TypeToken<List<RSS>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun saveRSSListToSharedPreference(rssList: List<RSS>) {
        val gson = Gson()
        val json = gson.toJson(rssList)
        val sharedPreference = activity?.getSharedPreferences("sharedPreference", Context.MODE_PRIVATE)
        val editor = sharedPreference?.edit()
        editor?.putString("rssList", json)
        editor?.apply()
    }


    private fun readJsonFileVartotojai(resources: Resources, rssList: MutableList<RSS>) {
        val jsonFile = resources.openRawResource(R.raw.vartotojai).bufferedReader().use {
            it.readText()
        }

        val jsonArray = JSONArray(jsonFile)
        val deletedMacs = readDeletedMacs() // Read the list of deleted MAC addresses

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            if (jsonObject.getString("type") == "table" && jsonObject.getString("name") == "vartotojai") {
                val data = jsonObject.getJSONArray("data")
                var j = 0
                while (j < data.length()) {
                    try {
                        val first = data.getJSONObject(j)
                        val second = data.getJSONObject(j + 1)
                        val third = data.getJSONObject(j + 2)

                        val mac = first.getString("mac")
                        if (!deletedMacs.contains(mac)) { // Check if the MAC address is not in the deleted list
                            val s1 = first.getInt("stiprumas")
                            val s2 = second.getInt("stiprumas")
                            val s3 = third.getInt("stiprumas")

                            val existingRSS = rssList.find { it.macAddress == mac }
                            if (existingRSS == null) {
                                rssList.add(RSS(mac, s1, s2, s3))
                            }
                        }

                        j += 3
                    } catch (e: JSONException) {
                        Log.e("JSONError", "Error reading JSON: ${e.message}")
                        break
                    }
                }
            }
        }
    }

    private fun saveDeletedMacs(deletedMacs: Set<String>) {
        val gson = Gson()
        val json = gson.toJson(deletedMacs)
        val sharedPreference = activity?.getSharedPreferences("sharedPreference", Context.MODE_PRIVATE)
        val editor = sharedPreference?.edit()
        editor?.putString("deletedMacs", json)
        editor?.apply()
    }

    private fun readDeletedMacs(): Set<String> {
        val sharedPreference = activity?.getSharedPreferences("sharedPreference", 0)
        val json = sharedPreference?.getString("deletedMacs", null) ?: return emptySet()

        val gson = Gson()
        val type = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(json, type) ?: emptySet()
    }
}