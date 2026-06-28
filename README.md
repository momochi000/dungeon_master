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

### ladybug explorer
Ladybug is now used as an embedded graph database. The provided docker-compose file starts the Ladybug Explorer web UI and mounts the local database directory so you can inspect the graph in the browser. Run `docker compose up` and open `http://localhost:8000`.

### Running
To run the main function (which includes the main game loop), first run
`docker-compose up`
which starts the Ladybug Explorer UI over the local embedded database file

then run
`clj -X dungeon-master.core/-main`
from inside the nix-shell
There is also a make command for convenience

To seed the bundled test world state from a REPL:

```clojure
(require '[dungeon-master.fixtures.test-world-state :as fixture])
(fixture/insert-test-world-state)
```

### Old

To do this:

    docker-compose up --build

this will also start Ladybug Explorer, which you can reach at `localhost:8000`.

Unfortunately, i wasn't able to connect to the repl from outside of the docker container, so currently I'm using nix. I start this with `make nix-repl`. This starts a repl at a fixed port, currently 61799. There is an included .nrepl-port file that instructs conjure(vim) to use this port to connect to the clojure repl.

You'll need to provide an OPENAI_API_KEY in the docker environment. Either create a .env file with
`OPENAI_API_KEY="<your key here>"`
or your method of choice.

## Running

Run the command line interface with `make play`, but note this expects the local Ladybug database path to be configured. When using docker compose, the explorer mounts `./ladybug/data` and opens `dungeon-master.lbug` from there.
