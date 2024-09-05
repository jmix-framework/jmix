class PivotTableProperty {
    constructor(property, localizedProperty) {
        this.property = property;
        this.localizedProperty = property;
    }

    toString() {
        return this.localizedProperty;
    }
}