# Mibo Smart

Aplicação Kotlin Multiplatform (Android + iOS) que consome as APIs da plataforma
Open Casa Inteligente da Intelbras.

## Arquitetura de módulos

```
:shared            app comum (Compose Multiplatform) — conhece apenas :shared:business
 ├── :shared:business   regras de negócio e casos de uso — conhece domain e rest
 ├── :shared:rest       client HTTP (Ktor) da Open Casa Inteligente — conhece domain
 └── :shared:domain     modelos, contratos e tipos de erro — não conhece ninguém
```

As dependências apontam sempre para dentro. A comunicação entre módulos acontece
por interfaces declaradas em `:shared:domain` (`DeviceRepository`, `LockRepository`,
`AccessTokenProvider`, casos de uso) e implementadas nas camadas externas, de forma que
trocar o client HTTP ou uma regra não exige tocar nos outros módulos.

Cada módulo publica seu próprio módulo Koin e os agrega por composição:
`restModule()` ← incluído por `businessModule()` ← consumido pelo app em
`startSmartHomeDependencies()`. O `:shared:domain` não tem módulo Koin porque não
instancia nada.

### :shared:domain

Modelos anotados com `@Serializable` (o mesmo tipo atravessa rede e regra de negócio).
Todo identificador é em inglês; o nome que a API espera fica no `@SerialName`
(`@SerialName("ns") val serialNumber`, `@SerialName("tamanhoPagina") val pageSize`), então
renomear no código nunca muda o corpo enviado. Além disso:
enums fechados para estados (`DeviceStatus`, `LockVolumeLevel`, `LightMode`),
resultado explícito `Outcome<T>` (`Success`/`Failure`) e o catálogo de erros
`SmartHomeError` (token ausente, token inválido/expirado, rede indisponível, cota de
streaming estourada, requisição inválida, operação recusada, resposta inesperada).

### :shared:rest

Client Ktor cobrindo os 39 endpoints do Swagger "Gerenciador de APIs Mibo", agrupados
por tag em sete repositórios: autenticação, produtos, câmeras, streaming, fechaduras,
lâmpadas e sensores. Pontos de atenção do contrato tratados aqui:

- todos os endpoints são `POST` no host `open-casainteligente.intelbras.com.br`;
- o envelope da plataforma é inconsistente (`{statusCode, body:{status, data}}` em uns,
  `{status, data}` em outros) — `EnvelopeReader` aceita os dois formatos;
- o header `Authorization` nunca aparece no log (`sanitizeHeader`).

## Tratamento de erro

O rest **lança**: `SmartHomeApiException` é sealed e cada falha tem seu tipo
(`SmartHomeUnauthorizedException`, `SmartHomeQuotaExceededException`,
`SmartHomeOperationRejectedException`, `SmartHomeNetworkException`…). A decisão por status HTTP
fica no `ensureSuccess` do `SmartHomeApiCaller`; a decisão que depende do endpoint fica no
próprio endpoint, pelo gancho `onEndpointError` — é assim que `HTTP 400` em
`cameras/gravacao` vira `SmartHomeDeviceOfflineException` ou `SmartHomeRecordingNotFoundException`
conforme a mensagem, sem que nenhum outro endpoint saiba disso.

O business traduz exception no resultado daquela intenção: `DeviceListResult` tem
`Success`/`Empty`/`InvalidToken`/`NetworkUnavailable`/`Error(cause)`, então a tela trata os casos
que existem para ela em vez de um catálogo global. Regra de negócio mora aqui, não no client:
`HTTP 500` com "Erro desconhecido" é uma resposta real do gateway para token expirado, e é o
caso de uso — não o rest — que decide interpretá-la assim.

Todo `catch` de `Throwable` relança `CancellationException` antes de tratar, para não engolir
cancelamento de coroutine.

### :shared:business

Casos de uso e guarda do token de acesso em memória, em `business/usecase`: o contrato
(`ListDevices`), o resultado daquela intenção (`DeviceListResult`) e a implementação
(`DeviceListing`) ficam juntos. Hoje: autenticar com token (valida chamando a listagem com uma
página mínima e descarta o token se a plataforma recusar), listar dispositivos (com filtro de
origem e paginação saneada), conectar a um dispositivo e encerrar sessão.

Cada dispositivo listado vem com o seu tipo: `DeviceKindResolution` pergunta as funções do aparelho e
traduz para `DeviceKind` — hoje `Camera` (marcador `RTSV*`) ou `Unknown`. A listagem resolve os tipos em
paralelo e não deixa uma consulta de funções que falhou derrubar a lista; o aparelho só aparece sem tipo.
É o mesmo resolvedor que o connect usa, para não existirem duas respostas sobre o que um aparelho é.

Conectar é genérico: `DeviceConnecting` pergunta as funções do aparelho (`/produtos/funcoes/v1`) e
só abre o fluxo de vídeo quando ele anuncia streaming ao vivo (`RTSV*`); o que ele devolve é um
`DeviceConnection` fechado, hoje com `LiveVideo`, amanhã com o ramo da fechadura. `ConnectionTermination`
faz o caminho de volta pelo mesmo tipo — para o vídeo, encerrando a sessão de streaming para liberar a
cota de 1 GB.

A regra de reprodução também mora aqui, não no player: `LiveVideoPlayback` conecta, entrega a fonte ao
player pelo contrato `VideoPlayer` (do domínio, implementado por ExoPlayer no Android e pelo player do
sistema no iOS) e devolve um `Flow<VideoPlaybackState>`. Quando o vídeo cai, `PlaybackRetryPolicy`
decide: falha de decodificação não tem retry; queda de rede ou fim de fluxo tenta de novo, a primeira
tentativa recarregando a mesma URL e as seguintes abrindo uma conexão nova, com espera crescente entre
elas. Ao sair, o player é parado e a sessão de streaming encerrada, mesmo quando a tela foi fechada no
meio.

## Rodando

- App Android: `./gradlew :androidApp:assembleDebug`
- App iOS: abrir [/iosApp](./iosApp) no Xcode e executar de lá.

### Testes

No `:shared:rest` cada método de repositório tem seu teste: o `MockEngine` do Ktor intercepta
a requisição que sairia e o teste confere método, rota, corpo JSON exato e a resposta já
convertida em modelo de domínio — mais os cenários de falha em `assertFailsWith`. Os **39
métodos** dos sete repositórios estão cobertos. O `SmartHomeApiCallerTest` concentra o que vale
para todos: bearer token, ausência de token e a tradução de cada status HTTP em exception.

No `:shared:business` os testes usam [Mokkery](https://mokkery.dev) para mockar os contratos do
domínio, inclusive fazendo o repositório lançar (`everySuspend { ... } throws ...`) para provar
a tradução de exception em resultado de caso de uso.

- Android: `./gradlew :shared:domain:testAndroidHostTest :shared:rest:testAndroidHostTest :shared:business:testAndroidHostTest :shared:testAndroidHostTest`
- iOS: `./gradlew :shared:rest:iosSimulatorArm64Test :shared:business:iosSimulatorArm64Test`

O link dos binários iOS exige o Xcode completo selecionado
(`sudo xcode-select -s /Applications/Xcode.app/Contents/Developer`).

### Token de acesso

O token temporário é gerado pelo usuário em
open-casainteligente.intelbras.com.br → Contas → Token Temporário e digitado no app.
Nenhum token ou credencial é versionado neste repositório.
