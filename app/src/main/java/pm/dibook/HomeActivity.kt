package pm.dibook

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.concurrent.TimeUnit

class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Carregar HomeFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
            bottomNavigation.selectedItemId = R.id.nav_home
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null

            when (item.itemId) {
                R.id.nav_home -> selectedFragment = HomeFragment()
                R.id.nav_pesquisar -> selectedFragment = PesquisarFragment()
                R.id.nav_favoritos -> selectedFragment = FavoritosFragment()
                R.id.nav_logout -> {
                    fazerLogout()
                    return@setOnItemSelectedListener true
                }
            }

            selectedFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, it)
                    .commit()
            }

            true
        }

        // Iniciar notificações dos favoritos
        iniciarNotificacoes()
    }

    private fun iniciarNotificacoes() {
        // Criar requisição periódica, a cada 30 minutos, poderia ter reduzido para 15 mas faria o utilizador gastar mais bateria
        val notificationWork = PeriodicWorkRequestBuilder<NotificationWorker>(
            30, TimeUnit.MINUTES // Intervalo de verificação
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "FavoritesNotification",
            ExistingPeriodicWorkPolicy.KEEP,
            notificationWork
        )
    }

    //Confirmar logout
    private fun fazerLogout() {
        val builder1 = android.app.AlertDialog.Builder(this)
        builder1.setTitle("Terminar Sessão")
        builder1.setMessage("Tem a certeza que deseja sair?")

        builder1.setNegativeButton("Sim") { _, _ ->
            // Confirmou logout
            mostrarDialogoLimparDados()
        }

        builder1.setPositiveButton("Não", null)  // Cancela

        builder1.show()
    }

    // Perguntar se quer limpar dados guardados
    private fun mostrarDialogoLimparDados() {
        val builder2 = android.app.AlertDialog.Builder(this)
        builder2.setTitle("Eliminar Dados Guardados?")
        builder2.setMessage("Pretende eliminar email e password da sessão?")

        // sim,Limpa tudo
        builder2.setNegativeButton("Sim") { _, _ ->
            executarLogout(limparCredenciais = true)
        }

        // nao, mantem dados
        builder2.setPositiveButton("Não") { _, _ ->
            executarLogout(limparCredenciais = false)
        }

        builder2.show()
    }

    // Executar logout com ou sem limpeza de credenciais
    private fun executarLogout(limparCredenciais: Boolean) {
        val editor = sharedPreferences.edit()

        // Sempre limpar dados da sessão
        editor.putBoolean("isLoggedIn", false)
        editor.remove("userId")
        editor.remove("userName")

        // Limpar credenciais apenas se solicitado
        if (limparCredenciais) {
            editor.remove("email")
            editor.remove("password")
            editor.remove("rememberMe")
        }

        editor.apply()

        // Cancelar notificações ao fazer logout, mas app minimizada nao cancela nots(mostrar ao professor isto na segunda)
        WorkManager.getInstance(this).cancelUniqueWork("FavoritesNotification")

        // Volta MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()

        // Verificar se o utilizador ainda está com login
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (!isLoggedIn) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}