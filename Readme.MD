# Chat IA (xhetibot)

## 1. Sobre o projeto

O **Chat IA** é uma aplicação desktop de conversação com Inteligência Artificial, feita em Java e JavaFX. Vários usuários podem usar o mesmo sistema, e cada um tem o seu próprio ambiente: as conversas e o histórico de cada pessoa ficam separados das demais.

Principais recursos:

- Animação de abertura feita com **GSAP**, exibida em um `WebView` antes da tela de login.
- Cadastro, login, logout e recuperação de senha por código temporário.
- Conversas individuais por usuário: criar, renomear e excluir (com confirmação).
- Envio de perguntas para a **API da Groq** e histórico salvo no **MongoDB Atlas**.
- **RAG** por palavras-chave: a pergunta é cruzada com uma base de conhecimento antes de ir para a IA.
- Tema claro e escuro, com a escolha lembrada entre execuções.
- Instalador para Windows gerado com `jpackage`.

## 2. Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 21 | Linguagem |
| JavaFX 21 (`controls`, `graphics`, `web`) | Interface gráfica, construída 100% por código (sem Scene Builder) |
| JavaFX CSS | Estilos e temas (`base.css`, `light.css`, `dark.css`) |
| HTML + CSS + JavaScript + GSAP | Animação de abertura, dentro do `WebView` |
| Maven | Build e dependências (`pom.xml`) |
| MongoDB Atlas (driver `mongodb-driver-sync` 5.2.1) | Banco de dados |
| jBCrypt 0.4 | Hash das senhas |
| API da Groq (`java.net.http.HttpClient`) | Inteligência Artificial |
| jpackage | Empacotamento e instalador para Windows |
| Git/GitHub | Versionamento |

## 3. Arquitetura

O projeto segue MVC com as camadas Service e Repository. Cada camada só conversa com a seguinte:

```
View  →  Controller  →  Service  →  Repository  →  MongoDB
```

O fluxo de uma pergunta ao chat:

```
ChatView
   ↓
ChatController      (recebe a ação da tela e roda fora da thread da interface)
   ↓
ChatService         (orquestra o fluxo e salva o histórico)
   ↓
RAGService          (busca contexto na base de conhecimento)
   ↓
GroqService         (monta a requisição e chama a API)
   ↓
Groq API
```

Responsabilidades:

- **View**: só monta a tela e repassa os cliques ao controller.
- **Controller**: traduz eventos da tela em chamadas aos services e devolve o resultado à interface. Não tem regra de negócio nem acessa o banco.
- **Service**: regras de negócio (validações, hash de senha, RAG, chamada à IA).
- **Repository**: único ponto que fala com o MongoDB.

A animação de abertura segue o fluxo:

```
JavaFX (splashView) → WebView → HTML + CSS + JS → GSAP → animação → LoginView
```

Quando a timeline do GSAP termina, o JavaScript chama `alert("CHATIA_FINALIZAR")`. A `splashView` escuta esse sinal com `WebEngine.setOnAlert` e abre o login. Há também uma trava de segurança de 20 segundos para o usuário nunca ficar preso na abertura.

## 4. Estrutura de pastas

```
chat-ia/
├── pom.xml
├── README.md
├── build-installer.bat            Gera o instalador/versão portátil (jpackage)
├── installer/
│   ├── Configurar.bat             Chama o Configurar.ps1
│   ├── Configurar.ps1             Cria o arquivo de configuração do usuário
│   └── LEIA-ME.txt                Guia de uso para quem recebe o app
└── src/main/
    ├── java/br/com/chatia/
    │   ├── Main.java, Launcher.java   Pontos de entrada
    │   ├── Teste*.java                Testes manuais (ver seção 9)
    │   ├── config/        AppConfig (variáveis/arquivo de configuração) e MongoConfig (conexão)
    │   ├── controller/    Login, Register, ForgotPassword, Chat e Theme controllers
    │   ├── model/         Usuario, Conversa, Mensagem, Documento, TokenRecuperacao
    │   ├── repository/    Acesso ao MongoDB (um repositório por coleção)
    │   ├── service/       Usuario, Conversa, Mensagem, Chat, RAG, Groq e PasswordRecovery
    │   ├── rag/           ProcessadorTexto, StopWords e ResultadoBusca
    │   ├── view/          Telas: splash, login, cadastro, recuperação e chat
    │   ├── exception/     IAException
    │   └── util/          Sessão, temas, animações, tarefas assíncronas, mensagens de erro
    └── resources/
        ├── css/           base.css, light.css, dark.css
        ├── images/        Ícone do aplicativo
        └── splash/        splash.html, splash.css, splash.js e gsap.min.js
```

