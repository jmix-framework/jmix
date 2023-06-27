import { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated';

// WARN: do not change this row, it will be overwritten by the Studio
let hmrPort = 60001;

const customConfig: UserConfigFn = (env) => ({
    server: {
        hmr: {
            protocol: 'ws',
            host: 'localhost',
            port: hmrPort
        }
    }
});

export default overrideVaadinConfig(customConfig);
