# https://docs.earthly.dev/basics

VERSION 0.8

devbox:
  FROM jetpackio/devbox:latest

  # Installing your devbox project
  WORKDIR /code
  USER root:root
  RUN mkdir -p /code && chown ${DEVBOX_USER}:${DEVBOX_USER} /code
  USER ${DEVBOX_USER}:${DEVBOX_USER}
  COPY --chown=${DEVBOX_USER}:${DEVBOX_USER} devbox.json devbox.json
  COPY --chown=${DEVBOX_USER}:${DEVBOX_USER} devbox.lock devbox.lock
  RUN devbox run -- echo "Installed Packages."

build:
  FROM +devbox
  WORKDIR /code
  COPY --dir build.sc backend frontend rpc schema.sql schema.scala.ssp ./
  RUN devbox run -- mill backend.assembly && mv out/backend/assembly.dest/out.jar out.jar
  COPY package.json bun.lockb ./
  RUN devbox run -- bun install
  RUN devbox run -- mill frontend.fullLinkJS
  COPY main.js index.html vite.config.ts style.css ./
  RUN devbox run -- bunx  vite build
  SAVE ARTIFACT out.jar
  SAVE ARTIFACT dist


docker-image:
  FROM ghcr.io/graalvm/jdk-community:22
  COPY +build/out.jar ./
  COPY --dir +build/dist ./
  ENV FRONTEND_DISTRIBUTION_PATH=dist
  CMD ["java", "-jar", "out.jar"]
  SAVE IMAGE app:latest


app-deploy:
  # run locally:
  # FLY_API_TOKEN=$(flyctl tokens create deploy) earthly --allow-privileged --secret FLY_API_TOKEN -i +app-deploy --COMMIT_SHA=<xxxxxx>
  ARG --required COMMIT_SHA
  ARG IMAGE="registry.fly.io/dropica:deployment-$COMMIT_SHA"
  FROM earthly/dind:alpine-3.19-docker-25.0.5-r0
  RUN apk add curl
  RUN set -eo pipefail; curl -L https://fly.io/install.sh | sh
  COPY fly.toml ./
  WITH DOCKER --load $IMAGE=+docker-image
    RUN --secret FLY_API_TOKEN \
        docker image ls \
     && /root/.fly/bin/flyctl auth docker \
     && docker push $IMAGE \
     && /root/.fly/bin/flyctl deploy --image $IMAGE --build-arg COMMIT_SHA=$COMMIT_SHA
  END
