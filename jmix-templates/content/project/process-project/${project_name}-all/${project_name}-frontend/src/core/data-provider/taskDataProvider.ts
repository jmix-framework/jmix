import axios from "axios";
import {PageData, TaskFilterData, TaskTableSortData} from "../types.ts";

const BASE_PATH = import.meta.env.VITE_BACKEND_URL + "/jmee/rest/v1/task-list";

const defaultPageData: PageData = {
    page: 1,
    size: 10
}

const defaultSort: TaskTableSortData = {
    order: "desc",
    property: "createDate"
};

export const loadTasks = (token?: string, pageData: PageData = defaultPageData,
                          sort: TaskTableSortData = defaultSort, filter?: TaskFilterData) => {

    const {property, order} = sort;
    const {page, size} = pageData;
    if (!filter) {
        return axios.get(`\${BASE_PATH}/tasks?page=\${page - 1}&size=\${size}&sort=\${property}&order=\${order}`, {
            headers: {
                Authorization: `Bearer \${token}`
            }
        });
    }
    return axios.post(`\${BASE_PATH}/tasks/search`, {
            page: page - 1,
            size,
            filter,
            order,
            sort: property
        },
        {
            headers: {
                Authorization: `Bearer \${token}`
            }
        });
}

export const loadTask = (taskId: string, token?: string) => {
    return axios.get(`\${BASE_PATH}/tasks/\${taskId}`, {
        headers: {
            Authorization: `Bearer \${token}`
        }
    });
}

export const loadTaskFormData = (taskId: string, token?: string) => {
    return axios.get(`\${BASE_PATH}/tasks/\${taskId}/form-data`, {
        headers: {
            Authorization: `Bearer \${token}`
        }
    });
}

export const submitTaskForm = (taskId: string, variables: Record<string, unknown>, token?: string) => {
    return axios.post(`\${BASE_PATH}/tasks/\${taskId}/submit-form`, variables, {
        headers: {
            Authorization: `Bearer \${token}`
        }
    });
}