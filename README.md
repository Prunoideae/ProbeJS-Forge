# ProbeJS

A data dumper for the KubeJS functions, constants and classes.

Currently highly WIP.

The event catcher is still on its way because it's a bit too tricky to get it done.

# Installation

1. Get `npm` and `python3`.
2. Compile the project and place the mod into your mod directory.
3. Open the game, use `/probejs dump server` and `/kubejs export` commands in game to generate dump data (Both are needed now).
4. Paste the files inside `dump_parser` into your `.minecraft/kubejs` directory, make sure that the `declaration_compiler.py` is at the kubejs folder, and no relative topology changes.
5. With the `kubejs` folder as current working directory, run `python ./declaration_compiler.py --kube ./exported/kubejs-server-export.json --probe ./exported/probejs-server-export.json` to generate typing files.
6. Leave the folder unchanged, run `npm install ./kubetypings` to install typing files into your KubeJS directory. Maybe also `npm init -y` at the KubeJS folder to setup the package.
7. If the game content is changed, redo the step 5 to update the typing and dumps, typing files are now linked to current KubeJS directory so no need for repetitve installation.
