result = []
for (def entity : items) {
    if (!entity.isEmbeddable()
            && (!templateDirName == 'single-screen' || !entity.isDeepInheritor('com.haulmont.cuba.core.entity.AbstractNotPersistentEntity'))) {
        result << entity
    }
}

return result