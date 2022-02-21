import argparse
import json
import os
from os import chdir, mkdir, path
import shutil
from typing import Dict, List, Tuple

from compiler.class_compiler import Controller, Class, Field, Method
from compiler.constant_compiler import Constant
from compiler.registry_compiler import compile_blocks, compile_fluids, compile_items, compile_tags
from compiler.string_formatter import Component
import special_compiler


class DeclaredNamespace(Component):
    c: List[Class]

    def __init__(self, c: List[Class]) -> None:
        self.c = c

    def format(self, ident, pad, step) -> str:
        stepped = ident + step
        result = [ident * pad + f"declare namespace {Controller.transform_classpath(self.c[0].path)} {{"]
        for clazz in self.c:
            result.append(clazz.format().format(stepped, pad, step))
        result.append(ident * pad + "}")
        return "\n".join(result)


def main():
    basepath = path.abspath(".")
    parser = argparse.ArgumentParser(
        "declaration_compiler",
        "Compile the probejs dump file to the TypeScript declaration package.")
    parser.add_argument("--probe", metavar="FILE", type=str, help="the ProbeJS dump file", dest="probe")
    parser.add_argument("--kube", metavar="FILE", type=str, help="the KubeJS dump file", dest="kube")

    dump_probe = json.load(open(parser.parse_args().probe))
    dump_kube = json.load(open(parser.parse_args().kube))
    Controller.classes_remapper = {x['classname']: x['name'] for x in dump_probe['classes']}
    Controller.typing_transformer, Controller.value_transformer, Controller.keyword = json.load(open("./type_transformer.json"))

    ### The KubeJS Registry Script Generation ###

    for folder in ['./server_scripts', './startup_scripts', './client_scripts']:
        os.chdir(folder)
        item_component = compile_items(dump_kube['registries']['items'])
        block_component = compile_blocks(dump_kube['registries']['blocks'])
        fluid_component = compile_fluids(dump_kube['registries']['fluids'])
        tags_component = compile_tags(dump_kube["tags"])
        with open('./dumps.js', 'w') as d_file:
            print("// priority: 1000", file=d_file)
            print(item_component.format(0, '', 0), file=d_file)
            print(fluid_component.format(0, '', 0), file=d_file)
            print(block_component.format(0, '', 0), file=d_file)
            print(tags_component.format(0, '', 0), file=d_file)
        os.chdir("../")

    ### The ProbeJS Typing Script Generation ###
    os.chdir(basepath)

    # Create project directory
    if path.exists("./kubetypings"):
        shutil.rmtree("./kubetypings")
    os.mkdir("./kubetypings")
    os.chdir("./kubetypings")
    os.system("npm init -y")
    os.system("tsc --init")

    # Configure the project
    package_json: Dict = json.load(open("./package.json"))
    package_json["description"] = "The compiled typing files for KubeJS"
    package_json["types"] = "./index.d.ts"
    package_json["author"] = "ProbeJS"
    package_json["licence"] = "MIT"
    package_json.pop("main")
    json.dump(package_json, open("./package.json", 'w'))

    # Generate framework and stubs
    with open("./index.d.ts", 'w') as idx_file:
        print("/// <reference path=\"./src/index.d.ts\" />", file=idx_file)
        print("// The auto-generated index file", file=idx_file)
    mkdir("./src")
    chdir("./src")

    with open("./index.d.ts", 'w') as idx_file:
        print("/// <reference path=\"./globals.d.ts\" />", file=idx_file)
        print("/// <reference path=\"./constants.d.ts\" />", file=idx_file)
        print("/// <reference path=\"./java.d.ts\" />", file=idx_file)
        print("/// <reference path=\"./events.d.ts\" />", file=idx_file)

        print("// The auto-generated index file", file=idx_file)

    # Generate global classes definition file

    with open("./globals.d.ts", 'w') as cls_file:
        remapped_classes = [Class(entry) for entry in dump_probe['globalClasses']
                            if entry['name'] in Controller.classes_remapper]
        for c in remapped_classes:
            c.name = Controller.classes_remapper[c.orig_name]
            print("declare " + c.format().format(0, ' ', 4), file=cls_file)

        global_classes = [Class(entry) for entry in dump_probe['globalClasses']
                          if all([entry['name'] not in Controller.classes_remapper,
                                  entry['name'] not in Controller.typing_transformer,
                                  not entry['name'].startswith("[")])]
        recipe_maps: Dict[str, List[Tuple[str, str]]] = {}
        for recipe_class in dump_probe['recipes']:
            mod, name = recipe_class['resourceLocation'].split(":")
            r_class = recipe_class['recipeClass']
            if mod not in recipe_maps:
                recipe_maps[mod] = []
            recipe_maps[mod].append((name, r_class))

        recipes = {}
        for mod, recipe_methods in recipe_maps.items():
            dummy_methods = [special_compiler.DummyRecipeMethod(name, recipe_class) for name, recipe_class in recipe_methods]
            dummy_class = special_compiler.DummyRecipeClass(mod, dummy_methods)
            global_classes.append(dummy_class)
            recipes[mod] = dummy_class

        dummy_recipes_class = special_compiler.DummyRecipesClass(recipes)
        global_classes.append(dummy_recipes_class)

        for clazz in global_classes:
            if clazz.orig_name == "dev.latvian.mods.kubejs.recipe.RecipeEventJS":
                clazz.fields.append(Field({
                    "name": "recipes",
                    "static": False,
                    "type": {
                        "classname": dummy_recipes_class.orig_name,
                        "parameterized": []
                    },
                    "value": None
                }))

        class_dict: Dict[str, List[Class]] = {}
        for clazz in global_classes:
            if clazz.path not in class_dict:
                class_dict[clazz.path] = []
            class_dict[clazz.path].append(clazz)
        for v in class_dict.values():
            namespace = DeclaredNamespace(v)
            print(namespace.format(0, " ", 4), file=cls_file)

    with open("./constants.d.ts", 'w') as cns_file:
        print("/// <reference path=\"./globals.d.ts\" />", file=cns_file)
        for c in map(Constant, dump_probe['constants']):
            print(c.format().format(0, '', 0), file=cns_file)

    with open('./java.d.ts', 'w') as j_file:
        print("/// <reference path=\"./globals.d.ts\" />", file=j_file)
        for c in filter(lambda x: x['allowed'], dump_probe['globalClasses']):
            print(f"declare function java(name: {json.dumps(c['name'])}): {Controller.transform_classpath(c['name'])};", file=j_file)

    with open("./events.d.ts", 'w') as eve_file:
        print("/// <reference path=\"./globals.d.ts\" />", file=eve_file)
        for name, clazz in map(lambda x: (x['name'], x['classname']), dump_probe['events']):
            clazz = Controller.transform_classpath(clazz)
            print(f"declare function onEvent(name: {json.dumps(name)}, handler: (event: {clazz}) => void): void;", file=eve_file)
            print(f"declare function captureEvent(name: {json.dumps(name)}, handler: (event: {clazz}) => void): void;", file=eve_file)


if __name__ == "__main__":
    main()
