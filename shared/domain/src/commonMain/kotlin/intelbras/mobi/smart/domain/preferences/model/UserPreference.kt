package intelbras.mobi.smart.domain.preferences.model

private const val SCOPE_SEPARATOR = ":"

enum class UserPreference(val key: String) {
    ThemeMode("theme_mode"),
    LockVolume("lock_volume"),
    ;

    fun keyFor(scope: String?): String =
        if (scope.isNullOrBlank()) key else "$key$SCOPE_SEPARATOR$scope"
}
