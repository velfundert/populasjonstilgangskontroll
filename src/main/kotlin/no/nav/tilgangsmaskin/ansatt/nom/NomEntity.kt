package no.nav.tilgangsmaskin.ansatt.nom

import jakarta.persistence.*
import jakarta.persistence.GenerationType.IDENTITY
import no.nav.tilgangsmaskin.ansatt.AnsattId.Companion.ANSATTID_LENGTH
import no.nav.tilgangsmaskin.bruker.BrukerId.Companion.BRUKERID_LENGTH
import java.time.Instant

@Entity
@Table(
        name = "ansatte",
        indexes = [Index(name = "idx_gyldig", columnList = "gyldigtil")],
        uniqueConstraints = [UniqueConstraint(name = "uc_ansattentity_navid", columnNames = ["navid"])])
class NomEntity(
        @Column(length = ANSATTID_LENGTH, nullable = false) val navid: String,
        @Column(length = BRUKERID_LENGTH, nullable = false) var fnr: String,
        @Column(nullable = false) var startdato: Instant,
        @Column(nullable = false) var gyldigtil: Instant) {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null

    @Column(nullable = false, updatable = false)
    var created: Instant? = null

    @Column(nullable = false)
    var updated: Instant? = null
}