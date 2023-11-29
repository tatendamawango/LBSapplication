package edu.ktu.networks.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.ktu.networks.R
import edu.ktu.networks.databinding.FragmentSecondBinding
import edu.ktu.networks.models.RSS


class SecondFragment : Fragment() {
    private lateinit var binding: FragmentSecondBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSecondBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.macAddressInput.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating)  return

                isUpdating = true

                val formattedMacAddress = formatMacAddress(s.toString())
                binding.macAddressInput.setText(formattedMacAddress)

                binding.macAddressInput.setSelection(formattedMacAddress.length)

                isUpdating = false
            }
        })

        binding.saveButton.setOnClickListener {
            val isValid = validateInput()
            if (isValid) {
                val s1 = binding.s1Input.text.toString().toInt()
                val s2 = binding.s2Input.text.toString().toInt()
                val s3 = binding.s3Input.text.toString().toInt()
                val macAddress = binding.macAddressInput.text.toString()

                val newRSS = RSS(macAddress, s1, s2, s3)
                val existingRSSList = readFromSharedPreference()
                val updateRSSList = existingRSSList.toMutableList()
                updateRSSList.add(newRSS)
                saveToSharedPreference(updateRSSList)
                clearInputs()
            }
        }

    }

    private fun saveToSharedPreference(rssList: List<RSS>) {
        val gson = Gson()
        val json = gson.toJson(rssList)
        val sharedPreference = activity?.getSharedPreferences("sharedPreference", 0)
        val editor = sharedPreference?.edit()
        editor?.putString("rssList", json)
        editor?.apply()
    }

    private fun readFromSharedPreference(): List<RSS> {
        val sharedPreference = activity?.getSharedPreferences("sharedPreference", 0)
        val json = sharedPreference?.getString("rssList", null) ?: return emptyList()

        val gson = Gson()
        val type = object : TypeToken<List<RSS>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun validateInput(): Boolean {

        var isValid = true

        if (binding.macAddressInput.text.isNullOrEmpty()) {
            binding.macAddressInput.error = "Please enter a valid MAC address"
            isValid = false
        }

        if (binding.s1Input.text.isNullOrEmpty()) {
            binding.s1Input.error = "Please enter a valid RSS value"
            isValid = false
        }

        if (binding.s2Input.text.isNullOrEmpty()) {
            binding.s2Input.error = "Please enter a valid RSS value"
            isValid = false
        }

        if (binding.s3Input.text.isNullOrEmpty()) {
            binding.s3Input.error = "Please enter a valid RSS value"
            isValid = false
        }

        if (!validateMacAddress()) {
            binding.macAddressInput.error = "Please enter a valid MAC address"
            isValid = false
        }

        return isValid
    }

    private fun validateMacAddress(): Boolean {
        val macAddress = binding.macAddressInput.text.toString()
        val regex = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$".toRegex()
        return regex.matches(macAddress)
    }

    private fun clearInputs() {
        binding.macAddressInput.text?.clear()
        binding.s1Input.text?.clear()
        binding.s2Input.text?.clear()
        binding.s3Input.text?.clear()
    }

    private fun formatMacAddress(input: String): String {
        val cleaned = input.replace("[:,-]".toRegex(), "")

        val sb = StringBuilder()
        for (i in cleaned.indices) {
            sb.append(cleaned[i])
            if (i % 2 == 1 && i < cleaned.length - 1) {
                sb.append(':')
            }
        }
        return sb.toString()
    }
}