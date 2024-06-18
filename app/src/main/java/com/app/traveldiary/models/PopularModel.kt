package com.app.traveldiary.models

data class PopularModel (
    val title : String,
    val desc : String,
    val detailDest : String,
    val image : String,
    var isLiked: Boolean = false
)