package ${project_rootPackage}.security

import io.jmix.security.model.EntityAttributePolicyAction
import io.jmix.security.model.EntityPolicyAction
import io.jmix.security.role.annotation.EntityAttributePolicy
import io.jmix.security.role.annotation.EntityPolicy
import io.jmix.security.role.annotation.ResourceRole
import io.jmix.security.role.annotation.SpecificPolicy
import io.jmix.securityui.role.annotation.MenuPolicy
import io.jmix.securityui.role.annotation.ScreenPolicy

@ResourceRole(name = "Full Access", code = FullAccessRole.CODE)
interface FullAccessRole {

    companion object {
        const val CODE = "system-full-access"
    }

    @EntityPolicy(entityName = "*", actions = [EntityPolicyAction.ALL])
    @EntityAttributePolicy(entityName = "*", attributes = ["*"], action = EntityAttributePolicyAction.MODIFY)
    @ScreenPolicy(screenIds = ["*"])
    @MenuPolicy(menuIds = ["*"])
    @SpecificPolicy(resources = ["*"])
    fun fullAccess()
}