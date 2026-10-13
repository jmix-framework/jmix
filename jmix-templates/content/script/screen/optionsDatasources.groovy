// entityComboBox from a DROPDOWN @LookupField without itemsQuery needs a collection container for its items
def isLookupTypeDropdown(attr) {
    return attr.isEntityType() && attr.isLookupItemsContainerRequired()
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