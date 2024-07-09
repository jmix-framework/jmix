import axios from "axios";

const BASE_PATH = import.meta.env.VITE_BACKEND_URL + "/jmee/rest/v1/task-list";

export const loadDashboardData = (token?: string) => {
    return axios.get(`\${BASE_PATH}/dashboard`, {
        headers: {
            Authorization: `Bearer \${token}`
        }
    });
}