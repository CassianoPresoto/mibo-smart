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
}
