import {dataHolder} from './DataHolder.js';

(function () {
    /**
     * Removes sources and their data from global storage.
     * <p>
     * Must be invoked after the component is detached from the UI or when the event source is removed from
     * the component.
     * @param sourceIds a list of event source IDs
     */
    function removeSources(sourceIds) {
        for (const sourceId of sourceIds) {
            dataHolder.delete(sourceId);
        }
    }

    window.Vaadin.Flow.jmixFullCalendarConnector = {
        removeSources
    };
})();