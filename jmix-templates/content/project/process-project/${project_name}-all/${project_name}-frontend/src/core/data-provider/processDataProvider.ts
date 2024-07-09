import axios from "axios";
import {ProcessFilter} from "../types.ts";

const BASE_PATH = import.meta.env.VITE_BACKEND_URL + "/jmee/rest/v1/task-list";

export const loadProcessDefinitions = async (token?: string, filter?: ProcessFilter) => {
    if (!filter) {
        return axios.get(`\${BASE_PATH}/processes`, {
            headers: {
                Authorization: `Bearer \${token}`
            }
        });
    }
    return axios.post(`\${BASE_PATH}/processes/search`, filter, {
        headers: {
            Authorization: `Bearer \${token}`
        }
    });
}

export const startProcess = async (processDefinitionId: string, token?: string) => {
    return axios.post(`\${BASE_PATH}/processes/\${processDefinitionId}/start`, {}, {
        headers: {
            Authorization: `Bearer \${token}`
        }
    });
}