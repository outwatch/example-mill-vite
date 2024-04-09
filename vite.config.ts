import { defineConfig, Plugin } from 'vite';

const isProd = process.env.NODE_ENV == "production";

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
      '/RpcApi/': 'http://localhost:8080',
    }
  },
});
