package pm.dibook

data class Livro(
    val id: Int,
    val titulo: String,
    val autores: String,
    val editora: String,
    val isbn: String,
    val categoria: String,
    val descricao: String,
    val quantidade_disponivel: Int,
    val disponivel: Boolean,
    var is_favorite: Boolean = false  // NOVO CAMPO
)