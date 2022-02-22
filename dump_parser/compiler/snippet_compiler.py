import json
from typing import Dict, List
from collections import defaultdict


def compile_simple_entry(json_entry: List[str], type: str):
    by_mod = defaultdict(list)
    for item in json_entry:
        mod, name = item.split(":")
        by_mod[mod].append(name)

    mods_items = {}
    for mod, names in by_mod.items():
        mod_items = {
            "prefix": [
                f"@{mod}.{type}"
            ],
            "body": f"\"{mod}:${{1|{','.join(names)}|}}\""
        }
        mods_items[f"{mod}_{type}"] = mod_items
    return mods_items


def compile_tag_entry(json_entry: Dict[str, List[str]], type: str):
    by_mod = defaultdict(list)
    for item in json_entry.keys():
        mod, name = item.split(":")
        by_mod[mod].append(name)

    mods_items = {}
    for mod, names in by_mod.items():
        mod_items = {
            "prefix": [
                f"@{mod}.tags.{type}"
            ],
            "body": f"\"#{mod}:${{1|{','.join(names)}|}}\""
        }
        mods_items[f"{mod}_tag_{type}"] = mod_items
    return mods_items
