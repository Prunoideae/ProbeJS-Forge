# The Future of ProbeJS

Unfortunately, the current codebase of ProbeJS is not suitable for any further extension and improvement in my opinion, thus, a complete rewrite is needed for continuing the development of ProbeJS - and for continuing the development for maybe even more mods to improve coding experience of Minecraft Modpacks.

Any port to other Minecraft version should not be accepted during this time, as maintaining different branches will drastically slow down the development. However, if the major goals of the rewrite are completed, further ports will be much faster and easier.

The major goals of the ProbeJS rewrite should be listed as follows, but goals may change as development progresses:

1. Rework the pseudo-TypeScript document parser.
2. Rework the entire type analyzer of Probe.
3. Rework the stack-based state machine, and add more utilities for complex document handling.
4. Split several modules of Probe out for reusing and extending.
5. Rework the comment system, move annotations into comments.
6. Use own-defined special comments, instead of overriding the original comments.
7. Add more special comment handler for 4 and 5, like `@modify` or `@remove`
8. Decouple the modules, make Probe into 3 almost-independent parts - the `Document`, the `Compiler` and the `Adapter`.

Some detailed goals of each major goal are as followed.

## Document Parser

1. Decouple the logic by maintaining a global static document component handler registry. Possibly be a `Map<Class<? extends AbstractComponent>, Class<? extends AbstractComponent>>`.
2. Clarify the components by their behavior, mainly separate them into `IDecorativeEntity` and `IDocumentEntity`. Where `IDocumentEntity` will implement `IDocumentEntity::accept(decoratives: List<IDecorativeEntity>)`.
3. (May need further consideration) Separate class additions by file names, or add a Namespace declaration for this.

## Stack Machine

1. Add more Util functions.

## Type Analyzer

1. Rework most `Info` classes, especially the `TypeInfo` class, this should be reworked completely.
2. Merge the function of `Formatter` classes into `Info` classes, simplify the model.
3. The `Info::format` should accept a corresponding `Document` as argument.
4. Introduce the abstract class `Documented<? extends Document>`.

## Comment Handler

### 1. Specification of `@modify`

1. Formatted in `@modify {type} <paramName>`.
2. Modifies an argument with same `paramName`.
3. If `paramName` is not specified, modify the returned value of method instead.
4. If `type` is `null`, removes the parameter.
5. If `paramName` is not in parameters, add a new param with the same `type` and `paramName` at the end of list.
6. Applicable for: Method

### 2. Specification of `@remove`

1. Formatted in `@remove <paramName>`.
2. Removes an argument with same `paramName`.
3. Applicable for: Method

### 3. Specification of `@hide`

1. Formatted in `@hidden`
2. Hides an property commented by this.
3. Applicable for: Class, Method, Field

### 4. Specification of `@target`

1. Formatted in `@target <className>`
2. Change the class addition to class modification.
3. `className` must be a valid full name of a class, or the modification will fail and be skipped.
4. Applicable for: Class
