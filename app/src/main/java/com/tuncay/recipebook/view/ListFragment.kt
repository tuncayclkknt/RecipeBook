package com.tuncay.recipebook.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.tuncay.recipebook.adapter.RecipeAdapter
import com.tuncay.recipebook.databinding.FragmentListBinding
import com.tuncay.recipebook.model.Recipe
import com.tuncay.recipebook.roomdb.RecipeDao
import com.tuncay.recipebook.roomdb.RecipeDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: RecipeDatabase
    private lateinit var recipeDao: RecipeDao
    private val mDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(requireContext(),RecipeDatabase::class.java,"Recipes").build()
        recipeDao = db.recipeDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener { addNew(it) }
        binding.recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        getDatas()
    }

    private fun getDatas(){
        mDisposable.add(
            recipeDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }

    private fun handleResponse(recipes : List<Recipe>){
        val adapter = RecipeAdapter(recipes)
        binding.recipeRecyclerView.adapter = adapter
    }

    private fun addNew(view: View) {
        val action = ListFragmentDirections.actionListFragmentToRecipeFragment(id = 0, info = true)
        Navigation.findNavController(view).navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}