## 5. Pré-requisitos

- Windows
- JDK 21 (com `jpackage`)
- Maven
- Conta no MongoDB Atlas (um cluster e um usuário de banco)
- Conta na Groq (para gerar a API Key)
- Para gerar o instalador `.exe`: WiX Toolset 3.x no PATH. Sem o WiX dá para gerar a versão portátil (ver seção 10).
- Para rodar o app em outro computador: Microsoft Visual C++ Redistributable 2015-2022 (x64), exigido pelo JavaFX.

## 6. Configuração

O aplicativo precisa de duas informações, e **nenhuma delas fica no código nem no Git**:

| Chave | O que é |
|---|---|
| `MONGODB_URI` | String de conexão do MongoDB Atlas (`mongodb+srv://...`) |
| `GROQ_API_KEY` | Chave da API da Groq |
| `GROQ_MODEL` (opcional) | Troca o modelo da Groq. O padrão é `openai/gpt-oss-120b` |

O aplicativo procura cada chave nesta ordem:

1. **Variável de ambiente** com o mesmo nome.
2. **Arquivo** `%USERPROFILE%\.chatia\config.properties`.

**Jeito mais simples:** execute `Configurar.bat` (pasta `installer`, ou ao lado do instalador/app portátil) e cole os dois valores quando for pedido. Ele cria o arquivo acima.

**Jeito manual:** crie o arquivo `%USERPROFILE%\.chatia\config.properties` com este conteúdo:

```properties
MONGODB_URI=mongodb+srv://USUARIO:SENHA@SEU-CLUSTER.mongodb.net/
GROQ_API_KEY=cole_sua_chave_aqui
```

Ou defina variáveis de ambiente (feche e abra o terminal/IDE depois):

```bat
setx MONGODB_URI "mongodb+srv://USUARIO:SENHA@SEU-CLUSTER.mongodb.net/"
setx GROQ_API_KEY "cole_sua_chave_aqui"
```

**No MongoDB Atlas**, libere o IP do computador em *Network Access*. Sem isso a conexão é recusada. A string `mongodb+srv://` usa conexão segura (TLS). O banco usado pelo aplicativo se chama `chat_ia` e as coleções são criadas automaticamente no primeiro uso.

Se faltar alguma configuração, o aplicativo mostra uma mensagem clara na tela em vez de travar.

## 7. Banco de dados

Banco: `chat_ia`. Coleções:

| Coleção | Campos | Observações |
|---|---|---|
| `usuarios` | `_id`, `nome`, `email`, `senhaHash` | E-mail salvo em minúsculas. Senha só como hash BCrypt |
| `conversas` | `_id`, `usuarioId`, `titulo`, `criadaEm`, `atualizadaEm` | Cada conversa pertence a um usuário |
| `mensagens` | `_id`, `usuarioId`, `conversaId`, `autor`, `conteudo`, `criadaEm` | `autor` é `USUARIO` ou `IA` |
| `documents` | `_id`, `title`, `content`, `keywords`, `createdAt`, `updatedAt` | Base de conhecimento do RAG |
| `password_reset_tokens` | `usuarioId`, `tokenHash`, `expiraEm`, `tentativas`, `criadoEm` | No máximo um código ativo por usuário |

Os nomes `usuarios`, `conversas` e `mensagens` estão em português no código. Eles correspondem a *users*, *conversations* e *messages* do enunciado.

