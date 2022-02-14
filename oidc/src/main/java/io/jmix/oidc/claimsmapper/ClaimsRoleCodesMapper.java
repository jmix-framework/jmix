package io.jmix.oidc.claimsmapper;

import java.util.Collection;
import java.util.Map;

//todo user Converter<Map<String, Object>, Collection<String>> instead?
@FunctionalInterface
public interface ClaimsRoleCodesMapper {

    Collection<String> toRoleCodes(Map<String, Object> claims);
}
