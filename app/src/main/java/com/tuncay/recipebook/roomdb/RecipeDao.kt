package com.tuncay.recipebook.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.tuncay.recipebook.model.Recipe
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface RecipeDao {

    @Query("SELECT * FROM Recipe")
    fun getAll() : Flowable<List<Recipe>>

    @Query("SELECT * FROM Recipe WHERE id = :id")
    fun findById(id: Int) : Flowable<Recipe>

    @Insert
    fun insert(recipe: Recipe) : Completable

    @Delete
    fun delete(recipe: Recipe) : Completable
}