package intelbras.mobi.smart.domain.camera.model

import intelbras.mobi.smart.domain.core.IntCoded
import intelbras.mobi.smart.domain.core.IntCodedSerializer
import kotlinx.serialization.Serializable

@Serializable(with = StreamProfileSerializer::class)
enum class StreamProfile(override val code: Int) : IntCoded {
    Main(0),
    Secondary(1),
}

object StreamProfileSerializer :
    IntCodedSerializer<StreamProfile>("StreamProfile", StreamProfile.entries.toTypedArray())
