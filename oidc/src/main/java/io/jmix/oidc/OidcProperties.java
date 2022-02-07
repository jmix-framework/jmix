package io.jmix.oidc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.oidc")
@ConstructorBinding
public class OidcProperties {

    /**
     * Whether default OIDC configuration should be used. Used by OidcAutoConfiguration.
     */
    boolean useDefaultConfiguration;

    ClaimsMapper claimsMapper;

    public OidcProperties(
            @DefaultValue("true") boolean useDefaultConfiguration,
            ClaimsMapper claimsMapper
    ) {
        this.useDefaultConfiguration = useDefaultConfiguration;
        this.claimsMapper = claimsMapper;
    }

    public boolean isUseDefaultConfiguration() {
        return useDefaultConfiguration;
    }

    public ClaimsMapper getClaimsMapper() {
        return claimsMapper;
    }

    public static class ClaimsMapper {
        /**
         * Prefix that can be used to recognize a resource role from the authorization server
         */
        String resourceRolePrefix;

        /**
         * Prefix that can be used to recognize a row-level role from the authorization server
         */
        String rowLevelRolePrefix;

        /**
         * OidcUser claim name that stores a collection of roles names
         */
        String rolesClaimName;

        public ClaimsMapper(
                @DefaultValue("resource$") String resourceRolePrefix,
                @DefaultValue("row-level$") String rowLevelRolePrefix,
                @DefaultValue("roles") String rolesClaimName
        ) {
            this.resourceRolePrefix = resourceRolePrefix;
            this.rowLevelRolePrefix = rowLevelRolePrefix;
            this.rolesClaimName = rolesClaimName;
        }

        public String getResourceRolePrefix() {
            return resourceRolePrefix;
        }

        public String getRowLevelRolePrefix() {
            return rowLevelRolePrefix;
        }

        public String getRolesClaimName() {
            return rolesClaimName;
        }
    }
}