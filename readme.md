# Outwatch Frontend Example

A [github-template][github-template] for [outwatch](https://github.com/outwatch/outwatch).

Technologies used:
- [Outwatch](https://github.com/outwatch/outwatch/) functional Web-Frontend Library
- [Scala 3](https://www.scala-lang.org/) programming language, compiled to javascript using [ScalaJS](https://www.scala-js.org/)
- [Mill](https://mill-build.com/mill/Intro_to_Mill.html) build tool
- [Vite](https://vitejs.dev) fast frontend Hot reloading
- [devbox](https://www.jetpack.io/devbox) for a reproducible dev environment
- [direnv](https://direnv.net/) to automatically load dev environment when entering project directory

# Getting Started

1. Setup [devbox](https://www.jetpack.io/devbox) and [direnv](https://direnv.net/).
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
    Which will load [.envrc](.envrc).
1. Start the dev server
    ```shell
    devbox services up
    ```
   The services are defined in [process-compose.yml](process-compose.yml).
1. Point your browser to <http://localhost:5173>
1. Edit [Main.scala](frontend/src/main/scala/frontend/Main.scala) to see live reloading
