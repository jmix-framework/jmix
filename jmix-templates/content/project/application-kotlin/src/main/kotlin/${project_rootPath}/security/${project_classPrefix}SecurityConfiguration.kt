package ${project_rootPackage}.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.web.SecurityFilterChain

/**
 * This configuration complements standard security configurations that come from Jmix modules (security-flowui, oidc,
 * authserver).
 * <p>
 * You can configure custom API endpoints security by defining [SecurityFilterChain] beans in this class.
 * In most cases, custom SecurityFilterChain must be applied first, so the proper
 * [org.springframework.core.annotation.Order] should be defined for the bean. The order value from the
 * [io.jmix.core.JmixSecurityFilterChainOrder#CUSTOM] is guaranteed to be smaller than any other filter chain
 * order from Jmix.
 *
 * @see io.jmix.securityflowui.security.FlowuiVaadinWebSecurity
 */
@Configuration
class ${project_classPrefix}SecurityConfiguration {

}