**Isolamento por usuário.** Os repositórios de conversas e mensagens nunca buscam só pelo id. Toda consulta, renomeação e exclusão usa um filtro com o **id do registro e o id do dono** (`filtroDono`). Além disso, os services confirmam que a conversa pertence ao usuário logado antes de qualquer operação. Assim, o isolamento não depende da interface: um usuário não consegue ler, alterar ou apagar a conversa de outro, mesmo conhecendo o id dela.

## 8. RAG (recuperação de informações)

O RAG desta primeira versão é feito por palavras-chave:

```
Pergunta
  → Normalização      minúsculas e remoção de acentos
  → Tokenização       separa o texto em palavras
  → Stop words        descarta palavras sem valor para a busca
  → Palavras relevantes (e plural simples: "senhas" → "senha")
  → Busca na coleção documents (campo keywords)
  → Os 3 documentos com mais palavras em comum
  → Contexto enviado à Groq junto com a pergunta
```

**Cadastro dos documentos.** Ao cadastrar um documento (`RAGService.cadastrar`), as palavras-chave são extraídas automaticamente do título e do conteúdo, com o mesmo pipeline usado nas perguntas. Assim, pergunta e documento passam pelo mesmo tratamento. O `TesteRAG` cadastra uma base de exemplo no Atlas.

**Uso do contexto.** Os documentos encontrados entram na instrução enviada à IA (cada um limitado a 1200 caracteres). A IA é orientada a usar a base quando ela for relevante e, quando a base não tiver a resposta, a avisar que a informação veio do conhecimento geral. Se nenhum documento for encontrado, ou se a busca falhar, o chat continua normalmente, sem contexto.

### Por que as stop words são removidas?

Stop words são palavras muito frequentes que quase não dizem sobre o que a pergunta trata, como *a, o, de, da, em, para, que, com, uma*. Na pergunta "O que é encapsulamento em Java?", as palavras que identificam o assunto são **encapsulamento** e **java**. O resto aparece em praticamente todos os textos.

Se elas não fossem removidas:

- **A busca ficaria imprecisa**: quase todo documento tem "de" e "que", então quase todo documento seria considerado parecido com a pergunta.
- **A pontuação seria distorcida**: palavras comuns somariam pontos e esconderiam as palavras realmente importantes.
- **A busca ficaria mais lenta e as palavras-chave poluídas**: os documentos guardariam dezenas de termos inúteis.

A lista fica em `rag/StopWords.java` (artigos, preposições, pronomes, verbos muito comuns, conjunções e palavras de pedido como "explique" e "quero"). Está escrita sem acento e em minúsculas, o mesmo formato que o texto normalizado tem quando chega à comparação.

## 9. Execução

Pelo IDE (Eclipse, IntelliJ ou outro com suporte a Maven): importe o projeto como projeto Maven, configure `MONGODB_URI` e `GROQ_API_KEY` (seção 6) e execute `br.com.chatia.Main`.

Pelo Maven:

```bash
mvn clean javafx:run
```

**Testes.** Os testes são manuais, sem interface, e usam o MongoDB Atlas real. Execute cada classe pelo IDE (`main`) e leia o resumo `[OK]` / `[FALHOU]` no console:

| Classe | O que verifica |
|---|---|
| `TesteIsolamento` | Dois usuários com conversas diferentes. Tentativas de acesso indevido são barradas e o fluxo do dono funciona |
| `TesteRAG` | Cadastro de documentos, palavra existente, palavra inexistente e remoção de stop words |
| `TesteGroq` | Envio de uma pergunta e recebimento da resposta |
| `TesteRecuperacaoSenha` | Geração do código, redefinição, código expirado e limite de tentativas |
| `TesteTextoSimples` | Limpeza do Markdown devolvido pela IA |

Cadastro, login, senha incorreta, logout, tema claro/escuro e redimensionamento são verificados pela própria interface.

## 10. Instalação

**Gerar o instalador** (na raiz do projeto, no Windows):

```bat
build-installer.bat              :: instalador ChatIA-Setup.exe (precisa do WiX 3.x)
build-installer.bat appimage     :: pasta portátil + .zip (não precisa do WiX)
build-installer.bat debug        :: abre o app com console, para ver erros
```

