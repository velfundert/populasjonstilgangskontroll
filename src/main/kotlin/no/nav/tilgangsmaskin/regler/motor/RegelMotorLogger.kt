package no.nav.tilgangsmaskin.regler.motor

import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.tilgangsmaskin.ansatt.Ansatt
import no.nav.tilgangsmaskin.bruker.Bruker
import no.nav.tilgangsmaskin.felles.rest.ConsumerAwareHandlerInterceptor.Companion.CONSUMER_ID
import no.nav.tilgangsmaskin.felles.utils.secureLog
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.MDC
import org.springframework.stereotype.Component
import no.nav.tilgangsmaskin.tilgang.Token

@Component
class RegelMotorLogger(private val registry: MeterRegistry, private val token: Token) {

    private val teller: AvvisningTeller = AvvisningTeller(registry, token)
    private val bulkHistogram: DistributionSummary = DistributionSummary
        .builder("bulk.histogram")
        .description("Histogram av bulk-størrelse")
        .publishPercentileHistogram(true)
        .serviceLevelObjectives(10.0, 20.0, 50.0, 100.0, 200.0, 500.0, 1000.0)
        .register(registry)

    private val log = getLogger(javaClass)
    fun avvist(ansatt: Ansatt, bruker: Bruker, regel: Regel) {
        MDC.put(BESLUTNING,regel.kode)
        val fra =  MDC.get(CONSUMER_ID)?.let { "fra $it" } ?: "(fra uautentisert konsument)"
        log.warn("Tilgang avvist av regel '${regel.kortNavn}'. (${regel.begrunnelse}) for ${ansatt.ansattId} for $bruker $fra")
        secureLog.warn("Tilgang til ${bruker.brukerId.verdi} avvist av regel '${regel.kortNavn}' for ${ansatt.ansattId} $fra")
        teller.tell(Tags.of("navn", regel.navn))
        MDC.remove(BESLUTNING)
    }

    fun ok(ansatt: Ansatt, bruker: Bruker,regelSett: RegelSett) {
        MDC.put(BESLUTNING,OK)
        val fra =  MDC.get(CONSUMER_ID)?.let { "fra $it" } ?: "(fra uautentisert konsument)"
        log.info("${regelSett.beskrivelse} ga tilgang for ${ansatt.ansattId} $fra")
        secureLog.info("${regelSett.beskrivelse} ga tilgang til  ${bruker.brukerId.verdi}  for ${ansatt.ansattId} $fra")
        MDC.remove(BESLUTNING)
    }

    fun info(message: String) = log.info(message)

    fun warn(message: String, e: Throwable? = null) = log.warn(message,e)

    fun trace(message: String) = log.trace(message)

    fun evaluerer(ansatt: Ansatt, bruker: Bruker, regel: Regel) =
        log.trace("Evaluerer regel: '{}' for {}  og {}", regel.kortNavn, ansatt.ansattId, bruker.brukerId)

    fun tellBulkSize(size: Int) =   registry.summary("bulk.histogram", "system", token.system)
        .record(size.toDouble())

    companion object   {
        private const val BESLUTNING = "beslutning"
        private const val OK = "TILGANG_OK"
    }
}



