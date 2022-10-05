
package test_support.role;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import test_support.entity.Bar;
import test_support.entity.ExtBar;

@ResourceRole(code = TestExtBarRole.CODE, name = "Test ExtBar role")
public interface TestExtBarRole {

    String CODE = "test-ext-bar-role";

    @EntityPolicy(entityClass = Bar.class,
            actions = {EntityPolicyAction.CREATE})
    @EntityAttributePolicy(entityClass = Bar.class, attributes = {"name"}, action = EntityAttributePolicyAction.MODIFY)
    void bar();

    @EntityPolicy(entityClass = ExtBar.class,
            actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = ExtBar.class, attributes = {"description"}, action = EntityAttributePolicyAction.VIEW)
    void extBar();

}
