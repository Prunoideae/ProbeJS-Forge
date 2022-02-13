
import json

from .string_formatter import Component, ComponentProvider, Concat, String
from typing import Any, Callable, Dict, List, TypeVar, Type, Union

T = TypeVar('T')


def singleton(deco: Type[T]) -> T:
    return deco()


@singleton
class Controller():
    typing_transformer: Dict = {}
    value_transformer: Dict = {}
    classes_remapper: Dict = {}
    keyword: List = []

    array_primitives = {
        '[Z': 'boolean',
        '[B': 'number',
        '[I': 'number',
        '[S': 'number',
        '[J': 'number',
        '[F': 'number',
        '[D': 'number',
        '[C': 'string'
    }

    def setup(self, typing_transformer, value_transformer, keyword):
        self.typing_transformer = typing_transformer
        self.value_transformer = value_transformer
        self.keyword = keyword

    def transform_name(self, name: str) -> str:
        return name if name not in self.keyword else name + "_"

    def transform_classpath(self, classpath: str) -> str:
        for k, v in self.array_primitives.items():
            if classpath.endswith(k):
                return v + "[]" * classpath.count("[")
        array_count = classpath.count("[")
        if array_count > 0:
            classpath = classpath[array_count + 1:-1]
        if classpath in self.classes_remapper:
            return self.classes_remapper[classpath] + array_count * '[]'
        if classpath in self.typing_transformer:
            return self.typing_transformer[classpath] + array_count * '[]'
        classpath = classpath.replace('$', '.')
        return '.'.join(self.transform_name(p)
                        for p in classpath.split('.')) + array_count * '[]'


class ClassType(ComponentProvider):
    special_formatters: Dict[str, Callable[['ClassType'], Component]] = {}

    classpath: str
    params: List[str]

    def __init__(self, json_entry: Dict) -> None:
        self.classpath = json_entry['classname']
        self.params = json_entry['parameterized']

    def format(self) -> Component:
        if self.classpath not in self.special_formatters:
            return String(Controller.transform_classpath(self.classpath))
        else:
            return self.special_formatters[self.classpath](self)


class Param(ComponentProvider):
    name: str
    type: ClassType

    def __init__(self, json_entry: Dict) -> None:
        self.name = json_entry['name']
        self.type = ClassType(json_entry['type'])

    def format(self) -> Component:
        return Concat([
            String(Controller.transform_name(self.name)),
            String(": "),
            self.type.format()
        ])


class Field(ComponentProvider):
    name: str
    static: bool
    value: Union[None, Any]
    type: ClassType

    def __init__(self, json_entry: Dict) -> None:
        self.name = json_entry['name']
        self.type = ClassType(json_entry['type'])
        self.static = json_entry['static']
        self.value = json_entry.get('value')

    def format(self) -> Component:
        components = []
        if self.static:
            components.append(String("static "))
        components += [String(self.name), String(": ")]
        if all([self.type.classpath in Controller.value_transformer,
                self.static,
                self.value is not None
                ]):
            components.append(String(json.dumps(self.value)))
        else:
            components.append(self.type.format())
        components.append(";")
        return Concat(components)


class Method(ComponentProvider):
    name: str
    static: bool
    return_type: ClassType
    params: List[ClassType]

    def __init__(self, json_entry: Dict) -> None:
        self.name = json_entry['name']
        self.return_type = ClassType(json_entry['return_type'])
        self.static = json_entry['static']
        self.params = [Param(e) for e in json_entry['params']]

    def format(self) -> Component:
        components = []
        if self.static:
            components.append(String("static "))

        components += [
            String(self.name),
            String("("),
            Concat([p.format() for p in self.params], sep=", "),
            String("): "),
            self.return_type.format(),
            String(";")
        ]

        return Concat(components)


class Constructor(ComponentProvider):
    params: List[ClassType]

    def __init__(self, json_entry: Dict) -> None:
        self.params = [Param(e) for e in json_entry['params']]

    def format(self) -> Component:
        return Concat([
            String("constructor("),
            Concat([p.format() for p in self.params], sep=", "),
            String(");")
        ])


class Class(ComponentProvider):
    path: str
    name: str
    orig_name: str
    superclass: Union[None, str]
    fields: List[Field]
    methods: List[Method]
    constructors: List[Constructor]

    def __init__(self, json_entry: Dict) -> None:
        self.orig_name = json_entry['name']
        try:
            self.path, self.name = self.orig_name.rsplit(".", maxsplit=1)
        except:
            self.path = ''
            self.name = self.orig_name

        self.superclass = json_entry.get('super')
        self.fields = [Field(f) for f in json_entry['fields']]
        self.methods = [Method(m) for m in json_entry['methods']]
        self.constructors = [Constructor(c) for c in json_entry['constructors']]

    def format(self) -> Component:
        class ClassCompiler(Component):
            clazz: Class

            def __init__(self, clazz) -> None:
                self.clazz = clazz

            def format(self, ident, pad, step) -> str:
                stepped = ident + step
                used = set()

                classdef = f"class {self.clazz.name} "
                if self.clazz.superclass is not None and self.clazz.superclass not in Controller.typing_transformer:
                    classdef += f"extends {Controller.transform_classpath(self.clazz.superclass)} "

                result = [ident * pad + classdef + "{"]
                for method in self.clazz.methods:
                    used.add(method.name)
                    result.append(method.format().format(stepped, pad, step))
                for field in filter(lambda x: x.name not in used, self.clazz.fields):
                    result.append(field.format().format(stepped, pad, step))
                for constructor in self.clazz.constructors:
                    result.append(constructor.format().format(stepped, pad, step))
                result.append(ident * pad + "}")
                return "\n".join(result)
        return ClassCompiler(self)
