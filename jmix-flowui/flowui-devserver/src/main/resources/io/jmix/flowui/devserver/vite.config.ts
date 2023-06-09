// NOTE: do not change the file, it will be overwritten by the Studio

import { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated';

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
