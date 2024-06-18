package com.app.traveldiary.interfaces

import com.app.traveldiary.models.PopularModel

interface OnPopularItemClickListener {
    fun ItemClickListener(item : PopularModel)
    fun ZnakomoClickListener(item : PopularModel)
}