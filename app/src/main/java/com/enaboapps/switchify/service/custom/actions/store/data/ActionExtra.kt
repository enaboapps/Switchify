package com.enaboapps.switchify.service.custom.actions.store.data

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class ActionExtra(
    @SerializedName("app_package") val appPackage: String = "",
    @SerializedName("app_name") val appName: String = "",
    @SerializedName("text_to_copy") val textToCopy: String = "",
    @SerializedName("number_to_call") val numberToCall: String = "",
    @SerializedName("link_url") val linkUrl: String = "",
    @SerializedName("number_to_send") val numberToSend: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("email_address") val emailAddress: String = ""
) {
    companion object {
        fun fromJson(json: String): ActionExtra =
            Gson().fromJson(json, ActionExtra::class.java)
    }

    fun toJson(): String = Gson().toJson(this)
}
