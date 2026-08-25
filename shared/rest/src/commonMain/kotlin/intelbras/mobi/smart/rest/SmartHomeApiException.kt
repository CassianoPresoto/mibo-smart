package intelbras.mobi.smart.rest

sealed class SmartHomeApiException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

class SmartHomeMissingAccessTokenException(
    message: String = "No access token stored for the current session",
) : SmartHomeApiException(message)

class SmartHomeUnauthorizedException(message: String) : SmartHomeApiException(message)

class SmartHomeForbiddenException(message: String) : SmartHomeApiException(message)

class SmartHomeNotFoundException(message: String) : SmartHomeApiException(message)

class SmartHomeInvalidRequestException(message: String) : SmartHomeApiException(message)

class SmartHomeQuotaExceededException(message: String) : SmartHomeApiException(message)

class SmartHomeDeviceOfflineException(message: String) : SmartHomeApiException(message)

class SmartHomeRecordingNotFoundException(message: String) : SmartHomeApiException(message)

class SmartHomeUnknownPlatformErrorException(message: String) : SmartHomeApiException(message)

class SmartHomeServerException(message: String) : SmartHomeApiException(message)

class SmartHomeOperationRejectedException(message: String) : SmartHomeApiException(message)

class SmartHomeUnexpectedResponseException(
    message: String,
    cause: Throwable? = null,
) : SmartHomeApiException(message, cause)

class SmartHomeNetworkException(
    message: String = "Could not reach the platform",
    cause: Throwable? = null,
) : SmartHomeApiException(message, cause)
