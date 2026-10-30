CHAT IA - COMO USAR (versao portatil, nao precisa instalar)

1. Extraia esta pasta INTEIRA do .zip. Nao abra o .exe de dentro do zip.
   Mantenha as pastas "app" e "runtime" ao lado do "Chat IA.exe".

2. De duplo clique em "Configurar.bat" e informe:
  - a MONGODB_URI   (string de conexao do MongoDB Atlas)
  - a GROQ_API_KEY  (chave da API da Groq)
    Isso cria o arquivo  %USERPROFILE%\.chatia\config.properties
    Basta fazer UMA vez em cada computador.

3. De duplo clique em "Chat IA.exe".

OBSERVACOES
- O computador precisa de internet, e o IP dele precisa estar liberado em
  "Network Access" no MongoDB Atlas.
- Se o programa nao abrir, instale o "Microsoft Visual C++ Redistributable
  2015-2022 (x64)", exigido pelo JavaFX.
- As credenciais NAO vem nesta pasta, de proposito (seguranca).
- O Windows ou o antivirus podem avisar sobre "editor desconhecido", pois o
  programa nao e assinado digitalmente. Escolha "Executar mesmo assim".