Aplicação Android para gestão de empréstimos de livros numa biblioteca, desenvolvida em Kotlin. Permite aos clientes consultar o seu estado de conta, pesquisar livros, gerir favoritos e receber notificações sobre a disponibilidade dos livros que seguem.
 
---
 
## Funcionalidades
 
- **Login seguro** — autenticação via API REST com opção de guardar credenciais localmente
- **Dashboard pessoal** — visualização de empréstimos ativos, devolvidos e multas pendentes
- **Pesquisa de livros** — pesquisa em tempo real por título, autor ou categoria
- **Favoritos** — adicionar/remover livros aos favoritos, sincronizados com o servidor
- **Notificações push** — alertas automáticos quando um livro favorito fica disponível ou indisponível
- **Suporte multilingue** — Português, Inglês e Alemão
- **Tema escuro** — suporte nativo a dark mode
---
 
## Arquitetura
 
A aplicação segue uma arquitetura simples baseada em **Activities e Fragments**:
 
```
MainActivity        → Login / autenticação
└── HomeActivity    → Ecrã principal com navegação bottom bar
    ├── HomeFragment        → Dashboard do utilizador
    ├── PesquisarFragment   → Catálogo e pesquisa de livros
    └── FavoritosFragment   → Lista de livros favoritos
 
NotificationWorker  → Worker periódico (WorkManager) para notificações
LivroAdapter        → RecyclerView adapter para listagem de livros
Livro               → Data class do modelo de livro
```
 
---
 
## Tecnologias
 
| Tecnologia | Versão | Uso |
|---|---|---|
| Kotlin | 2.0.21 | Linguagem principal |
| Android SDK | compileSdk 36 / minSdk 28 | Plataforma |
| Volley | 1.2.1 | Requisições HTTP à API |
| Glide | 4.16.0 | Carregamento de imagens |
| RecyclerView | 1.3.2 | Listagem de livros |
| CardView | 1.0.0 | Layout dos cartões de livro |
| WorkManager | 2.9.0 | Notificações periódicas em background |
| Material Design | 1.13.0 | Componentes de UI |
 
---
 
## Pré-requisitos
 
- Android Studio (recomendado: versão recente com suporte a AGP 8.13)
- Android 9.0 (API 28) ou superior
- Ligação à internet (a app consome uma API REST remota)
- Conta de cliente registada no sistema (apenas clientes podem usar a app; admins e funcionários são bloqueados no login)
---
 
## Instalação e execução
 
1. Clona o repositório:
   ```bash
   git clone https://github.com/PMNCastro/Dibook.git
   ```
 
2. Abre o projeto no **Android Studio**.
3. Aguarda a sincronização do Gradle.
4. Corre a aplicação num emulador ou dispositivo físico com Android 9+.
> **Nota:** A app comunica com o servidor em `https://esan-tesp-ds-paw.web.ua.pt/tesp-ds-g29/projeto/`. Certifica-te de que tens acesso à rede da UA ou VPN, caso necessário.
 
---
 
## API Endpoints
 
| Endpoint | Método | Descrição |
|---|---|---|
| `mobile/login_mobile.php` | POST | Autenticação do utilizador |
| `mobile/getUserData.php` | GET | Dados do dashboard (empréstimos, multas) |
| `mobile/getLivros.php` | GET | Catálogo completo de livros |
| `mobile/favoritos.php` | GET / POST | Listar / adicionar / remover favoritos |
| `mobile/checkFavorites.php` | GET | Verificar alterações de disponibilidade |
 
---
 
## Sistema de Notificações
 
O `NotificationWorker` é executado pelo **WorkManager** a cada **30 minutos** enquanto o utilizador estiver autenticado. Compara a quantidade disponível atual de cada livro favorito com o último valor guardado localmente (`SharedPreferences`) e dispara uma notificação quando:
 
- Um livro que estava **indisponível passa a disponível** 📚
- Um livro que estava **disponível passa a indisponível**
As notificações são canceladas automaticamente ao fazer logout.
 
---
 
## 🌍 Idiomas suportados
 
- 🇵🇹 Português (padrão)
- 🇬🇧 Inglês (`values-en`)
- 🇩🇪 Alemão (`values-de`)
---
 
##  Estrutura do projeto
 
```
Dibook/
├── app/
│   ├── src/main/
│   │   ├── java/pm/dibook/
│   │   │   ├── MainActivity.kt
│   │   │   ├── HomeActivity.kt
│   │   │   ├── HomeFragment.kt
│   │   │   ├── PesquisarFragment.kt
│   │   │   ├── FavoritosFragment.kt
│   │   │   ├── LivroAdapter.kt
│   │   │   ├── Livro.kt
│   │   │   └── NotificationWorker.kt
│   │   ├── res/
│   │   │   ├── layout/         # Layouts XML
│   │   │   ├── values/         # Strings PT, cores, temas
│   │   │   ├── values-en/      # Strings EN
│   │   │   ├── values-de/      # Strings DE
│   │   │   └── drawable/       # Recursos gráficos
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
└── settings.gradle.kts
```
 
---
 
## Autor
 
**Pedro Castro** — [@PMNCastro](https://github.com/PMNCastro)
 
Projeto desenvolvido no âmbito da unidade curricular de **Engenharia de Software** 
