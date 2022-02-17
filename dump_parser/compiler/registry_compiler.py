import json
from os import sep
from typing import Dict, List
from .string_formatter import Component, ComponentProvider, Concat, String


class StringEntry(ComponentProvider):
    mod: str
    name: str
    resource_location: str

    def __init__(self, resource_location) -> None:
        self.resource_location = resource_location
        self.mod, self.name = resource_location.split(":", maxsplit=1)

    def format(self) -> Component:
        return String(json.dumps(self.resource_location))


class Bracket(ComponentProvider):
    values: List[Component]

    def __init__(self, values: List[Component]) -> None:
        self.values = values

    def format(self) -> Component:
        return Concat([
            String("{"),
            Concat(self.values, sep=',\n'),
            String("}")], sep="\n")


class Array(ComponentProvider):
    values: List[Component]

    def __init__(self, values: List[Component]) -> None:
        self.values = values

    def format(self) -> Component:
        return Concat([
            String("["),
            Concat(self.values, sep=", "),
            String("]")
        ])


class MapEntry(ComponentProvider):
    key: str
    value: Component

    def __init__(self, key: str, value: Component) -> None:
        self.key = key
        self.value = value

    def format(self) -> Component:
        return Concat([String(self.key), String(":"), self.value])


def compile_items(json_entry: List[str]) -> Component:
    items = [StringEntry(j) for j in json_entry]
    mod_map = {}
    for item in items:
        if item.mod not in mod_map:
            mod_map[item.mod] = []
        mod_map[item.mod].append(item)
    item_comps = []
    for mod, comps in mod_map.items():
        mod: str
        comps: List[StringEntry]
        item_comps.append(MapEntry(mod, Bracket(
            [MapEntry(comp.name, comp.format()).format() for comp in comps]).format()
        ).format())
    return Concat([String("const items = "), Bracket(item_comps).format(), String(";")])


def compile_fluids(json_entry: List[str]) -> Component:
    fluids = [StringEntry(j) for j in json_entry]
    mod_map = {}
    for fluid in filter(lambda x: not x.name.startswith("flowing_"), fluids):
        if fluid.mod not in mod_map:
            mod_map[fluid.mod] = []
        mod_map[fluid.mod].append(fluid)
    item_comps = []
    for mod, comps in mod_map.items():
        mod: str
        comps: List[StringEntry]
        item_comps.append(MapEntry(mod, Bracket(
            [MapEntry(comp.name, comp.format()).format() for comp in comps]).format()
        ).format())
    return Concat([String("const fluids = "), Bracket(item_comps).format(), String(";")])


def compile_tag(json_entry: Dict) -> Component:

    class TagEntry(ComponentProvider):
        name: str
        items: List[str]

        def __init__(self, name: str, items: List[str]) -> None:
            self.name = name
            self.items = items

        def format(self) -> Component:
            return Bracket([
                MapEntry("tag", String(json.dumps(self.name))).format(),
                MapEntry("members", Array([String(json.dumps(x)) for x in self.items]).format()).format(),
            ]).format()

    tags = [TagEntry(k, v) for k, v in json_entry.items()]
    by_mod: Dict[str, List[TagEntry]] = {}
    for tag in tags:
        mod = tag.name.split(':', maxsplit=1)[0]
        if mod not in by_mod:
            by_mod[mod] = []
        by_mod[mod].append(tag)

    mod_entries: List[Component] = []
    for k, v in by_mod.items():
        def process_key(k: str) -> str: return k.replace("/", "_").split(":", maxsplit=1)[1]
        entries = [MapEntry(process_key(e.name), e.format()).format() for e in v]
        mod_entries.append(MapEntry(k, Bracket(entries).format()).format())
    return Bracket(mod_entries).format()


def compile_tags(json_entry: Dict[str, Dict]) -> Component:
    tags_formatted = []
    for k, v in json_entry.items():
        tags_formatted.append(MapEntry(k, compile_tag(v)).format())
    return Concat([String("const tags ="), Bracket(tags_formatted).format(), String(";")])
