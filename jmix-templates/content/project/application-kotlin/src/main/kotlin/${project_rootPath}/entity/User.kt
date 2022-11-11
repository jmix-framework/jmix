package ${project_rootPackage}.entity

import io.jmix.core.HasTimeZone
import io.jmix.core.annotation.Secret
import io.jmix.core.entity.annotation.JmixGeneratedValue
import io.jmix.core.entity.annotation.SystemLevel
import io.jmix.core.metamodel.annotation.DependsOnProperties
import io.jmix.core.metamodel.annotation.InstanceName
import io.jmix.core.metamodel.annotation.JmixEntity
import io.jmix.security.authentication.JmixUserDetails
import org.springframework.security.core.GrantedAuthority
import java.util.*
import javax.persistence.*
import javax.validation.constraints.Email

@JmixEntity
@Entity<%if(!normalizedPrefix_underscore.isEmpty()) {%>(name = "${normalizedPrefix_underscore}User")<%}%>
@Table(name = "${userTable}", indexes = [
    Index(name = "IDX_${userTable}_ON_USERNAME", columnList = "USERNAME", unique = true)
])
open class User : JmixUserDetails, HasTimeZone {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    var id: UUID? = null

    @Version
    @Column(name = "VERSION", nullable = false)
    var version: Int? = null

    @Column(name = "USERNAME", nullable = false)
    @get:JvmName("getUsername_")
    var username: String? = null

    @Secret
    @SystemLevel
    @Column(name = "PASSWORD")
    @get:JvmName("getPassword_")
    var password: String? = null

    @Column(name = "FIRST_NAME")
    var firstName: String? = null

    @Column(name = "LAST_NAME")
    var lastName: String? = null

    @Email
    @Column(name = "EMAIL")
    var email: String? = null

    @Column(name = "ACTIVE")
    var active: Boolean? = true

    @Column(name = "TIME_ZONE_ID")
    @get:JvmName("getTimeZoneId_")
    var timeZoneId: String? = null;

    @Transient
    protected var userAuthorities: Collection<GrantedAuthority?>? = null

    override fun getPassword(): String? = password

    override fun getUsername(): String? = username

    override fun getAuthorities(): Collection<GrantedAuthority?> =
        userAuthorities ?: emptyList()

    override fun setAuthorities(authorities: Collection<GrantedAuthority?>) {
        this.userAuthorities = authorities
    }

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = active == true

    @get:DependsOnProperties("firstName", "lastName", "username")
    @get:InstanceName
    val displayName: String
        get() = "\${firstName ?: ""} \${lastName ?: ""} [\${username ?: ""}]".trim()

    override fun getTimeZoneId(): String? {
        return timeZoneId
    }
}