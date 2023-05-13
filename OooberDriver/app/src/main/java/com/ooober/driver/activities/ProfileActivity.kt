package com.ooober.driver.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.ooober.driver.R
import com.ooober.driver.databinding.ActivityProfileBinding
import com.ooober.driver.models.Driver
import com.ooober.driver.providers.AuthProvider
import com.ooober.driver.providers.DriverProvider
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()

    private var imageFile: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        binding.ivBack.setOnClickListener{
            finish()
        }
        getDriver()
        binding.ivBack.setOnClickListener { finish() }
        binding.btnUpdate.setOnClickListener { updateInfo() }
        binding.circleImageProfile.setOnClickListener{selectImage()}
        binding.btnVerifyEmail.setOnClickListener { verifyUserEmail() }

    }

    private fun updateInfo(){
        val name = binding.tfName.text.toString()
        val lastname = binding.tflastname.text.toString()
        val phone = binding.tfPhone.text.toString()
        val carBrand = binding.tfCarBrand.text.toString()
        val carColor = binding.tfCarColor.text.toString()
        val carPlate = binding.tfCarPlate.text.toString()

        val driver = Driver(
            id = authProvider.getId(),
            name = name,
            lastname = lastname,
            phone = phone,
            colorCar = carColor,
            brandCar = carBrand,
            plateNumber = carPlate,
        )

        if(imageFile != null){
            driverProvider.uploadImage(authProvider.getId(), imageFile!!).addOnSuccessListener { taskSnapshot ->
                driverProvider.getImageUrl().addOnSuccessListener { url ->
                    val imageUrl = url.toString()
                    driver.image = imageUrl
                    driverProvider.update(driver).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this@ProfileActivity,  R.string.txtToastUpdatedData, Toast.LENGTH_LONG).show()
                        }
                        else {
                            Toast.makeText(this@ProfileActivity, R.string.txtToastFailedToUpdateInformation, Toast.LENGTH_LONG).show()
                        }
                    }
                    Log.d("STORAGE", "$imageUrl")
                }
            }
        }
        else{
            driverProvider.updateWithOutImage(driver).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this@ProfileActivity,  R.string.txtToastUpdatedData, Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(this@ProfileActivity, R.string.txtToastFailedToUpdateInformation, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun getDriver(){
        driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
            if(document.exists()){
                val driver = document.toObject(Driver::class.java)
                binding.tvEmail.text = "${(driver?.email)?:""}"
                binding.tfName.setText(driver?.name)
                binding.tflastname.setText(driver?.lastname)
                binding.tfPhone.setText(driver?.phone)
                binding.tfCarBrand.setText(driver?.brandCar)
                binding.tfCarColor.setText(driver?.colorCar)
                binding.tfCarPlate.setText(driver?.plateNumber)

                if(driver?.image != null){
                    if(driver?.image != ""){
                        Glide.with(this).load(driver?.image).into(binding.circleImageProfile)
                    }
                }
            }
        }
    }

    private val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if(resultCode == Activity.RESULT_OK) {
            val fileUri = data?.data
            imageFile = File(fileUri?.path)
            binding.circleImageProfile.setImageURI(fileUri)
        }
        else if(resultCode == ImagePicker.RESULT_ERROR ){
           Log.d("ImagePicker","Error:${ImagePicker.getError(data)}")
        }
        else{
            Log.d("ImagePicker","No se seleccionó ninguna imagen")
        }
    }
    private fun selectImage(){
        ImagePicker.with(this)
            .crop().compress(1024).maxResultSize(1080,1080).createIntent { intent->
                startImageForResult.launch(intent)
            }
    }

    private fun verifyUserEmail(){
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val userEmail = user?.email

        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "${resources.getString(R.string.txt_resetPassMailSent)} $userEmail", Toast.LENGTH_LONG).show()
                }
                else{
                    Toast.makeText(applicationContext, "Error",Toast.LENGTH_LONG).show()
                }
            }
    }


}