{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = [
    #pkgs.jdk21
    pkgs.jdk
    pkgs.clojure
    pkgs.leiningen
    #pkgs.boot
  ];
}
