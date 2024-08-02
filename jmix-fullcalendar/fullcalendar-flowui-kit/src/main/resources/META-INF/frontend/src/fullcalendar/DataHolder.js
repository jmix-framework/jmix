class DataHolder {

    contexts = new Map();

    constructor() {
    }

    get(object) {
        return this.contexts.get(object);
    }

    set(object, context) {
        this.contexts.set(object, context);
    }

    delete(object) {
        this.contexts.delete(object)
    }
}

const dataHolder = new DataHolder();

export default dataHolder;