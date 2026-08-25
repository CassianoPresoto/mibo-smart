package intelbras.mobi.smart.rest.client

internal object ApiRoutes {
    const val LIST_DEVICES = "/produtos/listar-dispositivos/v1"
    const val FIND_DEVICE = "/produtos/buscar-dispositivo/v1"
    const val DEVICE_CAPABILITIES = "/produtos/funcoes/v1"
    const val DEVICE_AVAILABILITY = "/produtos/online/v1"
    const val DEVICE_FIRMWARE = "/produtos/versao/v1"
    const val DEVICE_BATTERY = "/produtos/bateria/v1"
    const val RENAME_DEVICE = "/produtos/mudar-nome/v1"
    const val UPDATE_DEVICE = "/produtos/atualizar-dispositivo/v1"

    const val RENEW_TOKEN = "/autenticacao/renovarToken"

    const val CREATE_VIDEO_STREAM = "/cameras/criar-fluxo-video/v1"
    const val RECORDING = "/cameras/gravacao/v1"

    const val AVAILABLE_QUOTA = "/streaming/cota-disponivel/v1"
    const val MY_SESSIONS = "/streaming/minhas-sessoes/v1"
    const val SESSION_INFO = "/streaming/sessao-info/v1"
    const val END_SESSION = "/streaming/encerrar-sessao/v1"

    const val LOCK_OPENING_STATUS = "/fechaduras/status-abertura/v1"
    const val LOCK_CONTROL = "/fechaduras/controle-fechadura/v1"
    const val LOCK_VOLUME = "/fechaduras/volume/v1"
    const val LOCK_CHANGE_VOLUME = "/fechaduras/mudar-volume/v1"
    const val LOCK_OPENING_HISTORY = "/fechaduras/historico-abertura/v1"
    const val LOCK_REMOTE_OPENING_STATUS = "/fechaduras/status-abrir-remoto/v1"
    const val LOCK_ENABLE_REMOTE_OPENING = "/fechaduras/habilitar-abrir-remoto/v1"
    const val LOCK_CREATE_SINGLE_PASSWORD = "/fechaduras/criar-senha-unica/v1"
    const val LOCK_CREATE_PERIODIC_PASSWORD = "/fechaduras/criar-senha-periodica/v1"
    const val LOCK_CREATE_DYNAMIC_PASSWORD = "/fechaduras/criar-senha-dinamica/v1"
    const val LOCK_DELETE_SINGLE_PASSWORD = "/fechaduras/deletar-senha-unica/v1"
    const val LOCK_DELETE_PERIODIC_PASSWORD = "/fechaduras/deletar-senha-periodica/v1"

    const val LIGHT_POWER = "/lampadas/ligada/v1"
    const val LIGHT_START_TIMER = "/lampadas/iniciar-temporizador/v1"
    const val LIGHT_STOP_TIMER = "/lampadas/parar-temporizador/v1"
    const val LIGHT_BRIGHTNESS = "/lampadas/mudar-brilho/v1"
    const val LIGHT_CONTRAST = "/lampadas/mudar-contraste/v1"
    const val LIGHT_COLOR = "/lampadas/mudar-cor/v1"
    const val LIGHT_MODE = "/lampadas/mudar-modo/v1"
    const val LIGHT_TEMPERATURE = "/lampadas/mudar-temperatura/v1"
}
