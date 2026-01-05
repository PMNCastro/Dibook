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
        // Criar requisição periódica, verifica a cada 30 minutos
        val notificationWork = PeriodicWorkRequestBuilder<NotificationWorker>(
            30, TimeUnit.MINUTES // Intervalo de verificação
        ).build()

        // Agendar o trabalho
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "FavoritesNotification",
            ExistingPeriodicWorkPolicy.KEEP,
            notificationWork
        )
    }

    private fun fazerLogout() {
        // Limpar apenas o estado de login, mas manter credenciais se o Guardar estiver marcado
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.remove("userId")
        editor.remove("userName")
        editor.apply()

        // Cancelar notificações ao fazer logout, mas app minimizada nao cancela nots
        WorkManager.getInstance(this).cancelUniqueWork("FavoritesNotification")

        // Voltar para MainActivity
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