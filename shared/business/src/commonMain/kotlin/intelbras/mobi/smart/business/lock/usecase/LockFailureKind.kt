package intelbras.mobi.smart.business.lock.usecase

import intelbras.mobi.smart.business.session.rejectsTheAccessToken
import intelbras.mobi.smart.rest.SmartHomeDeviceOfflineException
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeNotFoundException
import intelbras.mobi.smart.rest.SmartHomeOperationRejectedException
import intelbras.mobi.smart.rest.SmartHomeUnknownPlatformErrorException

internal enum class LockFailureKind {
    Refused,
    DeviceOffline,
    InvalidToken,
    NetworkUnavailable,
    PlatformFailure,
    Unexpected,
}

internal fun Throwable.asLockFailureKind(): LockFailureKind = when {
    rejectsTheAccessToken() -> LockFailureKind.InvalidToken
    this is SmartHomeOperationRejectedException -> LockFailureKind.Refused
    this is SmartHomeDeviceOfflineException -> LockFailureKind.DeviceOffline
    this is SmartHomeNotFoundException -> LockFailureKind.DeviceOffline
    this is SmartHomeNetworkException -> LockFailureKind.NetworkUnavailable
    this is SmartHomeUnknownPlatformErrorException -> LockFailureKind.PlatformFailure
    else -> LockFailureKind.Unexpected
}
