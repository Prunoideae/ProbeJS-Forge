

from typing import Callable, Iterable, List, overload


class Component():
    '''
    A single component. Responsible for formatting a (any)
    text unit.
    '''

    def format(self, ident, pad, step) -> str: ...


class SequentialComponents(Component, list):
    '''
    A component of multiple sequential components.
    '''

    def __init__(self, iterable: Iterable) -> None:
        super().__init__(iterable)

    def format(self, ident, pad, step) -> str: ...


class MappedComponents(Component, dict):
    '''
    A component of multiple key-value mapped components.
    '''

    def __init__(self, iterable: Iterable) -> None:
        super().__init__(iterable)

    def format(self, ident, pad, step) -> str: ...


class ComponentProvider():
    def format(self) -> Component: ...


class SimpleComponent(Component):
    __formatter__: Callable[[int, str], str]

    def __init__(self, formatter: Callable[[int, str], str]) -> None:
        super().__init__()
        self.__formatter__ = formatter

    def format(self, ident, pad, step) -> str:
        return self.__formatter__(ident, pad)


class String(SimpleComponent):
    def __init__(self, content: str) -> None:
        super().__init__(lambda i, p: i * p + content)


class Concat(SequentialComponents):
    sep: str

    def __init__(self, iterable: Iterable, sep='') -> None:
        super().__init__(iterable)
        self.sep = sep

    def format(self, ident, pad, step) -> str:
        return ident * pad + self.sep.join(comp.format(0, '', 0) for comp in self)
