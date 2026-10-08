package ${packageName}

import io.jmix.flowui.sys.registration.ComponentRegistration
import io.jmix.flowui.sys.registration.ComponentRegistrationBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class ${registrationClassName} {

    @Bean
    open fun ${xmlElement}(): ComponentRegistration =
        ComponentRegistrationBuilder.create(${componentClassName}::class.java)
            .withComponentLoader("${xmlElement}", ${loaderClassName}::class.java)
            .build()
}
