import svelte from 'rollup-plugin-svelte';
import resolve from '@rollup/plugin-node-resolve';
import pkg from './package.json';

const name = pkg.name
    .replace(/^(@\S+\/)?(svelte-)?(\S+)/, '$3')
    .replace(/^\w/, m => m.toUpperCase())
    .replace(/-\w/g, m => m[1].toUpperCase());

export default [{
    input: 'src/main.js',
    output: [
        {file: process.env.DEV ? "server.mjs" : "src/main/resources/plugin-ui/server.mjs", 'format': 'es'},
        // { file: pkg.main, 'format': 'umd', name }
    ],
    plugins: [
        svelte({
            compilerOptions: {
                generate: "ssr",
                hydratable: true
            },
            emitCss: false
        }),
        resolve()
    ]
}, {
    input: 'src/main.js',
    output: [
        {file: process.env.DEV ? "client.mjs" : "src/main/resources/plugin-ui/client.mjs", 'format': 'es', name},
        // { file: pkg.main, 'format': 'umd', name }
    ],
    plugins: [
        svelte({
            compilerOptions: {
                generate: "dom",
                hydratable: true
            },
            emitCss: false
        }),
        resolve()
    ]
}];
