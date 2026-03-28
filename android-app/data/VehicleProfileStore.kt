package com.andreaplloci.thesisobdapp.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class VehicleProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences("jarvis_profiles", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val KEY = "profiles"

    fun getAll(): List<VehicleProfile> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<VehicleProfile>>() {}.type)
        } catch (e: Exception) { emptyList() }
    }

    fun save(profile: VehicleProfile) {
        val list = getAll().toMutableList()
        val i = list.indexOfFirst { sameVehicle(it, profile) }
        if (i >= 0) list[i] = profile else list.add(0, profile)
        prefs.edit().putString(KEY, gson.toJson(list)).apply()
    }

    fun delete(profile: VehicleProfile) {
        val list = getAll().filterNot { sameVehicle(it, profile) }
        prefs.edit().putString(KEY, gson.toJson(list)).apply()
    }

    private fun sameVehicle(a: VehicleProfile, b: VehicleProfile) =
        a.marca == b.marca && a.modello == b.modello && a.anno == b.anno && a.motore == b.motore
}
