package pm.dibook

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var txtEmail: TextInputEditText
    private lateinit var txtPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegistar: Button
    private lateinit var checkGuardar: CheckBox
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var requestQueue: RequestQueue

    companion object {
        private const val LOGIN_URL = "https://esan-tesp-ds-paw.web.ua.pt/tesp-ds-g29/projeto/mobile/login_mobile.php"
        private const val REGISTER_URL = "https://esan-tesp-ds-paw.web.ua.pt/tesp-ds-g29/projeto/index.php"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtEmail = findViewById(R.id.txtEmail)
        txtPassword = findViewById(R.id.txtPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegistar = findViewById(R.id.btnRegistar)
        checkGuardar = findViewById(R.id.checkGuardar)

        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        requestQueue = Volley.newRequestQueue(this)

        verificarLoginGuardado()

        btnLogin.setOnClickListener {
            val email = txtEmail.text.toString().trim()
            val password = txtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            } else {
                fazerLogin(email, password)
            }
        }

        btnRegistar.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(REGISTER_URL))
            startActivity(browserIntent)
        }
    }

    private fun verificarLoginGuardado() {
        val loginGuardado = sharedPreferences.getBoolean("loginGuardado", false)

        if (loginGuardado) {
            val emailGuardado = sharedPreferences.getString("email", "")
            val passwordGuardada = sharedPreferences.getString("password", "")

            txtEmail.setText(emailGuardado)
            txtPassword.setText(passwordGuardada)
            checkGuardar.isChecked = true
        }
    }

    private fun fazerLogin(email: String, password: String) {
        val stringRequest = object : StringRequest(
            Request.Method.POST, LOGIN_URL,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.getBoolean("success")
                    val message = jsonResponse.getString("message")

                    if (success) {
                        // Verificar role do utilizador
                        if (jsonResponse.has("user")) {
                            val user = jsonResponse.getJSONObject("user")
                            val role = user.getString("role")

                            //   Apenas o cliente pode utilizar
                            if (role == "admin" || role == "funcionario") {
                                Toast.makeText(
                                    this,
                                    "️ Esta aplicação é apenas para clientes.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                val editor = sharedPreferences.edit()

                                if (checkGuardar.isChecked) {
                                    editor.putBoolean("loginGuardado", true)
                                    editor.putString("email", email)
                                    editor.putString("password", password)
                                } else {
                                    editor.putBoolean("loginGuardado", false)
                                    editor.remove("email")
                                    editor.remove("password")
                                }

                                editor.putBoolean("isLoggedIn", true)
                                editor.putString("userId", user.getString("id"))
                                editor.putString("userName", user.getString("name"))
                                editor.putString("userRole", role)
                                editor.apply()

                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                                // Ir  HomeActivity
                                val intent = Intent(this, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.processing_error), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, getString(R.string.processing_error), Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(this, getString(R.string.connection_error, error.message), Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["password"] = password
                return params
            }
        }

        requestQueue.add(stringRequest)
    }
}