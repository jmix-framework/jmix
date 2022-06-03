package io.jmix.oidc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.oidc")
@ConstructorBinding
public class OidcProperties {

    /**
     * Whether default OIDC configuration should be used. The property is used by OidcAutoConfiguration.
     */
    boolean useDefaultConfiguration;

    /**
     * DefaultClaimsRolesMapper configuration.
     */
    DefaultClaimsRolesMapperConfig defaultClaimsRolesMapper;

    /**
     * JWT authentication converter configuration
     */
    JwtAuthenticationConverterConfig jwtAuthenticationConverter;

    public OidcProperties(
            @DefaultValue("true") boolean useDefaultConfiguration,
            @DefaultValue DefaultClaimsRolesMapperConfig defaultClaimsRolesMapper,
            @DefaultValue JwtAuthenticationConverterConfig jwtAuthenticationConverter
    ) {
        this.useDefaultConfiguration = useDefaultConfiguration;
        this.defaultClaimsRolesMapper = defaultClaimsRolesMapper;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    public boolean isUseDefaultConfiguration() {
        return useDefaultConfiguration;
    }

    public DefaultClaimsRolesMapperConfig getDefaultClaimsRolesMapper() {
        return defaultClaimsRolesMapper;
    }

    public JwtAuthenticationConverterConfig getJwtAuthenticationConverter() {
        return jwtAuthenticationConverter;
    }

    public static class DefaultClaimsRolesMapperConfig {
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

        public DefaultClaimsRolesMapperConfig(
                @DefaultValue("roles") String rolesClaimName,
                @DefaultValue("") String resourceRolePrefix,
                @DefaultValue("") String rowLevelRolePrefix
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

    public static class JwtAuthenticationConverterConfig {

        /**
         * JWT claim name that is used as a username of Jmix user
         */
        private String usernameClaim;

        public JwtAuthenticationConverterConfig(
                @DefaultValue("sub") String usernameClaim) {
            this.usernameClaim = usernameClaim;
        }

        public String getUsernameClaim() {
            return usernameClaim;
        }
    }
}