import json
from typing import Any, Dict, Union
from .string_formatter import Component, ComponentProvider, String
from .class_compiler import Class, Controller


class Constant(ComponentProvider):
    name: str
    classname: str
    value: Union[None, Any]

    def __init__(self, json_entry: Dict) -> None:
        self.name = json_entry['name']
        self.classname = json_entry['classname']
        self.value = json_entry.get('value')

    def format(self) -> Component:
        if self.classname in Controller.value_transformer:
            return String(f"declare const {self.name}: {json.dumps(self.value)};")
        else:
            return String(f"declare const {self.name}: {Controller.transform_classpath(self.classname)};")
