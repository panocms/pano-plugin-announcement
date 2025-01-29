import svelte from 'rollup-plugin-svelte';
import resolve from '@rollup/plugin-node-resolve';
import del from 'rollup-plugin-delete';

const baseConfig = {
    input: 'src/main.js',
    output: {
        format: 'es',
        chunkFileNames: '[name]-[hash].js', // Chunk file naming
    },
    plugins: [
        del({
            targets: process.env.DEV ? 'dist/*' : 'src/main/resources/plugin-ui/*', // Targets to clean
            runOnce: true // Run only once
        }),
        resolve(),
    ],
    preserveEntrySignatures: 'strict'
};

export default [
    // Server configuration
    {
        ...baseConfig,
        output: {
            ...baseConfig.output,
            dir: process.env.DEV ? "dist/server" : "src/main/resources/plugin-ui/server", // Server directory
            entryFileNames: 'server.mjs' // Server entry file
        },
        plugins: [
            ...baseConfig.plugins,
            svelte({
                compilerOptions: {
                    generate: "ssr",
                    hydratable: true
                },
                emitCss: false
            })
        ]
    },
    // Client configuration
    {
        ...baseConfig,
        output: {
            ...baseConfig.output,
            dir: process.env.DEV ? "dist/client" : "src/main/resources/plugin-ui/client", // Client directory
            entryFileNames: 'client.mjs' // Client entry file
        },
        plugins: [
            ...baseConfig.plugins,
            svelte({
                compilerOptions: {
                    generate: "dom",
                    hydratable: true
                },
                emitCss: false
            })
        ]
    }
];
