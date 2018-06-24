package org.geekhaven.ishare.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signin.*
import org.geekhaven.ishare.R

class LoginActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }

        // set the view now
        setContentView(R.layout.activity_signin)
        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance()

        btn_signup.setOnClickListener { startActivity(Intent(this@LoginActivity, SignupActivity::class.java)) }

        btn_reset_password_signin.setOnClickListener { startActivity(Intent(this@LoginActivity, ResetPasswordActivity::class.java)) }

        btn_login.setOnClickListener(View.OnClickListener {
            val emailtext = email_signin.text
            val passwordtext = password_signin.text.toString()

            if (TextUtils.isEmpty(emailtext)) {
                Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            if (TextUtils.isEmpty(emailtext)) {
                Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            progressBar_signin.visibility = View.VISIBLE
            val emailCorrected= "$emailtext@iiita.ac.in"
            //authenticate user
            auth.signInWithEmailAndPassword(emailCorrected, passwordtext)
                    .addOnCompleteListener(this@LoginActivity) { task ->
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        progressBar_signin.visibility = View.GONE
                        if (!task.isSuccessful) {
                            // there was an error
                            if (passwordtext.length < 6) {
                                password_signin.error = getString(R.string.minimum_password)
                            } else {
                                Toast.makeText(this@LoginActivity, getString(R.string.auth_failed), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
        })
    }
}