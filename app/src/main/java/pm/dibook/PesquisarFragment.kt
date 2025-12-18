package pm.dibook

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class PesquisarFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LivroAdapter
    private val livrosList = ArrayList<Livro>()
    private val livrosListFull = ArrayList<Livro>()
    private lateinit var requestQueue: RequestQueue
    private var userId: String? = null

    companion object {
        private const val LIVROS_URL = "https://esan-tesp-ds-paw.web.ua.pt/tesp-ds-g29/projeto/mobile/getLivros.php"
        private const val FAVORITOS_URL = "https://esan-tesp-ds-paw.web.ua.pt/tesp-ds-g29/projeto/mobile/favoritos.php"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pesquisar, container, false)

        // Inicializar componentes
        searchView = view.findViewById(R.id.searchView)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Obter userId
        val sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "")

        // Adapter com callback de favoritos
        adapter = LivroAdapter(requireContext(), livrosList) { livro, position ->
            toggleFavorite(livro, position)
        }
        recyclerView.adapter = adapter

        requestQueue = Volley.newRequestQueue(requireContext())

        // Carregar todos os livros inicialmente
        carregarLivros()

        // Configurar pesquisa
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarLivros(newText ?: "")
                return true
            }
        })

        return view
    }

    private fun carregarLivros() {
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            LIVROS_URL,
            null,
            { response ->
                try {
                    livrosList.clear()
                    livrosListFull.clear()

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
                            is_favorite = false  // Será atualizado depois
                        )

                        livrosList.add(livro)
                        livrosListFull.add(livro)
                    }

                    adapter.notifyDataSetChanged()

                    if (livrosList.isEmpty()) {
                        Toast.makeText(context, getString(R.string.no_books_found), Toast.LENGTH_SHORT).show()
                    }

                    // Carregar estado dos favoritos
                    carregarFavoritos()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, getString(R.string.error_processing_data), Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(context, getString(R.string.error_loading_books, error.message ?: ""), Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    private fun carregarFavoritos() {
        if (userId.isNullOrEmpty()) return

        val url = "$FAVORITOS_URL?user_id=$userId"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val favoritosIds = mutableSetOf<Int>()
                    for (i in 0 until response.length()) {
                        val jsonObject = response.getJSONObject(i)
                        favoritosIds.add(jsonObject.getInt("id"))
                    }

                    // Atualizar estado de favoritos
                    for (livro in livrosList) {
                        livro.is_favorite = favoritosIds.contains(livro.id)
                    }
                    for (livro in livrosListFull) {
                        livro.is_favorite = favoritosIds.contains(livro.id)
                    }

                    adapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                // Silencioso - não precisa mostrar erro
            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    private fun toggleFavorite(livro: Livro, position: Int) {
        if (userId.isNullOrEmpty()) {
            Toast.makeText(context, "Erro ao obter utilizador", Toast.LENGTH_SHORT).show()
            return
        }

        val action = if (livro.is_favorite) "remove" else "add"

        val stringRequest = object : StringRequest(
            Request.Method.POST,
            FAVORITOS_URL,
            { response ->
                try {
                    val jsonResponse = org.json.JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        // Atualizar estado local
                        livro.is_favorite = !livro.is_favorite

                        // Atualizar também na lista completa
                        for (l in livrosListFull) {
                            if (l.id == livro.id) {
                                l.is_favorite = livro.is_favorite
                                break
                            }
                        }

                        adapter.notifyItemChanged(position)

                        val message = if (livro.is_favorite) "⭐ Adicionado aos favoritos" else "Removido dos favoritos"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(context, "Erro ao atualizar favorito", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return hashMapOf(
                    "user_id" to userId!!,
                    "livro_id" to livro.id.toString(),
                    "action" to action
                )
            }
        }

        requestQueue.add(stringRequest)
    }

    private fun filtrarLivros(texto: String) {
        livrosList.clear()

        if (texto.isEmpty()) {
            livrosList.addAll(livrosListFull)
        } else {
            val textoLower = texto.lowercase()
            for (livro in livrosListFull) {
                if (livro.titulo.lowercase().contains(textoLower) ||
                    livro.autores.lowercase().contains(textoLower) ||
                    livro.categoria.lowercase().contains(textoLower)) {
                    livrosList.add(livro)
                }
            }
        }

        adapter.notifyDataSetChanged()

        if (livrosList.isEmpty() && texto.isNotEmpty()) {
            Toast.makeText(context, getString(R.string.no_books_for_search, texto), Toast.LENGTH_SHORT).show()
        }
    }
}