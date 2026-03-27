Alerta Cidadão

Aplicativo Android para registro e visualização de relatos locais (protótipo).

Resumo rápido
- App Android nativo em Kotlin para criar relatórios com foto, localização e categoria.
- Autenticação local simples via `SharedPreferences` (sem backend).

Principais funcionalidades
- Criar conta (nome, e-mail, senha, bairro).
- Fazer login / recuperar senha (localmente).
- Criar novo relato com foto (câmera ou galeria) e local.
- Visualizar detalhes do relato (com carregamento seguro de imagens).
- Tela de perfil exibindo Nome, Bairro e E‑mail.

Tecnologias
- Kotlin, Android SDK
- ViewBinding, Material Components
- Gradle (wrapper)

Ferramentas usadas
- Android Studio
- Figma Editor
- Figma Maker
- GitHub Copilot

Prototipação e Design
- Protótipos e telas foram criados no Figma (Editor + Maker) para definir fluxos, espaçamentos e interações.
- Seguiu-se as diretrizes do Material Design 3 


Arquivos importantes
- `app/src/main/java/com/alertacidadao/app/data/AuthRepository.kt` — persistência local de conta (SharedPreferences).
- `app/src/main/java/com/alertacidadao/app/data/ReportRepository.kt` — CRUD local de relatos (JSON em SharedPreferences).
- `app/src/main/java/com/alertacidadao/app/AddReportActivity.kt` — fluxo de criação de relato (câmera + galeria, persiste permissão de URI).
- `app/src/main/java/com/alertacidadao/app/ReportDetailActivity.kt` — carregamento seguro da imagem do relato.
- `app/src/main/java/com/alertacidadao/app/fragment/ProfileFragment.kt` — exibição de dados do usuário e estatísticas.
- `app/src/main/assets/leaflet_map.html` — mapa/visualização baseada em Leaflet (usada por WebView/Asset).

Assinatura (signing)
- Para builds de release, o projeto pode usar um arquivo `keystore.properties` na raiz com as propriedades:

```properties
storeFile=alertacidadao.keystore
storePassword=SEU_STORE_PASSWORD
keyAlias=SEU_KEY_ALIAS
keyPassword=SEU_KEY_PASSWORD
```


- Para gerar um APK assinado (release) execute:

```bash
./gradlew.bat assembleRelease
```

Como compilar (debug)
1. Abra um terminal na raiz do projeto.
2. Execute:

```
./gradlew.bat assembleDebug
```

3. Instale o APK no dispositivo ou execute pelo Android Studio.

Testes
- Testes unitários locais:

```
./gradlew.bat test
```

- Notas de desenvolvimento
- Imagens de câmera usam `FileProvider` (saída em arquivo privado do app).
- Imagens da galeria usam `ActivityResultContracts.OpenDocument` e a permissão de leitura é persistida (`takePersistableUriPermission`).
- Para persistência real de usuários e relatórios em produção, será necessário implemenetar backend.


Licença
- Este projeto está licenciado sob a Licença MIT — veja o arquivo `LICENSE`.

---
Feito por:

- Daniel Leite Delfino — https://github.com/xdanielzdelfino
- Wanessa Lanne de Araujo Batista — https://github.com/wanessalanne

Trabalho da faculdade: Universidade de Fortaleza (UNIFOR)
Curso: Análise e Desenvolvimento de Sistemas
Disciplina: Desenvolvimento para Plataformas Móveis
