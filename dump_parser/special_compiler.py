
from typing import Dict, List
from compiler.class_compiler import Class, Controller, ClassType, Field, Method, Param
from compiler.string_formatter import Component, String


class Closure(Component):
    params: List[str]
    return_type: str

    def __init__(self, params: List[str], return_type: str) -> None:
        self.params = params
        self.return_type = return_type

    def format(self, ident, pad, step) -> str:
        params = [f"arg{idx}: {Controller.transform_classpath(x)}" for idx, x in enumerate(self.params)]
        return ident * pad + f"({', '.join(params)}) => {Controller.transform_classpath(self.return_type)}"


def register_formatter(cls: str):
    def inner(fn):
        ClassType.special_formatters[cls] = fn
        return fn
    return inner


def object_filter(t: str) -> str:
    if any([
        'extends' in t,
        'super' in t,
        len(t) == 1,
        '.' not in t
    ]):
        return "object"
    return t


@register_formatter("java.util.function.BiConsumer")
def biconsumer(clazz: ClassType):
    if len(clazz.params) != 2:
        return String(Controller.transform_classpath(clazz.classpath))
    params = [object_filter(x) for x in clazz.params]
    return Closure(params, "void")


@register_formatter("java.util.function.Consumer")
def consumer(clazz: ClassType):
    if len(clazz.params) != 1:
        return String(Controller.transform_classpath(clazz.classpath))
    params = [object_filter(x) for x in clazz.params]
    return Closure(params, "void")


@register_formatter("java.util.function.Supplier")
def supplier(clazz: ClassType):
    if len(clazz.params) != 1:
        return String(Controller.transform_classpath(clazz.classpath))
    params = [object_filter(x) for x in clazz.params]
    return Closure([], params[0])


@register_formatter("java.util.function.BiFunction")
def bifunction(clazz: ClassType):
    if len(clazz.params) != 3:
        return String(Controller.transform_classpath(clazz.classpath))
    params = [object_filter(x) for x in clazz.params]
    return Closure(params[:-1], params[-1])


@register_formatter("java.util.function.Function")
def function(clazz: ClassType):
    if len(clazz.params) != 2:
        return String(Controller.transform_classpath(clazz.classpath))
    params = [object_filter(x) for x in clazz.params]
    return Closure(params[:-1], params[-1])


@register_formatter("java.util.function.Predicate")
def predicate(clazz: ClassType):
    if len(clazz.params) != 1:
        return String(Controller.transform_classpath(clazz.classpath))
    params = [object_filter(x) for x in clazz.params]
    return Closure(params, "boolean")


@register_formatter("java.util.function.ToIntFunction")
def to_int_function(clazz: ClassType):
    if len(clazz.params) != 1:
        return String(Controller.transform_classpath(clazz.classpath))
    params = [object_filter(x) for x in clazz.params]
    return Closure(params, "int")


@register_formatter("java.util.function.BinaryOperator")
def binary_operator(clazz: ClassType):
    if len(clazz.params) != 1:
        return String(Controller.transform_classpath(clazz.classpath))
    params = [object_filter(x) for x in clazz.params]
    return Closure([params[0], params[0]], params[0])


@register_formatter("java.util.function.BooleanSupplier")
def boolean_supplier(clazz: ClassType):
    if len(clazz.params) != 0:
        return String(Controller.transform_classpath(clazz.classpath))
    return Closure([], "boolean")


@register_formatter("java.util.function.IntFunction")
def int_function(clazz: ClassType):
    if len(clazz.params) != 1:
        return String(Controller.transform_classpath(clazz.classpath))
    params = [object_filter(x) for x in clazz.params]
    return Closure(["int"], params[0])


@register_formatter("java.util.function.ToLongFunction")
def to_long_function(clazz: ClassType):
    if len(clazz.params) != 1:
        return String(Controller.transform_classpath(clazz.classpath))
    params = [object_filter(x) for x in clazz.params]
    return Closure(params, "long")


@register_formatter("java.util.function.UnaryOperator")
def unary_operator(clazz: ClassType):
    if len(clazz.params) != 1:
        return String(Controller.transform_classpath(clazz.classpath))
    params = [object_filter(x) for x in clazz.params]
    return Closure([params[0]], params[0])


class DummyRecipesClass(Class):
    class DummyRecipeField(Field):
        def __init__(self, mod: str, type: str) -> None:
            super().__init__({
                "name": mod,
                "static": False,
                "type": {
                    'classname': type,
                    'parameterized': []
                },
                "value": None
            })

    def __init__(self, mod_recipes: Dict[str, "DummyRecipeClass"]) -> None:
        super().__init__({
            'name': f"com.prunoideae.probejs.RecipesHolder",
            'super': None,
            'methods': [],
            "fields": [],
            "constructors": []
        })
        self.fields = [DummyRecipesClass.DummyRecipeField(k, v.orig_name) for k, v in mod_recipes.items()]


class DummyRecipeClass(Class):
    def __init__(self, mod: str, methods: List[Method]) -> None:
        super().__init__({
            'name': f"com.prunoideae.probejs.recipes.{mod.capitalize()}",
            'super': None,
            'methods': [],
            'fields': [],
            'constructors': []
        })
        self.methods = methods


class DummyRecipeMethod(Method):
    class DummyVarParam(Param):

        def __init__(self) -> None: ...

        def format(self) -> Component:
            return String("...args: object")

    def __init__(self, name: str, classpath: str) -> None:
        super().__init__({
            "name": name,
            "return_type": {
                "classname": classpath,
                "parameterized": []
            },
            "params": [],
            "static": False
        })
        self.params.append(DummyRecipeMethod.DummyVarParam())
