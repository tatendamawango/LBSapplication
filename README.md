# 📍 Wireless Network User Location – Android App

This is a location-based Android application developed for the **Wireless Network User Location Study** lab at Kaunas University of Technology. It uses Wi-Fi signal strengths from access points (APs) to determine a user’s location using the **K-Nearest Neighbor (KNN)** algorithm.

---

## 📲 Features

- ✅ **MAC Address Identification** with auto-formatting
- ✅ **RSS Data Entry and Validation** (S1, S2, S3)
- ✅ **RecyclerView List of Signal Strengths**
- ✅ **Signal Matching to Regions**
- ✅ **Location Calculation using KNN**
- ✅ **Material Design UI**
- ✅ **Persistent Storage using Room Database**
- ✅ **Shared Preferences for Session Data**
- ✅ **Remote Map Cache Integration via REST API**


---

## 🛠 Technologies Used

| Layer         | Library / Tool         |
|---------------|------------------------|
| UI            | Material Design, RecyclerView, View Binding |
| State Mgmt    | ViewModel, LiveData    |
| Storage       | Room Database, SharedPreferences |
| Remote Access | Retrofit, GSON         |
| Programming   | Kotlin (Android SDK)   |

---

## 📡 How It Works

1. User enters a **MAC address** and 3 **RSS values**.
2. Data is stored locally and displayed in a RecyclerView.
3. App pulls a signal strength map from a remote database or cache.
4. KNN algorithm is applied to **determine user location**.
5. Location is visually marked on a grid map.

---

## 🧪 Objectives

- Connect to and parse signal map data from a MySQL database.
- Calculate user location using Nearest Neighbor algorithm.
- Visualize measured and matched grid positions.
- Enable user-generated location queries with dynamic RSS inputs.

---

## 💡 Example Code Highlights

### MAC Address Formatting (Auto-complete)
```kotlin
binding.macAddressInput.addTextChangedListener(object : TextWatcher {
    // Auto-inserts ":" after every 2 characters
})
```

### SharedPreferences for Session Persistence
```kotlin
val sharedPreference = activity?.getSharedPreferences("sharedPreference", 0)
editor?.putString("rssList", json)
```

### RecyclerView Adapter for RSS Entries
```kotlin
class RSSAdapter(...) : RecyclerView.Adapter<...> { ... }
```
