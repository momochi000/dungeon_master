FROM docker.io/clojure:temurin-19-lein-focal

WORKDIR /app
COPY ./dungeon_master/project.clj .
RUN lein deps

EXPOSE 61799

COPY ./dungeon_master .
