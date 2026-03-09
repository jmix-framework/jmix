/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.saml.mapper.role;

import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.RowLevelRoleRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.*;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class DefaultSamlAssertionRolesMapper extends BaseSamlAssertionRolesMapper {

    private static final Logger log = getLogger(DefaultSamlAssertionRolesMapper.class);

    protected String resourceRolePrefix = "";
    protected String rowLevelRolePrefix = "";

    public DefaultSamlAssertionRolesMapper(RowLevelRoleRepository rowLevelRoleRepository,
                                           ResourceRoleRepository resourceRoleRepository,
                                           RoleGrantedAuthorityUtils roleGrantedAuthorityUtils) {
        super(rowLevelRoleRepository, resourceRoleRepository, roleGrantedAuthorityUtils);
    }

    @Override
    protected Collection<String> getResourceRolesCodes(Assertion assertion) {
        return getRolesCodes(assertion); //todo prefix for resource/row-level roles like in OIDC?
    }

    @Override
    protected Collection<String> getRowLevelRoleCodes(Assertion assertion) {
        return getRolesCodes(assertion);
    }

    protected Collection<String> getRolesCodes(Assertion assertion) {
        Map<String, List<Object>> assertionAttributes = getAssertionAttributes(assertion);
        List<Object> rolesAssertionAttributes = assertionAttributes.get("Role"); //todo parameter
        if (CollectionUtils.isEmpty(rolesAssertionAttributes)) {
            return Collections.emptySet();
        } else {
            return rolesAssertionAttributes.stream()
                    .map(Object::toString)
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    //todo: saml utils?
    protected Map<String, List<Object>> getAssertionAttributes(Assertion assertion) {
        Map<String, List<Object>> attributeMap = new LinkedHashMap<>();
        for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                String attributeName = attribute.getName();
                List<Object> attributeValues = attributeMap.computeIfAbsent(attributeName, k -> new ArrayList<>());
                for (XMLObject xmlObject : attribute.getAttributeValues()) {
                    Object attributeValue = getXmlObjectValue(xmlObject);
                    if (attributeValue != null) {
                        attributeValues.add(attributeValue);
                    }
                }
                attributeMap.put(attributeName, attributeValues);
            }
        }
        return attributeMap;
    }

    protected Object getXmlObjectValue(XMLObject xmlObject) {
        if (xmlObject instanceof XSAny) {
            return ((XSAny) xmlObject).getTextContent();
        }
        if (xmlObject instanceof XSString) {
            return ((XSString) xmlObject).getValue();
        }
        if (xmlObject instanceof XSInteger) {
            return ((XSInteger) xmlObject).getValue();
        }
//        if (xmlObject instanceof XSURI) {
//            return ((XSURI) xmlObject).getURI();
//        }
        if (xmlObject instanceof XSBoolean) {
            XSBooleanValue xsBooleanValue = ((XSBoolean) xmlObject).getValue();
            return (xsBooleanValue != null) ? xsBooleanValue.getValue() : null;
        }
        if (xmlObject instanceof XSDateTime) {
            return ((XSDateTime) xmlObject).getValue();
        }
        return null;
    }

    /*public List<String> getAttributeValues(Attribute attribute) {
        List<String> result = new ArrayList<>();

        for (XMLObject valueObj : attribute.getAttributeValues()) {

            if (valueObj instanceof XSString) {
                result.add(((XSString) valueObj).getValue());

            } else if (valueObj instanceof XSAny) {
                result.add(((XSAny) valueObj).getTextContent());

            } else if (valueObj instanceof XSBoolean) {
                result.add(((XSBoolean) valueObj).getValue().getValue().toString());

            } else if (valueObj instanceof XSInteger) {
                result.add(((XSInteger) valueObj).getValue().toString());

            } else {
                // Fallback: raw text
                try {
                    Element el = XMLObjectSupport.marshall(valueObj);
                    result.add(el.getTextContent());
                } catch (Exception e) {
                    result.add(valueObj.toString());
                }
            }
        }

        return result;
    }*/
}
