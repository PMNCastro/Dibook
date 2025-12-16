package pm.dibook

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class HomeFragment : Fragment() {

    private lateinit var txtNome: TextView
    private lateinit var txtEmprestimosAtivos: TextView
    private lateinit var txtEmprestimosDevolvidos: TextView
    private lateinit var txtMultas: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var requestQueue: RequestQueue

    companion object {
        private const val USER_DATA_URL = "https://esan-tesp-ds-paw.web.ua.pt/tesp-ds-g29/projeto/mobile/getUserData.php"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Inicializar componentes
        txtNome = view.findViewById(R.id.txtNome)
        txtEmprestimosAtivos = view.findViewById(R.id.txtEmprestimosAtivos)
        txtEmprestimosDevolvidos = view.findViewById(R.id.txtEmprestimosDevolvidos)
        txtMultas = view.findViewById(R.id.txtMultas)

        sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        requestQueue = Volley.newRequestQueue(requireContext())

        // Carregar dados do utilizador
        carregarDadosUtilizador()

        return view
    }

    private fun carregarDadosUtilizador() {
        val userId = sharedPreferences.getString("userId", "")
        val userName = sharedPreferences.getString("userName", getString(R.string.welcome))

        // Mostrar nome imediatamente
        txtNome.text = getString(R.string.welcome_user, userName)

        if (userId.isNullOrEmpty()) {
            Toast.makeText(context, getString(R.string.user_id_not_found), Toast.LENGTH_SHORT).show()
            return
        }

        // Buscar dados do servidor
        val url = "$USER_DATA_URL?user_id=$userId"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val emprestimosAtivos = response.getInt("emprestimos_ativos")
                    val emprestimosDevolvidos = response.getInt("emprestimos_devolvidos")
                    val multasPendentes = response.getString("multas_pendentes")

                    txtEmprestimosAtivos.text = getString(R.string.borrowed_books, emprestimosAtivos)
                    txtEmprestimosDevolvidos.text = getString(R.string.returned_books, emprestimosDevolvidos)
                    txtMultas.text = getString(R.string.pending_fines, multasPendentes)

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, getString(R.string.error_processing_data), Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // Mostrar valores padrão em caso de erro
                txtEmprestimosAtivos.text = getString(R.string.borrowed_books, 0)
                txtEmprestimosDevolvidos.text = getString(R.string.returned_books, 0)
                txtMultas.text = getString(R.string.pending_fines, "0.00")

                Toast.makeText(context, getString(R.string.error_loading_data), Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}