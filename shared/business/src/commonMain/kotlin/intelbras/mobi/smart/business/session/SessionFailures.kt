package intelbras.mobi.smart.business.session

import intelbras.mobi.smart.rest.SmartHomeForbiddenException
import intelbras.mobi.smart.rest.SmartHomeMissingAccessTokenException
import intelbras.mobi.smart.rest.SmartHomeUnauthorizedException

internal fun Throwable.rejectsTheAccessToken(): Boolean = when (this) {
    is SmartHomeUnauthorizedException,
    is SmartHomeForbiddenException,
    is SmartHomeMissingAccessTokenException,
    -> true

    else -> false
}
