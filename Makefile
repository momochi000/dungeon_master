DUNGEON_APP=docker-compose run --rm dungeon

.PHONY: build repl nix-repl dungeon-shell tests run play db-backup

# I can't seem to connect to the repl from outside of the docker container
repl:
	$(DUNGEON_APP) lein repl :start :host 0.0.0.0 :port 61799

nix-repl:
	nix-shell --command "cd dungeon_master; lein repl :start :port 61799"

dungeon-shell:
	$(DUNGEON_APP) bash

tests:
	$(DUNGEON_APP) lein test

run:
	docker-compose up

play:
	$(DUNGEON_APP) lein run

# This can only be run when the database is not running
db-backup:
	docker-compose run --rm graphdb mkdir -p /var/lib/neo4j/data/backups
	docker-compose run --rm graphdb neo4j-admin database dump --to-path=./data/backups/ neo4j
