package pm.dibook

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        private const val CHECK_FAVORITES_URL = "https://esan-tesp-ds-paw.web.ua.pt/tesp-ds-g29/projeto/mobile/checkFavorites.php"
        private const val CHANNEL_ID = "dibook_notifications"
        private const val CHANNEL_NAME = "Notificações de Livros"
    }

    override fun doWork(): Result {
        val sharedPreferences = applicationContext.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val userId = sharedPreferences.getString("userId", "")

        if (!isLoggedIn || userId.isNullOrEmpty()) {
            return Result.success()
        }

        createNotificationChannel()

        return verificarFavoritos(userId)
    }

    private fun verificarFavoritos(userId: String): Result {
        val latch = CountDownLatch(1)
        var result = Result.success()

        val requestQueue = Volley.newRequestQueue(applicationContext)
        val url = "$CHECK_FAVORITES_URL?user_id=$userId"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                processNotifications(response)
                latch.countDown()
            },
            { error ->
                result = Result.retry()
                latch.countDown()
            }
        )

        requestQueue.add(jsonArrayRequest)

// 30 segundos de espera
        try {
            latch.await(30, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            return Result.retry()
        }

        return result
    }

    private fun processNotifications(response: JSONArray) {
        try {
            for (i in 0 until response.length()) {
                val item = response.getJSONObject(i)
                val livroId = item.getInt("livro_id")
                val livroNome = item.getString("titulo")
                val quantidadeDisponivel = item.getInt("quantidade_disponivel")

                val prefs = applicationContext.getSharedPreferences("FavoritesCache", Context.MODE_PRIVATE)
                val lastQuantity = prefs.getInt("livro_$livroId", -1)

                if (lastQuantity == -1) {
                    saveLastQuantity(livroId, quantidadeDisponivel)
                    continue
                }

                if (lastQuantity != quantidadeDisponivel) {
                    if (lastQuantity == 0 && quantidadeDisponivel > 0) {
                        showNotification(
                            "Livro Disponível! 📚",
                            "Encontra-se disponível $quantidadeDisponivel cópia(s) do $livroNome, para empréstimo",
                            livroId
                        )
                    } else if (lastQuantity > 0 && quantidadeDisponivel == 0) {
                        showNotification(
                            "Livro Indisponível",
                            "O $livroNome, que tem nos favoritos, encontra-se indisponível de momento",
                            livroId
                        )
                    }

                    saveLastQuantity(livroId, quantidadeDisponivel)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações sobre disponibilidade dos livros favoritos"
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String, livroId: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(livroId, notification)
    }

    private fun saveLastQuantity(livroId: Int, quantidade: Int) {
        val prefs = applicationContext.getSharedPreferences("FavoritesCache", Context.MODE_PRIVATE)
        prefs.edit().putInt("livro_$livroId", quantidade).apply()
    }
}