package no.nav.tilgangsmaskin.ansatt

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.Tags
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.tilgangsmaskin.ansatt.GlobalGruppe.NASJONAL
import no.nav.tilgangsmaskin.ansatt.nom.NomTjeneste
import no.nav.tilgangsmaskin.bruker.BrukerTjeneste
import no.nav.tilgangsmaskin.regler.motor.NasjonalGruppeTeller
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service

@Service
@Timed
@ConditionalOnGCP
class AnsattTjeneste(private val ansatte: NomTjeneste,
                     private val brukere: BrukerTjeneste,
                     private val resolver: AnsattGruppeResolver,
                     private val teller: NasjonalGruppeTeller) {



    fun ansatt(ansattId: AnsattId) =
        Ansatt(ansattId,ansattBruker(ansattId), ansattGrupper(ansattId)).also {
            tell(it erMedlemAv NASJONAL)
        }

    private fun ansattGrupper(ansattId: AnsattId) = resolver.grupperForAnsatt(ansattId)

    private fun ansattBruker(ansattId: AnsattId) =
        ansatte.fnrForAnsatt(ansattId)?.let {
            runCatching { brukere.brukerMedUtvidetFamilie(it.verdi) }.getOrNull()
        }

    private fun tell(status: Boolean) = teller.tell(Tags.of(MEDLEM,"$status"))

    companion object {
        private const val MEDLEM = "medlem"
    }
}





