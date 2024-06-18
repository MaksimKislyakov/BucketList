package com.app.traveldiary.interfaces

import com.app.traveldiary.models.NotPopularModel

interface OnNotPopularItemClickListener {
    fun ItemClickListener(item : NotPopularModel)
    fun NeZnakomo(item : NotPopularModel)
}