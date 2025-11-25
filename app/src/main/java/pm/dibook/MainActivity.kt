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
import kotlin.jvm.java

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

        // Inicializar componentes
        txtEmail = findViewById(R.id.Email)
        txtPassword = findViewById(R.id.password)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegistar = findViewById(R.id.btnRegistar)
        checkGuardar = findViewById(R.id.checkGuardar)

        // SharedPreferences para guardar login
        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        requestQueue = Volley.newRequestQueue(this)

        // Verificar se há login guardado 7
        verificarLoginGuardado()

        // Botão de Login novo
        btnLogin.setOnClickListener {
            val email = txtEmail.text.toString().trim()
            val password = txtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                fazerLogin(email, password)
            }
        }

        // Botão de Registar, envia para o URL de registo, webview https://www.youtube.com/watch?v=hyzdE_0WoDE
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

            // Pode fazer login automático se usar o guardar
            // fazerLogin(emailGuardado ?: "", passwordGuardada ?: "")
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
                        // Guardar login se checkbox estiver marcado
                        val editor = sharedPreferences.edit()
                        if (checkGuardar.isChecked) {
                            editor.putBoolean("loginGuardado", true)
                            editor.putString("email", email)
                            editor.putString("password", password)
                        } else {
                            editor.clear()
                        }

                        // Guardar dados do utilizador
                        if (jsonResponse.has("user")) {
                            val user = jsonResponse.getJSONObject("user")
                            editor.putBoolean("isLoggedIn", true)
                            editor.putString("userId", user.getString("id"))
                            editor.putString("userName", user.getString("name"))
                        }
                        editor.apply()

                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                        // Ir para HomeActivity
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao processar resposta", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(this, "Erro de conexão: ${error.message}", Toast.LENGTH_SHORT).show()
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