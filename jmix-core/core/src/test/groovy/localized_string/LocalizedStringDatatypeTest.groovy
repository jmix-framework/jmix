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

package localized_string

import io.jmix.core.CoreConfiguration
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import io.jmix.core.impl.MetaModelLoader
import io.jmix.core.metamodel.model.impl.SessionImpl
import localized_string.misassigned.ExplicitDateDatatypeEntity
import localized_string.misassigned.MisassignedLocalizedStringEntity
import io.jmix.core.metamodel.annotation.LocalizedString
import io.jmix.core.metamodel.datatype.DatatypeRegistry
import io.jmix.core.metamodel.datatype.impl.DateDatatype
import io.jmix.core.metamodel.datatype.impl.LocalizedStringDatatype
import io.jmix.core.metamodel.datatype.impl.StringDatatype
import io.jmix.core.security.ClientDetails
import io.jmix.core.security.SecurityContextHelper
import io.jmix.core.security.SystemAuthenticationToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.base.TestBaseConfiguration
import test_support.localized_string.entity.TestLocalizedEntity
import test_support.localized_string.TestLocalizedStringConfiguration

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration, TestLocalizedStringConfiguration])
class LocalizedStringDatatypeTest extends Specification {

    static final Locale DE = Locale.GERMAN

    @Autowired
    DatatypeRegistry datatypeRegistry
    @Autowired
    Metadata metadata
    @Autowired
    MetadataTools metadataTools
    @Autowired
    MetaModelLoader metaModelLoader

    void cleanup() {
        SecurityContextHelper.setAuthentication(null)
    }

    def "the datatype is registered by id and does not replace the default String datatype"() {
        expect:
        datatypeRegistry.get(LocalizedStringDatatype.ID) instanceof LocalizedStringDatatype
        datatypeRegistry.find(String).class == StringDatatype
        datatypeRegistry.getIdByJavaClass(String) == 'string'
    }

    def "a localized string on a property that is not a String fails while the metamodel is built"() {
        when:
        metaModelLoader.loadModel(new SessionImpl(), [MisassignedLocalizedStringEntity.name] as Set)

        then: "the declaration is reported, instead of a ClassCastException at the first read of the property"
        def e = thrown(IllegalStateException)
        e.message.contains('test_MisassignedLocalizedString.count')
        e.message.contains(Integer.name)
    }

    def "an explicitly assigned datatype other than the localized string is not checked against the property type"() {
        given: "date declares java.sql.Date as its Java class and works with java.util.Date properties"
        def session = new SessionImpl()

        when:
        metaModelLoader.loadModel(session, [ExplicitDateDatatypeEntity.name] as Set)

        then:
        session.getClass(ExplicitDateDatatypeEntity).getProperty('day').range.asDatatype() instanceof DateDatatype
    }

    def "format with a locale resolves, format without a locale returns the raw value, parse returns a literal"() {
        given:
        def datatype = datatypeRegistry.get(LocalizedStringDatatype.ID)
        def raw = 'Default\nen=English\nde=Deutsch'

        expect:
        datatype.format(raw, DE) == 'Deutsch'
        datatype.format(raw, Locale.FRENCH) == 'English'
        datatype.format(raw) == raw
        datatype.format(null) == ''
        datatype.parse('typed', DE) == 'typed'
        datatype.parse('typed') == 'typed'
    }

    def "@LocalizedString assigns the datatype through the meta-annotated @PropertyDatatype and leaves a marker"() {
        given:
        def metaClass = metadata.getClass(TestLocalizedEntity)
        def name = metaClass.getProperty('name')
        def plain = metaClass.getProperty('plain')

        expect:
        name.range.isDatatype()
        name.range.asDatatype() instanceof LocalizedStringDatatype
        name.javaType == String
        name.annotations.containsKey(LocalizedString.name)
        plain.range.asDatatype().class == StringDatatype
    }

    def "MetadataTools.format and the instance name show the current user's locale"() {
        given:
        def entity = metadata.create(TestLocalizedEntity)
        entity.name = 'Default\nen=English\nde=Deutsch'
        def token = new SystemAuthenticationToken(null)
        token.details = ClientDetails.builder().locale(DE).build()
        SecurityContextHelper.setAuthentication(token)

        expect:
        metadataTools.format(entity.name, metadata.getClass(TestLocalizedEntity).getProperty('name')) == 'Deutsch'
        metadataTools.getInstanceName(entity) == 'Deutsch'
    }
}
