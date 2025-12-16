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
import org.json.JSONObject

class HomeFragment : Fragment() {

    private lateinit var txtNome: TextView
    private lateinit var txtEmprestimosAtivos: TextView
    private lateinit var txtEmprestimosDevolvidos: TextView
    private lateinit var txtMultas: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var requestQueue: RequestQueue

    companion object {
        private const val USER_DATA_URL = "https://esan-tesp-ds-paw.web.ua.pt/tesp-ds-g29/mobile/getUserData.php"
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
        val userName = sharedPreferences.getString("userName", "Utilizador")

        // Mostrar nome imediatamente
        txtNome.text = "Bem-vindo, $userName!"

        if (userId.isNullOrEmpty()) {
            Toast.makeText(context, "Erro: ID do utilizador não encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        // vai buscar os dados do servidor
        val url = "$USER_DATA_URL?user_id=$userId"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val emprestimosAtivos = response.getInt("emprestimos_ativos")
                    val emprestimosDevolvidos = response.getInt("emprestimos_devolvidos")
                    val multasPendentes = response.getString("multas_pendentes")

                    txtEmprestimosAtivos.text = "Livros Emprestados: $emprestimosAtivos"
                    txtEmprestimosDevolvidos.text = "Livros Devolvidos: $emprestimosDevolvidos"
                    txtMultas.text = "Multas Pendentes: €$multasPendentes"

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Erro ao processar dados", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // Mostrar valores padrão em caso de erro
                txtEmprestimosAtivos.text = "Livros Emprestados: 0"
                txtEmprestimosDevolvidos.text = "Livros Devolvidos: 0"
                txtMultas.text = "Multas Pendentes: €0.00"

                Toast.makeText(context, "Erro ao carregar dados: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}