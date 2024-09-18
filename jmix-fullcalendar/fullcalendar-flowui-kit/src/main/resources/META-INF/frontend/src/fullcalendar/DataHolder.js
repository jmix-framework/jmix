/**
 * Class is used as global storage for event sources and their data. Since FullCalendar does not enable to assign
 * and ID to an event source, and there is no ability to recognize for which source component should load data from
 * server, this class maps concrete source ID and its data.
 */
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

export default DataHolder;