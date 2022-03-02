# ProbeJS

A data dumper and typing generator for the KubeJS functions, constants and classes.

A recent migration to pure java makes the mod can function without any extra dependencies, enjoy the scripting!

Great thanks to @DAmNRelentless, @LatvianModder and @yesterday17 for invaluable suggestions during the development!

## 1. Showcase

Auto-completion snippets for Items, Blocks, Fluids, Entities and Tags:

![image](./examples/2.gif)

Auto-completion, type-hinting for most of the functions and classes:

![image](./examples/3.gif)

## 2. Installation

1. Get VSCode.
2. Install the mod.
3. In game, use `/probejs dump` and wait for the typings to be generated.
4. Open the `./minecraft/kubejs` in VSCode, you should see snippets and typing functioning.
5. Use `/probejs dump` in case of you want to refresh the generated typing. If VSCode is not responding to file changes,
   press F1 and execute `TypeScript: Restart TS server` to force a refresh in Code.

## 3. Event Dump

1. Replace all `onEvent` in your scripts to `captureEvent`, this is a event capturer implemented in ProbeJS functioning
   identical to the original, but with a little bit extra function - it will report to ProbeJS when the event is fired,
   so we can get into data of events.
2. Run the game, and use the `/probejs dump` commmand **only** after the events of interest are fired, then dump and
   regenerate typings as before.
3. Reload your IDE if your IDE doesn't know about the changes of typings, you will see the `onEvent` and `captureEvent`
   with correct typings now.
4. If you want to remove the mod, don't forget to replace all `captureEvent` back to `onEvent`.
