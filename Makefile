DUNGEON_FOLDER=cd dungeon_master;
.PHONY: build repl nrepl run

build:
	nix-shell --command "$(DUNGEON_FOLDER) clj -T:build jar"

repl:
	nix-shell --command "$(DUNGEON_FOLDER) clj -M:nREPL -m nrepl.cmdline -i -C"

nrepl:
	nix-shell --command "$(DUNGEON_FOLDER) clj -M:nREPL -m nrepl.cmdline"

run:
	nix-shell --command "$(DUNGEON_FOLDER) clj -X dungeon-master.core/-main"


# DEPRECATED
#   Keeping this around for documentation on how to use leiningen
#   but going forward i don't want to use leiningen anymore
#DUNGEON_APP=docker compose run --rm dungeon
#
#.PHONY: build repl nix-repl dungeon-shell tests run play db-backup
#
## I can't seem to connect to the repl from outside of the docker container
#repl:
#	$(DUNGEON_APP) lein repl :start :host 0.0.0.0 :port 61799
#
## Connect my editor to this via conjure
#nix-repl:
#	nix-shell --command "cd dungeon_master; lein repl :start :port 61799"
#
#dungeon-shell:
#	$(DUNGEON_APP) bash
#
#tests:
#	$(DUNGEON_APP) lein test
#
#run:
#	docker compose up
#
#play:
#	$(DUNGEON_APP) lein run
#
## This can only be run when the database is not running
#db-backup:
#	docker compose run --rm graphdb mkdir -p /var/lib/neo4j/data/backups
#	docker compose run --rm graphdb neo4j-admin database dump --to-path=./data/backups/ neo4j
