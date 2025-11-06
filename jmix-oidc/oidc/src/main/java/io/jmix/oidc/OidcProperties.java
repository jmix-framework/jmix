package io.jmix.oidc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "jmix.oidc")
public class OidcProperties {

    /**
     * Whether default OIDC configuration should be used. The property is used by OidcAutoConfiguration.
     */
    boolean useDefaultConfiguration;

    /**
     * Post logout redirect uri template for OIDC logout. The property is used by OidcAutoConfiguration.
     */
    String postLogoutRedirectUri;

    /**
     * DefaultClaimsRolesMapper configuration.
     */
    DefaultClaimsRolesMapperConfig defaultClaimsRolesMapper;

    /**
     * JWT authentication converter configuration.
     */
    JwtAuthenticationConverterConfig jwtAuthenticationConverter;

    //todo javadoc
    FilterChain filterChain;

    public OidcProperties(
            @DefaultValue("true") boolean useDefaultConfiguration,
            @DefaultValue("{baseUrl}") String postLogoutRedirectUri,
            @DefaultValue DefaultClaimsRolesMapperConfig defaultClaimsRolesMapper,
            @DefaultValue JwtAuthenticationConverterConfig jwtAuthenticationConverter,
            @DefaultValue FilterChain filterChain
    ) {
        this.useDefaultConfiguration = useDefaultConfiguration;
        this.postLogoutRedirectUri = postLogoutRedirectUri;
        this.defaultClaimsRolesMapper = defaultClaimsRolesMapper;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.filterChain = filterChain;
    }

    public boolean isUseDefaultConfiguration() {
        return useDefaultConfiguration;
    }

    public String getPostLogoutRedirectUri() {
        return postLogoutRedirectUri;
    }

    public DefaultClaimsRolesMapperConfig getDefaultClaimsRolesMapper() {
        return defaultClaimsRolesMapper;
    }

    public JwtAuthenticationConverterConfig getJwtAuthenticationConverter() {
        return jwtAuthenticationConverter;
    }

    public FilterChain getFilterChain() {
        return filterChain;
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

    //todo javadoc
    public static class FilterChain {

        boolean forceApiScopeEnabled;

        boolean forceUiScopeEnabled;

        List<String> apiScopeSecurityFilterChainNames;

        List<String> uiScopeSecurityFilterChainNames;

        public FilterChain(@DefaultValue("true") boolean forceApiScopeEnabled,
                           @DefaultValue("true") boolean forceUiScopeEnabled,
                           @DefaultValue("oidc_JwtSecurityFilterChain") List<String> apiScopeSecurityFilterChainNames,
                           @DefaultValue("VaadinSecurityFilterChainBean") List<String> uiScopeSecurityFilterChainNames) {
            this.forceApiScopeEnabled = forceApiScopeEnabled;
            this.apiScopeSecurityFilterChainNames = apiScopeSecurityFilterChainNames;
            this.forceUiScopeEnabled = forceUiScopeEnabled;
            this.uiScopeSecurityFilterChainNames = uiScopeSecurityFilterChainNames;
        }

        public boolean isForceApiScopeEnabled() {
            return forceApiScopeEnabled;
        }

        public boolean isForceUiScopeEnabled() {
            return forceUiScopeEnabled;
        }

        public List<String> getApiScopeSecurityFilterChainNames() {
            return apiScopeSecurityFilterChainNames;
        }

        public List<String> getUiScopeSecurityFilterChainNames() {
            return uiScopeSecurityFilterChainNames;
        }
    }
}