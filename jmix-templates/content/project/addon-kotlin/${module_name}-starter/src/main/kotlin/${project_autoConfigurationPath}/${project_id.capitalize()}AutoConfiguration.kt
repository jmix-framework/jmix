package ${project_autoConfigurationPackage}

import ${project_rootPackage}.${project_id.capitalize()}Configuration
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Import

@AutoConfiguration
@Import(${project_id.capitalize()}Configuration::class)
open class ${project_id.capitalize()}AutoConfiguration {

}

