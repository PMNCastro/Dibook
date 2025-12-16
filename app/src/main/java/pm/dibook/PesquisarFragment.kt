package pm.dibook

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
import com.android.volley.toolbox.Volley

class PesquisarFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LivroAdapter
    private val livrosList = ArrayList<Livro>()
    private val livrosListFull = ArrayList<Livro>() // Lista completa para filtrar localmente
    private lateinit var requestQueue: RequestQueue

    companion object {
        private const val LIVROS_URL = "https://esan-tesp-ds-paw.web.ua.pt/tesp-ds-g29/projeto/mobile/getLivros.php"
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

        adapter = LivroAdapter(requireContext(), livrosList)
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
                            disponivel = jsonObject.getBoolean("disponivel")
                        )

                        livrosList.add(livro)
                        livrosListFull.add(livro) // Guardar cópia completa
                    }

                    adapter.notifyDataSetChanged()

                    if (livrosList.isEmpty()) {
                        Toast.makeText(context, getString(R.string.no_books_found), Toast.LENGTH_SHORT).show()
                    }

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

    private fun filtrarLivros(texto: String) {
        livrosList.clear()

        if (texto.isEmpty()) {
            // Se não há pesquisa, mostrar todos
            livrosList.addAll(livrosListFull)
        } else {
            // Filtrar por título, autor ou categoria
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