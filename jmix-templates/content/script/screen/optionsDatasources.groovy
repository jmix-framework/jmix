def isLookupTypeDropdown(attr) {
    def lookupAnn = attr.getAnnotation('Lookup')
    return (lookupAnn != null
            && lookupAnn.params['type']?.endsWith('DROPDOWN')
            && (attr.hasAnnotation('OneToOne') || attr.hasAnnotation('ManyToOne')))
}

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