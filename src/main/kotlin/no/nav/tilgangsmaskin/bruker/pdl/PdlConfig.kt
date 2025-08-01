package no.nav.tilgangsmaskin.bruker.pdl

import no.nav.tilgangsmaskin.bruker.pdl.PdlConfig.Companion.PDL
import no.nav.tilgangsmaskin.felles.rest.AbstractRestConfig
import no.nav.tilgangsmaskin.felles.rest.CachableRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI
import java.time.Duration


@ConfigurationProperties(PDL)
class PdlConfig(
    baseUri: URI,
    pingPath: String = DEFAULT_PING_PATH,
    personPath: String = DEFAULT_PERSON_PATH,
    personBolkPath: String = DEFAULT_PERSON__BOLK_PATH,
    enabled: Boolean = true) : CachableRestConfig, AbstractRestConfig(baseUri, pingPath, PDL, enabled) {

    override val navn = name
    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    val personURI = uri(personPath)
    val personerURI = uri(personBolkPath)

    companion object {
        const val PDL = "pdl"
        private const val DEFAULT_PING_PATH = "/internal/health/liveness"
        private val DEFAULT_PERSON_PATH = "/api/v1/person"
        private val DEFAULT_PERSON__BOLK_PATH = "/api/v1/personBolk"

    }
}

