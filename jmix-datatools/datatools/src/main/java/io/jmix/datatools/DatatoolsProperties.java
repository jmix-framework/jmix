package io.jmix.datatools;

import io.jmix.datatools.datamodel.app.EngineType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.lang.Nullable;

@ConfigurationProperties(prefix = "jmix.datatools")
public class DatatoolsProperties {

    /**
     * If no configuration options are specified, it defaults to PlantUML with the default options.
     */
    protected final DiagramConstructor diagramConstructor;

    public static class DiagramConstructor {

        /**
         * The server address. This can be either an IP address with custom port or not. It could also be a domain name.
         */
        protected final String host;

        /**
         * Engine type. PlantUML by default
         */
        protected final EngineType engineType;

        public DiagramConstructor(@Nullable String host,
                                  @DefaultValue("plantuml") String engineType) {
            this.host = host;
            this.engineType = EngineType.valueOf(engineType.toUpperCase());
        }

        public EngineType getEngineType() {
            return engineType;
        }

        @Nullable
        public String getHost() {
            return host;
        }
    }

    public DatatoolsProperties(@Nullable DiagramConstructor diagramConstructor) {
        this.diagramConstructor = diagramConstructor == null
                ? new DiagramConstructor(null, "plantuml")
                : diagramConstructor;
    }

    public DiagramConstructor getDiagramConstructor() {
        return diagramConstructor;
    }
}