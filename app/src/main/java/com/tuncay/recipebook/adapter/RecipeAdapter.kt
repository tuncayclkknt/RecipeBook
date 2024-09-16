package com.tuncay.recipebook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.tuncay.recipebook.databinding.FragmentRecipeBinding
import com.tuncay.recipebook.databinding.RecyclerRowBinding
import com.tuncay.recipebook.model.Recipe
import com.tuncay.recipebook.view.ListFragmentDirections

class RecipeAdapter(val recipeList: List<Recipe>) : RecyclerView.Adapter<RecipeAdapter.RecipeHolder>() {

    class RecipeHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return RecipeHolder(recyclerRowBinding)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    override fun onBindViewHolder(holder: RecipeHolder, position: Int) {

        holder.binding.recyclerViewText.text = recipeList[position].name
        holder.itemView.setOnClickListener {
            val aciton = ListFragmentDirections.actionListFragmentToRecipeFragment(recipeList[position].id,false)
            Navigation.findNavController(it).navigate(aciton)
        }


    }
}