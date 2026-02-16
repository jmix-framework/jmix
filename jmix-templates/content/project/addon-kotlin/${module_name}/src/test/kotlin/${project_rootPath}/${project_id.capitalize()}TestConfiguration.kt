package ${project_rootPackage}

import io.jmix.core.annotation.JmixModule
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.PropertySource
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType

import javax.sql.DataSource

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(${project_id.capitalize()}Configuration::class)
@PropertySource("classpath:/${project_rootPath}/test-app.properties")
@JmixModule(id = "${project_rootPackage}.test", dependsOn = [${project_id.capitalize()}Configuration::class])
open class ${project_id.capitalize()}TestConfiguration {

    @Bean
    @Primary
    open fun dataSource(): DataSource {
        return EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }
}
