package intelbras.mobi.smart.ui.devices

internal object DeviceListTexts {
    const val TITLE = "Dispositivos"
    const val SIGN_OUT = "Sair"
    const val LOADING = "Buscando seus dispositivos…"
    const val EMPTY_TITLE = "Nenhum dispositivo"
    const val EMPTY_MESSAGE = "Esta conta ainda não tem dispositivos vinculados ou compartilhados."
    const val RELOAD = "Atualizar"
    const val RETRY = "Tentar de novo"
    const val ONLINE = "Online"
    const val OFFLINE = "Offline"

    fun failureMessage(failure: DeviceListFailure): String = when (failure) {
        DeviceListFailure.InvalidToken ->
            "Sua sessão expirou. Saia e cole um token novo para continuar."

        DeviceListFailure.NetworkUnavailable ->
            "Sem conexão com a plataforma. Verifique a internet e tente de novo."

        DeviceListFailure.Unexpected ->
            "Não foi possível carregar os dispositivos agora. Tente novamente em instantes."
    }

    fun statusLabel(isOnline: Boolean): String = if (isOnline) ONLINE else OFFLINE
}
