import { defineConfig, Plugin } from 'vite';

const isProd = process.env.NODE_ENV == "production";

// TODO: in dev path index.html with localhost config
// TODO: in prod, leave it untouched, so the backend can patch it

export default defineConfig({
  resolve: {
    alias: [
      {
        // to resolve scalajs import in main.js
        find: /^scalajs:(.*)$/,
        replacement: `/out/frontend/${isProd ? 'full' : 'fast'}LinkJS.dest/$1`
      }
    ]
  },
  server: {
    proxy: {
      // to avoid CORS issues, proxy the requests to the backend
      '/RpcApi/': 'http://localhost:8081',
    },
  },
});
