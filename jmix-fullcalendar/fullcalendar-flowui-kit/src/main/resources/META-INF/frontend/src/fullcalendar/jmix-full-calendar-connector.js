import dataHolder from './DataHolder.js'

(function () {
    function removeSources(sourceIds) {
        for (const sourceId of sourceIds) {
            dataHolder.delete(sourceId);
        }
    }

    window.Vaadin.Flow.jmixFullCalendarConnector = {
        removeSources
    };
})();