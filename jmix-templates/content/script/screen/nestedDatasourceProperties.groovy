result = []
def viewEntity = view.entity

view.orderedRootProperties.each {property ->
    if (property.entity != null && property.entity.isEmbeddable() && embeddable) {
        result << property.name
    } else {
        def propAttr = viewEntity.getAttribute(property.name)
        if (propAttr != null && propAttr.hasAnnotation('Composition') && propAttr.hasAnnotation('OneToOne')) {
            result << property.name
        }
    }
}

return result