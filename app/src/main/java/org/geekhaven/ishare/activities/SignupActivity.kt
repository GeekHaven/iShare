package org.geekhaven.ishare.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signup.*

import org.geekhaven.ishare.R


class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance()

        btn_reset_password_signup.setOnClickListener { startActivity(Intent(this@SignupActivity, ResetPasswordActivity::class.java)) }

        sign_in_button.setOnClickListener { finish() }

        sign_up_button.setOnClickListener(View.OnClickListener {
            val email = email_signup.text.toString().trim { it <= ' ' }
            val password = password_signup.text.toString().trim { it <= ' ' }

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(applicationContext, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            progressBar_signup.visibility = View.VISIBLE
            //create user
            val emailCorrected= "$email@iiita.ac.in"
            auth.createUserWithEmailAndPassword(emailCorrected, password)
                    .addOnCompleteListener(this@SignupActivity) { task ->
                        Toast.makeText(this@SignupActivity, "createUserWithEmail:onComplete:" + task.isSuccessful, Toast.LENGTH_SHORT).show()
                        progressBar_signup.visibility = View.GONE
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful) {
                            Toast.makeText(this@SignupActivity, "Authentication failed." + task.exception!!,
                                    Toast.LENGTH_SHORT).show()
                        } else {
                            startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                            finish()
                        }
                    }

        })
    }

    override fun onResume() {
        super.onResume()
        progressBar_signup.visibility = View.GONE
    }
}

