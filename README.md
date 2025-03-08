# Dungeon Master

## Description
This is an experimental project to build an AI powered dungeon master for paper and pencil role playing.


## Development setup
The project is heavily under development and at the moment the only thing you can do is set up your development environment, repl, and call some functions.

I'm using clojure's tools.build to build the app, transitioning away from leiningen that I used before. As such, the below instructions are in the process of being deprecated.

### clojure nix environment
I'm leveraging nix to have access to clojure so that it doesn't need to be installed on the host machine. There's a provided shell.nix file that has what you need.

### environment setup
create a .env file with the necessary environment variables needed for configuration

### build/compilation
`clj -T:build jar`
leveraging the `build.clj` file

### nrepl
`clj -M:nREPL -m nrepl.cmdline`

### neo4j database
This is run from a separate docker container. To make this easier, there is a provided docker-compose file. Simply run `docker-compose up`. It will expose the necessary ports


### Old

To do this:

    docker-compose up --build

this will also start the neo4j database, which includes a web interface you can reach at `localhost:7474`. By default there is no authentication.

Unfortunately, i wasn't able to connect to the repl from outside of the docker container, so currently I'm using nix. I start this with `make nix-repl`. This starts a repl at a fixed port, currently 61799. There is an included .nrepl-port file that instructs conjure(vim) to use this port to connect to the clojure repl.

You'll need to provide an OPENAI_API_KEY in the docker environment. Either create a .env file with
`OPENAI_API_KEY="<your key here>"`
or your method of choice.

## Running

Run the command line interface with `make play`, but note this expects the database to be running. This simply executes `lein run` inside the container.
