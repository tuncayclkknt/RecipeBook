package com.tuncay.recipebook.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Recipe (

    @ColumnInfo(name = "Name")
    val name: String,

    @ColumnInfo(name = "Ingredients")
    val ingredients: String,

    @ColumnInfo(name = "Image")
    val image: ByteArray

){
    @PrimaryKey(autoGenerate = true)
    var id = 0
}
