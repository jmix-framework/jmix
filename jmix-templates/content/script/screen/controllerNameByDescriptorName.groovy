if (!descriptorName?.trim()) {
    return descriptorName
}
StringBuilder controllerName = new StringBuilder()
String[] parts = descriptorName.toLowerCase().split('[_-]')
for (part in parts) {
    if (part.length() == 0) continue
    def first = part.substring(0, 1).toUpperCase()
    controllerName.append(first)
    if (part.length() > 1)
        controllerName.append(part.substring(1))
}
return controllerName.toString()

