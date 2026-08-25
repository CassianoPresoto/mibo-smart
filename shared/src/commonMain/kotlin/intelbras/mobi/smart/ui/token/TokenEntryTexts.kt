package intelbras.mobi.smart.ui.token

internal object TokenEntryTexts {
    const val TITLE = "Mibo Smart"
    const val SUBTITLE = "Cole o token de acesso para ver seus dispositivos."
    const val WHERE_TO_FIND =
        "Gere o token em open-casainteligente.intelbras.com.br, no menu Contas → Token Temporário."
    const val TOKEN_LABEL = "Token de acesso"
    const val SUBMIT = "Entrar"
    const val SUBMITTING = "Validando o token…"
    const val CHECKING_SESSION = "Verificando a sessão salva…"

    fun failureMessage(failure: TokenEntryFailure): String = when (failure) {
        TokenEntryFailure.EmptyToken -> "Cole o token de acesso para continuar."
        TokenEntryFailure.InvalidToken ->
            "Token inválido ou expirado. Gere um novo no portal e cole aqui."

        TokenEntryFailure.ExpiredSession ->
            "Sua sessão expirou depois de 2 horas. Cole um token novo para continuar."

        TokenEntryFailure.NetworkUnavailable ->
            "Sem conexão com a plataforma. Verifique a internet e tente de novo."

        TokenEntryFailure.Unexpected ->
            "Não foi possível validar o token agora. Tente novamente em instantes."
    }
}
