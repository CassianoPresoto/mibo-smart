package intelbras.mobi.smart.rest

import kotlinx.serialization.Serializable

internal const val TEST_ROUTE = "/test/resource/v1"

@Serializable
internal data class TestRequest(val ping: String = "pong")

@Serializable
internal data class TestResource(val id: String, val label: String = "")
