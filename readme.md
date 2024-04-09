# Outwatch Frontend Example

A [github-template](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-template-repository) for [outwatch](https://github.com/outwatch/outwatch).

Technologies used:
- [Outwatch](https://github.com/outwatch/outwatch/) functional web-frontend library
- [Scala 3](https://www.scala-lang.org/) programming language, compiled to javascript using [ScalaJS](https://www.scala-js.org/)
- [Mill](https://mill-build.com) build tool
- [Vite](https://vitejs.dev) hot reloading and bundling
- [devbox](https://www.jetpack.io/devbox) for a reproducible dev environment
- [direnv](https://direnv.net/) to automatically load dev environment when entering project directory


## Getting Started

1. Setup on your system:
   - [devbox](https://www.jetpack.io/devbox)
   - [direnv](https://direnv.net/)

   If you don't want to spend the time to setup those, skip to the manual setup section.
1. Clone the example
    ```shell
    # if you want to just get the template locally without creating a github repo:
    git clone --depth 1 https://github.com/outwatch/example-mill-vite my-first-outwatch-project

    # OR: create new repo on github based on this template (using github-cli)
    gh repo create my-first-outwatch-project --template outwatch/example-mill-vite --public --clone


    cd my-first-outwatch-project
    ```
1. Allow direnv to enter the dev environment when entering the project directory
    ```shell
    direnv allow
    ```
    Which will load [.envrc](.envrc) and install the packages from [devbox.json](devbox.json).
1. Start the dev server
    ```shell
    devbox services up
    ```
   The services are defined in [process-compose.yml](process-compose.yml).
1. Point your browser to <http://localhost:5173>
1. Edit [FrontendMain.scala](frontend/src/main/scala/frontend/FrontendMain.scala) to see hot reloading.
1. Production build:
   ```shell
   # compile frontend 
   mill frontend.fullLinkJS

   # bundle frontend to /dist
   npx vite build

   # run backend and serve /dist
   mill backend.run
   ```
   Point your browser to <http://localhost:8080>


## Manual Setup without devbox or direnv

1. Install:
    - [Mill](https://mill-build.com)
    - [NodeJS](https://nodejs.org) (provides `npm`)
1. Run:
    ```shell
    npm install

    # for automatically recompiling Scala sources to Javascript
    mill --watch frontend.fastLinkJS

    # in another terminal
    # to start the devserver with hot reloading
    npx vite dev
    ```
1. Point your browser to <http://localhost:5173>
1. Edit [FrontendMain.scala](frontend/src/main/scala/frontend/FrontendMain.scala) to see hot reloading.
