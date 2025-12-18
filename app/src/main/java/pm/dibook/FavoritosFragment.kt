package pm.dibook

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class FavoritosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var txtEmpty: TextView
    private lateinit var adapter: LivroAdapter
    private val favoritosList = ArrayList<Livro>()
    private lateinit var requestQueue: RequestQueue
    private var userId: String? = null

    companion object {
        private const val FAVORITOS_URL = "https://esan-tesp-ds-paw.web.ua.pt/tesp-ds-g29/projeto/mobile/favoritos.php"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favoritos, container, false)

        // Inicializar componentes
        recyclerView = view.findViewById(R.id.recyclerViewFavoritos)
        txtEmpty = view.findViewById(R.id.txtFavoritos)

        recyclerView.layoutManager = LinearLayoutManager(context)

        // Obter userId
        val sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "")

        // Adapter com callback de remover favorito
        adapter = LivroAdapter(requireContext(), favoritosList) { livro, position ->
            removerFavorito(livro, position)
        }
        recyclerView.adapter = adapter

        requestQueue = Volley.newRequestQueue(requireContext())

        // Carregar favoritos
        carregarFavoritos()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Recarregar favoritos quando voltar ao fragment
        carregarFavoritos()
    }

    private fun carregarFavoritos() {
        if (userId.isNullOrEmpty()) {
            Toast.makeText(context, "Erro ao obter utilizador", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "$FAVORITOS_URL?user_id=$userId"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    favoritosList.clear()

                    for (i in 0 until response.length()) {
                        val jsonObject = response.getJSONObject(i)

                        val livro = Livro(
                            id = jsonObject.getInt("id"),
                            titulo = jsonObject.getString("titulo"),
                            autores = jsonObject.getString("autores"),
                            editora = jsonObject.optString("editora", ""),
                            isbn = jsonObject.optString("isbn", ""),
                            categoria = jsonObject.optString("categoria", ""),
                            descricao = jsonObject.optString("descricao", ""),
                            quantidade_disponivel = jsonObject.getInt("quantidade_disponivel"),
                            disponivel = jsonObject.getBoolean("disponivel"),
                            is_favorite = true
                        )

                        favoritosList.add(livro)
                    }

                    adapter.notifyDataSetChanged()

                    // Mostrar/ocultar mensagem vazia
                    if (favoritosList.isEmpty()) {
                        recyclerView.visibility = View.GONE
                        txtEmpty.visibility = View.VISIBLE
                        txtEmpty.text = "⭐ Ainda não tens favoritos!\n\nAdiciona livros aos favoritos na página Pesquisar."
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        txtEmpty.visibility = View.GONE
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Erro ao carregar favoritos", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(context, "Erro ao carregar favoritos", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    private fun removerFavorito(livro: Livro, position: Int) {
        if (userId.isNullOrEmpty()) return

        val stringRequest = object : StringRequest(
            Request.Method.POST,
            FAVORITOS_URL,
            { response ->
                try {
                    val jsonResponse = org.json.JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        favoritosList.removeAt(position)
                        adapter.notifyItemRemoved(position)

                        Toast.makeText(context, "Removido dos favoritos", Toast.LENGTH_SHORT).show()

                        // Atualizar visibilidade se ficar vazio
                        if (favoritosList.isEmpty()) {
                            recyclerView.visibility = View.GONE
                            txtEmpty.visibility = View.VISIBLE
                            txtEmpty.text = "⭐ Ainda não tens favoritos!"
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(context, "Erro ao remover favorito", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return hashMapOf(
                    "user_id" to userId!!,
                    "livro_id" to livro.id.toString(),
                    "action" to "remove"
                )
            }
        }

        requestQueue.add(stringRequest)
    }
}