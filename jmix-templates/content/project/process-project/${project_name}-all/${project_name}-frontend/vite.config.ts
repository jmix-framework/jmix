import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
const TASK_LIST_BASE_URL = "http://localhost:8181";

export default defineConfig({
    plugins: [react()],
    server: {
        proxy: {
            "/jmee/rest/v1/task-list": {
                target: `\${TASK_LIST_BASE_URL}`,
            }
        }
    }
})
