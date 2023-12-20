# This isn't working for some reason
#repl:
#	docker-compose run --rm dungeon lein repl :start :host 0.0.0.0 :port 61799

nix-repl:
	nix-shell --command "cd dungeon_master; lein repl :start :port 61799"
