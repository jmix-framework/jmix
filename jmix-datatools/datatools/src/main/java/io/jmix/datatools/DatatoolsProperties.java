package io.jmix.datatools;

import io.jmix.datatools.datamodel.EngineType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.lang.Nullable;

@ConfigurationProperties(prefix = "jmix.datatools")
public class DatatoolsProperties {

    /**
     * If no configuration options are specified, it defaults to PlantUML with the default options.
     */
    protected final DataModelDiagram dataModelDiagram;

    public static class DataModelDiagram {

        /**
         * The server address. This can be either an IP address with custom port or not. It could also be a domain name.
         */
        protected final String host;

        /**
         * Engine type. PlantUML by default
         */
        protected final EngineType engineType;

        public DataModelDiagram(@Nullable String host,
                                @DefaultValue("PLANTUML") EngineType engineType) {
            this.host = host;
            this.engineType = engineType;
        }

        public EngineType getEngineType() {
            return engineType;
        }

        @Nullable
        public String getHost() {
            return host;
        }
    }

    public DatatoolsProperties(@Nullable DataModelDiagram dataModelDiagram) {
        this.dataModelDiagram = dataModelDiagram == null
                ? new DataModelDiagram(null, EngineType.PLANTUML)
                : dataModelDiagram;
    }

    public DataModelDiagram getDiagramConstructor() {
        return dataModelDiagram;
    }
}