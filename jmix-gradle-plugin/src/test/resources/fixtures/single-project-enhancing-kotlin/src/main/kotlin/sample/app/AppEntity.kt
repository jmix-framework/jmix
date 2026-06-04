package sample.app

import io.jmix.core.entity.annotation.JmixGeneratedValue
import io.jmix.core.metamodel.annotation.JmixEntity
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@JmixEntity
@Entity(name = "sample_AppEntity")
@Table(name = "SAMPLE_APP_ENTITY")
open class AppEntity {

    @Id
    @JmixGeneratedValue
    var id: UUID? = null
}
