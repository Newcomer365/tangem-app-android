package com.tangem.domain.onramp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@Serializable
@JsonClass(generateAdapter = true)
data class OnrampCurrency(
    @Json(name = "name")
    val name: String,
    @Json(name = "code")
    val code: String,
    @Json(name = "image")
    val image: String?,
    @Json(name = "precision")
    val precision: Int,
    @Json(name = "unit")
    val unit: String,
)
