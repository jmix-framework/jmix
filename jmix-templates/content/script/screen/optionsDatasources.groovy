def isLookupTypeDropdown(attr) {
    def lookupAnn = attr.getAnnotation('Lookup')
    return (lookupAnn != null
            && lookupAnn.params['type']?.endsWith('DROPDOWN')
            && (attr.hasAnnotation('OneToOne') || attr.hasAnnotation('ManyToOne')))
}

/*
 * Copyright 2022 Haulmont.
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

def result = []
def viewEntity = view.entity
view.allProperties.each { prop ->
    def attr = viewEntity.getAttribute(prop.name)
    if (attr == null) {
        return
    }

    if (isLookupTypeDropdown(attr)) {
        result << attr
    }

    if (prop.entity != null && prop.entity.isEmbeddable()) {
        prop.subProperties.each { embProp ->
            def embPropAttr = prop.entity.getAttribute(embProp.name)
            if (embPropAttr.allowPutToDataAware()
                    && !embPropAttr.hasAnnotation('Embedded')
                    && isLookupTypeDropdown(embPropAttr)) {
                result << embPropAttr
            }
        }
    }
}

return result