O script compila o projeto com Maven e chama o `jpackage`. O instalador inclui o runtime Java, cria atalho na área de trabalho e no Menu Iniciar, tem nome e versão e usa o ícone de `src/main/resources/images`. O resultado fica em `target\installer\output\`.

**Instalar em outro computador:**

1. Execute `ChatIA-Setup.exe` e conclua a instalação. Na versão portátil, extraia o `.zip` inteiro e use o `Chat IA.exe` de dentro da pasta.
2. Execute `Configurar.bat` (gerado junto com o instalador) e informe `MONGODB_URI` e `GROQ_API_KEY`. Isso só precisa ser feito uma vez por computador.
3. Abra o **Chat IA** pelo atalho ou pelo Menu Iniciar.

Lembre-se de liberar o IP do computador no *Network Access* do Atlas. O Windows pode mostrar o aviso de "editor desconhecido", porque o instalador não é assinado digitalmente. Escolha "Executar mesmo assim".

## 11. Segurança

- **Senhas**: nunca ficam em texto puro. São guardadas como hash **BCrypt** (com *salt* gerado a cada senha). Mínimo de 8 caracteres.
- **API Keys e credenciais**: nada fica no código. Vêm de variável de ambiente ou do arquivo `%USERPROFILE%\.chatia\config.properties`, que fica fora do projeto. O `.gitignore` bloqueia `config.properties`, `.env` e a pasta `.chatia/`.
- **Sessão**: a classe `Sessao` guarda o usuário autenticado em memória. O logout a encerra e volta ao login, e outro usuário pode entrar em seguida.
- **Isolamento de dados**: toda operação sobre conversas e mensagens exige o id do registro e o id do dono, no repositório e no service (ver seção 7).
- **Recuperação de senha**: a senha nunca é recuperada nem exibida. É gerado um código aleatório de 8 caracteres (`SecureRandom`), guardado apenas como hash SHA-256, com validade de 15 minutos, no máximo 5 tentativas e uso único. A comparação é em tempo constante.
- **Validação de entrada**: campos obrigatórios, formato do e-mail, tamanho de senha, tamanho de título e limite de 8000 caracteres por mensagem.
- **Erros**: exceções técnicas são convertidas em mensagens compreensíveis. O detalhe técnico vai só para o console.
- **Banco**: conexão com o Atlas por `mongodb+srv://` (TLS), com liberação de IP no Network Access.

## 12. Limitações

- **Código de recuperação na tela (modo demonstração).** Não há envio de e-mail, então o código aparece na própria tela de recuperação. Na prática, quem souber o e-mail de uma conta consegue redefinir a senha dela. Num sistema real, o código seria enviado por e-mail ou SMS. Além disso, como o código só aparece quando a conta existe, a tela acaba revelando se um e-mail está cadastrado, mesmo com a mensagem genérica.
- **RAG simples.** A busca é por palavras-chave: não entende sinônimos nem sentido. A pontuação é a contagem de palavras em comum, o que pode favorecer documentos longos. Cada documento é cortado em 1200 caracteres, sem divisão em trechos. Não há tela para cadastrar documentos; eles são inseridos por código.
- **Banco.** A unicidade do e-mail é verificada pela aplicação, sem índice único no MongoDB. Não há expiração automática (TTL) dos códigos de recuperação.
- **Dependência de internet.** Sem acesso ao Atlas ou à Groq o aplicativo mostra a mensagem de erro e não funciona.
- **Chat.** A resposta aparece de uma vez, sem *streaming*. Apenas as 20 mensagens mais recentes são enviadas à IA, e a resposta é exibida em texto simples (o Markdown é removido).
- **Sessão.** Fica em memória do processo, sem persistência entre execuções.
- **Testes.** São manuais, com `main`, e dependem do Atlas e da Groq reais. Não há testes automatizados com JUnit.
- **Distribuição.** O instalador não é assinado digitalmente e não leva as credenciais: cada computador precisa rodar o `Configurar.bat`.