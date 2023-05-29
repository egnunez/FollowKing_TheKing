package com.jen.theking.models

data class Driver (
    val id: String? = null,
    val name: String? = null,
    val lastname: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val image: String? = null,
    val plateNumber: String? = null,
    val colorCar: String? = null,
    val brandCar: String? = null
) {

    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Driver>(json)
    }
}