/*
 * Copyright 2025 Haulmont.
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

package io.jmix.searchflowui.view.result

import io.jmix.core.MessageTools
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import io.jmix.core.metamodel.model.MetaProperty
import io.jmix.core.metamodel.model.MetaPropertyPath
import io.jmix.flowui.view.MessageBundle
import spock.lang.Specification
import io.jmix.core.metamodel.model.MetaClass


class SearchResultFieldFormatterTest extends Specification {

    def "simple field"() {
        given:
        def fieldName = "someFieldName"
        def metaProperty = Mock(MetaProperty)
        def entityName = "some_EntityName"
        def metaClass = Mock(MetaClass)

        and:
        def metadataTools = Mock(MetadataTools)
        def metaPropertyPath = Mock(MetaPropertyPath)
        metaPropertyPath.getMetaProperties() >> [metaProperty].toArray()
        metadataTools.resolveMetaPropertyPathOrNull(metaClass, fieldName) >> metaPropertyPath

        and:
        def metadata = Mock(Metadata)
        metadata.getClass(entityName) >> metaClass

        and:
        def messageTools = Mock(MessageTools)
        messageTools.getPropertyCaption(metaProperty) >> "someFieldNameCaption"

        when:
        def formatter = new SearchResultFieldFormatter(metadata, messageTools, metadataTools)
        def fieldCaption = formatter.formatFieldCaption(entityName, fieldName, Mock(MessageBundle))

        then:
        fieldCaption == "someFieldNameCaption"
    }

    def "a field with two levels"() {
        given:
        def fieldName = "firstLevelFieldName.secondLevelFieldName"
        def metaProperty1 = Mock(MetaProperty)
        def metaProperty2 = Mock(MetaProperty)
        def entityName = "some_EntityName"
        def metaClass = Mock(MetaClass)

        and:
        def metadataTools = Mock(MetadataTools)
        def metaPropertyPath = Mock(MetaPropertyPath)
        metaPropertyPath.getMetaProperties() >> [metaProperty1, metaProperty2].toArray()
        metadataTools.resolveMetaPropertyPathOrNull(metaClass, fieldName) >> metaPropertyPath

        and:
        def metadata = Mock(Metadata)
        metadata.getClass(entityName) >> metaClass

        and:
        def messageTools = Mock(MessageTools)
        messageTools.getPropertyCaption(metaProperty1) >> "firstLevelFieldCaption"
        messageTools.getPropertyCaption(metaProperty2) >> "secondLevelFieldCaption"

        when:
        def formatter = new SearchResultFieldFormatter(metadata, messageTools, metadataTools)
        def fieldCaption = formatter.formatFieldCaption(entityName, fieldName, Mock(MessageBundle))

        then:
        fieldCaption == "firstLevelFieldCaption.secondLevelFieldCaption"
    }

    def "a field with two levels +"() {
        given:
        def fieldName = "firstLevelFieldName.secondLevelFieldName._file_name"
        def metaProperty1 = Mock(MetaProperty)
        def metaProperty2 = Mock(MetaProperty)
        def entityName = "some_EntityName"
        def metaClass = Mock(MetaClass)

        and:
        def metadataTools = Mock(MetadataTools)
        def metaPropertyPath = Mock(MetaPropertyPath)
        metaPropertyPath.getMetaProperties() >> [metaProperty1, metaProperty2].toArray()
        metadataTools.resolveMetaPropertyPathOrNull(metaClass, fieldName) >> metaPropertyPath

        and:
        def metadata = Mock(Metadata)
        metadata.getClass(entityName) >> metaClass

        and:
        def messageTools = Mock(MessageTools)
        messageTools.getPropertyCaption(metaProperty1) >> "firstLevelFieldCaption"
        messageTools.getPropertyCaption(metaProperty2) >> "secondLevelFieldCaption"

        and:
        def messageBundle = Mock(MessageBundle)
        messageBundle.getMessage(SearchResultFieldFormatter.SYSTEM_FIELD_LABELS.get("_file_name")) >> "File name"

        when:
        def formatter = new SearchResultFieldFormatter(metadata, messageTools, metadataTools)
        def fieldCaption = formatter.formatFieldCaption(entityName, fieldName, messageBundle)

        then:
        fieldCaption == "firstLevelFieldCaption.secondLevelFieldCaption.[File name]"
    }
}
