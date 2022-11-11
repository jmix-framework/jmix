result = []
def viewEntity = view.entity

view.orderedRootProperties.each {property ->
    def propAttr = viewEntity.getAttribute(property.name)
    if (propAttr != null && (propAttr.hasAnnotation('ManyToMany')
            || (propAttr.hasAnnotation('OneToMany') && propAttr.hasAnnotation('Composition')))) {
        result << propAttr.name
    }
}

return result