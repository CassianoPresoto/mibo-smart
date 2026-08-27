package intelbras.mobi.smart.ui.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intelbras.mobi.smart.business.session.SmartHomeSession
import intelbras.mobi.smart.business.session.usecase.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionViewModel(
    private val smartHomeSession: SmartHomeSession,
) : ViewModel() {

    private val mutableStatus = MutableStateFlow<SessionStatus?>(null)
    val status: StateFlow<SessionStatus?> = mutableStatus.asStateFlow()

    fun onSessionChecked() {
        if (mutableStatus.value != null) return

        viewModelScope.launch {
            mutableStatus.value = smartHomeSession.currentStatus()
        }
    }

    fun onSignOut() {
        viewModelScope.launch {
            smartHomeSession.signOut()
            mutableStatus.value = SessionStatus.None
        }
    }
}
