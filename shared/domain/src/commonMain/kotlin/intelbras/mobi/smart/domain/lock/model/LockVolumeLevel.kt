package intelbras.mobi.smart.domain.lock.model

import intelbras.mobi.smart.domain.core.IntCoded
import intelbras.mobi.smart.domain.core.IntCodedSerializer
import kotlinx.serialization.Serializable

@Serializable(with = LockVolumeLevelSerializer::class)
enum class LockVolumeLevel(override val code: Int) : IntCoded {
    Mute(0),
    Low(1),
    Medium(2),
    High(3),
}

object LockVolumeLevelSerializer :
    IntCodedSerializer<LockVolumeLevel>("LockVolumeLevel", LockVolumeLevel.entries.toTypedArray())
