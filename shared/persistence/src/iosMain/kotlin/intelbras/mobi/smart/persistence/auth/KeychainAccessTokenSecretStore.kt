package intelbras.mobi.smart.persistence.auth

import intelbras.mobi.smart.persistence.ACCESS_TOKEN_SECRET_KEY
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecReturnData
import platform.Security.kSecUseDataProtectionKeychain
import platform.Security.kSecValueData

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class KeychainAccessTokenSecretStore : AccessTokenSecretStore {

    override fun read(): String? = memScoped {
        val query = baseQuery()
        CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        CFRelease(query)

        if (status != errSecSuccess) return@memScoped null

        val data = CFBridgingRelease(result.value) as? NSData ?: return@memScoped null
        NSString.create(data, NSUTF8StringEncoding) as String?
    }

    override fun write(token: String) {
        clear()

        val query = baseQuery()
        val data = (token as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val retainedData = CFBridgingRetain(data)
        CFDictionarySetValue(query, kSecValueData, retainedData)
        CFDictionarySetValue(
            query,
            kSecAttrAccessible,
            kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
        )

        SecItemAdd(query, null)
        CFRelease(retainedData)
        CFRelease(query)
    }

    override fun clear() {
        val query = baseQuery()
        SecItemDelete(query)
        CFRelease(query)
    }

    private fun baseQuery(): CFMutableDictionaryRef {
        val query = CFDictionaryCreateMutable(null, CAPACITY, null, null)
            ?: error("Não foi possível montar a consulta do Keychain")

        CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(query, kSecAttrService, serviceName)
        CFDictionarySetValue(query, kSecAttrAccount, accountName)
        CFDictionarySetValue(query, kSecUseDataProtectionKeychain, kCFBooleanTrue)
        return query
    }

    private val serviceName = CFBridgingRetain(KEYCHAIN_SERVICE as NSString)

    private val accountName = CFBridgingRetain(ACCESS_TOKEN_SECRET_KEY as NSString)

    private companion object {
        const val KEYCHAIN_SERVICE = "intelbras.mobi.smart"
        const val CAPACITY = 6L
    }
}
