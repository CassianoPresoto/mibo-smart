package intelbras.mobi.smart.domain.light.model

import intelbras.mobi.smart.domain.core.IntCoded
import intelbras.mobi.smart.domain.core.IntCodedSerializer
import kotlinx.serialization.Serializable

@Serializable(with = LightModeSerializer::class)
enum class LightMode(override val code: Int) : IntCoded {
    White(0),
    Color(1),
    Scene(2),
}

object LightModeSerializer :
    IntCodedSerializer<LightMode>("LightMode", LightMode.entries.toTypedArray())
