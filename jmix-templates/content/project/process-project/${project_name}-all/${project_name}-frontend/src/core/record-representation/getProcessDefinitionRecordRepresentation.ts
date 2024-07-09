import {ProcessDefinition} from "../types.ts";

export const getProcessDefinitionRecordRepresentation = (processDefinition?: ProcessDefinition) => {
    if (!processDefinition) {
        return undefined;
    }

    return processDefinition.name || processDefinition.key;
}