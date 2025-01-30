package com.example.firebaseauth

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.firebaseauth.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        binding.btnRegister.setOnClickListener { registration() }
        binding.btnLogin.setOnClickListener { login() }
        binding.btnUploadImage.setOnClickListener { updateUserProfile() }

    }

    override fun onStart() {
        super.onStart()
        checkRegister()
    }

    private fun registration() {
        val email = binding.edtSingUpEmail.text.toString()
        val password = binding.edtSingUpPassword.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try{
                    auth.createUserWithEmailAndPassword(email, password).await()
                    CoroutineScope(Dispatchers.Main).launch {
                        checkRegister()
                    }
                } catch (e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@MainActivity, e.message,Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun login(){
        val email = binding.edtLoginEmail.text.toString()
        val password = binding.edtLoginPassword.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.signInWithEmailAndPassword(email, password).await()
                    CoroutineScope(Dispatchers.Main).launch {
                        checkRegister()
                    }
                } catch (e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@MainActivity, e.message,Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateUserProfile(){
        auth.currentUser?.let {
            val name = binding.edtUserName.text.toString()
            val profile = Uri.parse("android.resource://$packageName/${R.drawable.pelple2}")
            val updateUserProfile = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(profile)
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    it.updateProfile(updateUserProfile).await()
                    checkRegister()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Successfully updated user profile", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@MainActivity, e.message,Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkRegister() {
        val user = auth.currentUser
        if(user == null){
            binding.txvStatus.text = "You're not login!!"
        } else {
            binding.txvStatus.text = "Login Successful"
            binding.edtUserName.setText(user.displayName)
            binding.imvUserProfile.setImageURI(user.photoUrl)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}