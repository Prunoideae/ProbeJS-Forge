import json
from typing import Dict, List
from .string_formatter import Component, ComponentProvider, Concat, String


class Item(ComponentProvider):
    mod: str
    name: str
    resource_location: str

    def __init__(self, resource_location) -> None:
        self.resource_location = resource_location
        self.mod, self.name = resource_location.split(":", maxsplit=1)

    def format(self) -> Component:
        return String(f"Item.of({json.dumps(self.resource_location)})")


class Fluid(ComponentProvider):
    mod: str
    name: str
    resource_location: str

    def __init__(self, resource_location) -> None:
        self.resource_location = resource_location
        self.mod, self.name = resource_location.split(":", maxsplit=1)

    def format(self) -> Component:
        return String(f"Fluid.of({json.dumps(self.resource_location)})")


class Bracket(ComponentProvider):
    values: List[Component]

    def __init__(self, values: List[Component]) -> None:
        self.values = values

    def format(self) -> Component:
        return Concat([
            String("{"),
            Concat(self.values, sep=',\n'),
            String("}")], sep="\n")


class MapEntry(ComponentProvider):
    key: str
    value: Component

    def __init__(self, key: str, value: Component) -> None:
        self.key = key
        self.value = value

    def format(self) -> Component:
        return Concat([String(self.key), String(":"), self.value])


def compile_items(json_entry: List[str]) -> Component:
    items = [Item(j) for j in json_entry]
    mod_map = {}
    for item in items:
        if item.mod not in mod_map:
            mod_map[item.mod] = []
        mod_map[item.mod].append(item)
    item_comps = []
    for mod, comps in mod_map.items():
        mod: str
        comps: List[Item]
        item_comps.append(MapEntry(mod, Bracket(
            [MapEntry(comp.name, comp.format()).format() for comp in comps]).format()
        ).format())
    return Concat([String("const items = "), Bracket(item_comps).format(), String(";")])


def compile_fluids(json_entry: List[str]) -> Component:
    fluids = [Fluid(j) for j in json_entry]
    mod_map = {}
    for fluid in filter(lambda x: not x.name.startswith("flowing_"), fluids):
        if fluid.mod not in mod_map:
            mod_map[fluid.mod] = []
        mod_map[fluid.mod].append(fluid)
    item_comps = []
    for mod, comps in mod_map.items():
        mod: str
        comps: List[Fluid]
        item_comps.append(MapEntry(mod, Bracket(
            [MapEntry(comp.name, comp.format()).format() for comp in comps]).format()
        ).format())
    return Concat([String("const fluids = "), Bracket(item_comps).format(), String(";")])


def compile_tags(json_entry: Dict) -> Component:
    pass
