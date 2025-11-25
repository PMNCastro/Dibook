package pm.dibook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class PesquisarFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    private val itemList = ArrayList<Item>()
    private lateinit var requestQueue: RequestQueue

    companion object {
        private const val DATA_URL = "https://esan-tesp-ds-paw.web.ua.pt/tesp-ds-g29/projeto/getData.php"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pesquisar, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = ItemAdapter(requireContext(), itemList)
        recyclerView.adapter = adapter

        requestQueue = Volley.newRequestQueue(requireContext())

        carregarDados()

        return view
    }

    private fun carregarDados() {
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            DATA_URL,
            null,
            { response ->
                try {
                    itemList.clear()
                    for (i in 0 until response.length()) {
                        val jsonObject = response.getJSONObject(i)

                        val item = Item(
                            id = jsonObject.getInt("id"),
                            nome = jsonObject.getString("nome"),
                            descricao = jsonObject.getString("descricao")
                        )

                        itemList.add(item)
                    }
                    adapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Erro ao processar dados", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(context, "Erro ao carregar dados: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonArrayRequest)
    }
}

