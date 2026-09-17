{
  description = "A flake with developments tools for IQ Edition";

  inputs.nixpkgs.url = "https://channels.nixos.org/nixos-unstable/nixexprs.tar.zst";

  outputs = { self, nixpkgs }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs { inherit system; };
    in {
      devShells.x86_64-linux.default = pkgs.mkShell {
        buildInputs = with pkgs; [
          jdk8
        ];
        shellHook = ''
          ./gradlew setupDecompWorkspace
          echo "Welcome to the IQ Edition development shell!"
          echo "To run the mod, use ./gradlew runClient."
        '';
      };
    };
}
