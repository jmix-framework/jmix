/*
 * Derives the propertyFilter components to be generated for a list view, shared by every template that has
 * a filter step.
 *
 * Bindings:
 *   filterConfiguration - value of the FILTER_COMPONENT property
 *
 * Returns a map of:
 *   type            - 'GENERIC', 'PROPERTY', 'FULL_TEXT' or 'NONE'
 *   propertyFilters - maps of 'id', 'property', 'operation' and 'operationEditable'; empty unless type is 'PROPERTY'
 */

def camelCasedOperation = { operation -> operation.toLowerCase().split('_').collect { it.capitalize() }.join('') }

// an attribute path may be nested, and a component id cannot contain a dot: 'customer.grade' -> 'customerGrade'
def camelCasedProperty = { property ->
    def segments = property.split('\\.').collect { it.replaceAll('[^A-Za-z0-9_]', '') }.findAll { it }
    segments ? segments.head() + segments.tail().collect { it.capitalize() }.join('') : 'property'
}

def type = filterConfiguration.type.name()

def propertyFilters = []
if (type == 'PROPERTY') {
    def filters = filterConfiguration.propertyFilters
    // A property may legitimately be filtered by several operations - a date range, for instance - but the id
    // is derived from the property, so the operation joins the id of a repeated property to keep component
    // ids unique within the view.
    def repeatedProperties = filters.groupBy { it.property }.findAll { it.value.size() > 1 }.keySet()
    def usedIds = new HashSet()
    filters.each { f ->
        def base = repeatedProperties.contains(f.property)
                ? camelCasedProperty(f.property) + camelCasedOperation(f.operation)
                : camelCasedProperty(f.property)
        def id = base + 'Filter'
        // the wizard rejects a repeated property/operation pair, this only guards against a name clash
        for (int i = 2; !usedIds.add(id); i++) {
            id = base + 'Filter' + i
        }
        propertyFilters << [id               : id,
                            property         : f.property,
                            operation        : f.operation,
                            operationEditable: f.operationEditable]
    }
}

return [type: type, propertyFilters: propertyFilters]
