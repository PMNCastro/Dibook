package pm.dibook

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class LivroAdapter(
    private val context: Context,
    private val livrosList: List<Livro>,
    private val onFavoriteClick: ((Livro, Int) -> Unit)? = null
) : RecyclerView.Adapter<LivroAdapter.LivroViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivroViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_livro, parent, false)
        return LivroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LivroViewHolder, position: Int) {
        val livro = livrosList[position]

        // Título
        holder.txtTitulo.text = livro.titulo

        // Autores
        holder.txtAutores.text = context.getString(R.string.book_authors, livro.autores)

        // Categoria
        if (livro.categoria.isNotEmpty()) {
            holder.txtCategoria.text = "📚 ${livro.categoria}"
            holder.txtCategoria.visibility = View.VISIBLE
        } else {
            holder.txtCategoria.visibility = View.GONE
        }

        // Editora
        if (livro.editora.isNotEmpty()) {
            holder.txtEditora.text = context.getString(R.string.book_publisher_label, livro.editora)
            holder.txtEditora.visibility = View.VISIBLE
        } else {
            holder.txtEditora.visibility = View.GONE
        }

        // ISBN
        if (livro.isbn.isNotEmpty()) {
            holder.txtISBN.text = context.getString(R.string.book_isbn_label, livro.isbn)
            holder.txtISBN.visibility = View.VISIBLE
        } else {
            holder.txtISBN.visibility = View.GONE
        }

        // Descrição
        if (livro.descricao.isNotEmpty()) {
            holder.txtDescricao.text = livro.descricao
            holder.txtDescricao.visibility = View.VISIBLE
        } else {
            holder.txtDescricao.visibility = View.GONE
        }

        // capa do livro
        if (livro.isbn.isNotEmpty()) {
            val coverUrl = "https://books.google.com/books/publisher/content/images/frontcover/${livro.isbn}?fife=w200-h300" //biblioteca google books a open library tinha menos capas de livros portugueses

            Glide.with(context)
                .load(coverUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)  // Enquanto carrega
                .error(android.R.drawable.ic_menu_gallery)        // Se falhar
                .into(holder.imgCapa)
        } else {
            // Sem ISBN, mostra placeholder
            holder.imgCapa.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Disponibilidade
        holder.txtDisponibilidade.text = context.getString(R.string.available_count, livro.quantidade_disponivel)

        // Status
        if (livro.disponivel) {
            holder.txtStatus.text = context.getString(R.string.book_available)
            holder.txtStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            holder.txtDisponibilidade.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
        } else {
            holder.txtStatus.text = context.getString(R.string.book_unavailable)
            holder.txtStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            holder.txtDisponibilidade.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
        }

        // botao favorito
        updateFavoriteButton(holder.btnFavorite, livro.is_favorite)

        holder.btnFavorite.setOnClickListener {
            onFavoriteClick?.invoke(livro, position)
        }
    }

    override fun getItemCount(): Int = livrosList.size

    // Atualizar ícone de favorito
    private fun updateFavoriteButton(button: ImageButton, isFavorite: Boolean) {
        if (isFavorite) {
            button.setImageResource(android.R.drawable.star_big_on)  //  cheia
        } else {
            button.setImageResource(android.R.drawable.star_big_off) //  vazia
        }
    }

    class LivroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitulo: TextView = itemView.findViewById(R.id.txtTitulo)
        val txtAutores: TextView = itemView.findViewById(R.id.txtAutores)
        val txtCategoria: TextView = itemView.findViewById(R.id.txtCategoria)
        val txtEditora: TextView = itemView.findViewById(R.id.txtEditora)
        val txtISBN: TextView = itemView.findViewById(R.id.txtISBN)
        val txtDescricao: TextView = itemView.findViewById(R.id.txtDescricao)
        val txtDisponibilidade: TextView = itemView.findViewById(R.id.txtDisponibilidade)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)
        val imgCapa: ImageView = itemView.findViewById(R.id.imgCapa)
    }
}