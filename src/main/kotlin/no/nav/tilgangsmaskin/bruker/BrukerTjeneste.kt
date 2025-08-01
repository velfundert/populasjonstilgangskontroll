package no.nav.tilgangsmaskin.bruker

import io.micrometer.core.annotation.Timed
import no.nav.tilgangsmaskin.ansatt.skjerming.SkjermingTjeneste
import no.nav.tilgangsmaskin.bruker.PersonTilBrukerMapper.tilBruker
import no.nav.tilgangsmaskin.bruker.pdl.PDLTjeneste
import no.nav.tilgangsmaskin.bruker.pdl.Person
import no.nav.tilgangsmaskin.felles.utils.extensions.DomainExtensions.maskFnr
import no.nav.tilgangsmaskin.felles.utils.extensions.DomainExtensions.pluralize
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service

@Service
@Timed
class BrukerTjeneste(private val personTjeneste: PDLTjeneste, val skjermingTjeneste: SkjermingTjeneste) {

    private val log = getLogger(javaClass)


    fun brukere(brukerIds: Set<String>) : Set<Bruker> {
        if (brukerIds.isEmpty()) {
            log.debug("${"bruker".pluralize(brukerIds, ingen = "Ingen")} å slå opp")
            return emptySet()
        }
        log.debug("Slår opp ${"bruker".pluralize(brukerIds,"e")}: ${brukerIds.joinToString { it.maskFnr() }}")
        val personer =  personTjeneste.personer(brukerIds)
        val notFound = brukerIds - personer.map { it.brukerId.verdi }.toSet()
        val found =  personer.map { it.brukerId }.toSet()
        if (notFound.isNotEmpty()) {
            log.debug("Fant ikke ${"person".pluralize(notFound)}: ${notFound.joinToString { it.maskFnr() }}")
        }
        if (found.isNotEmpty()) {
            log.info("Bulk slo opp  ${"person".pluralize(found)} ${found.joinToString { it.verdi.maskFnr() }}")
        }

        return found.let { p ->
            if (p.isNotEmpty()) {
                log.info("Bulk slår opp ${"skjerming".pluralize(p)} for $p")
                val skjerminger = skjermingTjeneste.skjerminger(p)
                log.info("Bulk slo opp ${"skjerming".pluralize(skjerminger.keys)} for ${p.joinToString { it.verdi.maskFnr() }}")
                personer.map {
                    tilBruker(it, skjerminger[it.brukerId] ?: false)
                }
            } else {
                log.debug("${"skjerming".pluralize(p, ingen = "Ingen")} å slå opp")
                emptyList()
            }.toSet()
        }
    }

    fun brukerMedNærmesteFamilie(brukerId: String) =
        brukerMedSkjerming(brukerId, personTjeneste::medNærmesteFamilie)

    fun brukerMedUtvidetFamilie(brukerId: String) =
        brukerMedSkjerming(brukerId, personTjeneste::medUtvidetFamile)

    private fun brukerMedSkjerming(id: String, hentFamilie: (String) -> Person) =
        with(hentFamilie(id)) {
            tilBruker(this, skjermingTjeneste.skjerming(brukerId))
        }
            /*
            val statuser = skjermingTjeneste.skjerminger(historiskeIds + brukerId)
            statuser.filterValues { it }.forEach { (brukerId, _) ->
                if (brukerId.verdi != id) {
                    log.info("Bruker $brukerId er skjermet grunnet historikk")
                }
            }
            tilBruker(this, skjermingTjeneste.skjerming(this.brukerId))
        }*/
}
