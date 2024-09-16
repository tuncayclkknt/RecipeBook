package com.tuncay.recipebook.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.tuncay.recipebook.databinding.FragmentRecipeBinding
import com.tuncay.recipebook.model.Recipe
import com.tuncay.recipebook.roomdb.RecipeDao
import com.tuncay.recipebook.roomdb.RecipeDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException

class RecipeFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private val mDisposable = CompositeDisposable()

    private var selectedImage: Uri? = null
    private var selectedBitmap: Bitmap? = null

    private lateinit var db: RecipeDatabase
    private lateinit var recipeDao: RecipeDao

    private var selectedRecipe : Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        db = Room.databaseBuilder(requireContext(),RecipeDatabase::class.java,"Recipes")
            //.allowMainThreadQueries()
            .build()
        recipeDao = db.recipeDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSave.setOnClickListener { save(it) }
        binding.btnDelete.setOnClickListener { delete(it) }
        binding.imageView.setOnClickListener{ selectImage(it)}

        arguments?.let {
            val info = RecipeFragmentArgs.fromBundle(it).info

            if (info){
                //Added new item
                binding.btnDelete.isEnabled = false
                binding.btnSave.isEnabled = true
                binding.recipeText.setText("")
                binding.nameText.setText("")
            }
            else{
                //Showing clicked item
                binding.btnDelete.isEnabled = true
                binding.btnSave.isEnabled = false
                val id = RecipeFragmentArgs.fromBundle(it).id

                mDisposable.add(
                    recipeDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)
                )

            }
        }
    }

    private fun handleResponse(recipe: Recipe){
        val bitmap = BitmapFactory.decodeByteArray(recipe.image,0,recipe.image.size)
        binding.imageView.setImageBitmap(bitmap)
        binding.nameText.setText(recipe.name)
        binding.recipeText.setText(recipe.ingredients)
        selectedRecipe = recipe
    }

    private fun selectImage(view: View) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED){
                //permission denied, ask for a perm
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                    //snacbar , why do we need this perm? Explain.
                    Snackbar.make(view, "We need a photo from your galley.",Snackbar.LENGTH_INDEFINITE).setAction(
                        "Give Permission",
                        View.OnClickListener {

                            //asking perm
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    ).show()
                }else{
                    // ask for perm
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                }
            }else{
                //permission allowed
                val intentToGalley = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalley)

            }

        }else {

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                //permission denied, ask for a perm
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    //snacbar , why do we need this perm? Explain.
                    Snackbar.make(
                        view,
                        "We need a photo from your galley.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(
                        "Give Permission",
                        View.OnClickListener {

                            //asking perm
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    ).show()

                } else {
                    // ask for perm
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                }
            } else {
                //permission allowed
                val intentToGalley =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalley)

            }
        }
    }

    fun save(view: View) {

        val name = binding.nameText.text.toString()
        val ingredients = binding.recipeText.text.toString()

        if (selectedBitmap != null){
            val tinyBitmap = tinyBitmapCreater(selectedBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            tinyBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            val recipe = Recipe(name, ingredients, byteArray)

            //RxJava

            mDisposable.add(
                recipeDao.insert(recipe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponseForInsert))
        }

    }

    private fun handleResponseForInsert(){
        //back to previous page
        val action = RecipeFragmentDirections.actionRecipeFragmentToListFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    fun delete(view: View){

        if (selectedRecipe != null){
            mDisposable.add(
                recipeDao.delete(recipe = selectedRecipe!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)
            )
        }

    }

    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult != null){
                    selectedImage = intentFromResult.data

                    try {
                        if (Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(requireActivity().contentResolver, selectedImage!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitmap)

                        }else {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImage)
                            binding.imageView.setImageBitmap(selectedBitmap)

                        }
                    } catch (e: IOException){
                        println(e.localizedMessage)
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if (result){
                //permission allowed
                val intentToGalley = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGalley)

            }else{
                //permission denied
                Toast.makeText(requireContext(),"Izin Verilmedi", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun tinyBitmapCreater(userSelectedBitmap: Bitmap, maxSize: Int): Bitmap{
        var width = userSelectedBitmap.width
        var height = userSelectedBitmap.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1){
            //image horizontal
            width = maxSize
            val shortedHeight = width / bitmapRatio
            height = shortedHeight.toInt()

        }else{
            //image vertical

            height = maxSize
            val shortedWidth = height * bitmapRatio
            width = shortedWidth.toInt()
        }

        return Bitmap.createScaledBitmap(userSelectedBitmap, width, height,true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}