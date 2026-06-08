package io.jmix.datatools;

import io.jmix.datatools.datamodel.EngineType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.jspecify.annotations.Nullable;

@ConfigurationProperties(prefix = "jmix.datatools")
public class DatatoolsProperties {

    /**
     * Configuration for data model diagram generation.
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

        /**
         * Whether the public PlantUML server ({@code https://www.plantuml.com}) may be used to render data model
         * diagrams when no private {@link #host} is configured. When a private host is set, this property has no
         * effect. This is also gated by the global {@code jmix.core.unsafe-runtime-features-enabled} switch: if that
         * switch is disabled, the public server is not used regardless of this property.
         */
        protected final boolean publicServerEnabled;

        public DataModelDiagram(@Nullable String host,
                                @DefaultValue("PLANTUML") EngineType engineType,
                                @DefaultValue("true") boolean publicServerEnabled) {
            this.host = host;
            this.engineType = engineType;
            this.publicServerEnabled = publicServerEnabled;
        }

        public EngineType getEngineType() {
            return engineType;
        }

        @Nullable
        public String getHost() {
            return host;
        }

        /**
         * @see #publicServerEnabled
         */
        public boolean isPublicServerEnabled() {
            return publicServerEnabled;
        }
    }

    public DatatoolsProperties(@Nullable DataModelDiagram dataModelDiagram) {
        this.dataModelDiagram = dataModelDiagram == null
                ? new DataModelDiagram(null, EngineType.PLANTUML, true)
                : dataModelDiagram;
    }

    /**
     * @see #dataModelDiagram
     */
    public DataModelDiagram getDataModelDiagram() {
        return dataModelDiagram;
    }
}