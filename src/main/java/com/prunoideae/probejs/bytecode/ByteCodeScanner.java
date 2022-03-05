package com.prunoideae.probejs.bytecode;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.prunoideae.probejs.ProbeJS;
import dev.latvian.mods.kubejs.event.EventJS;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

public class ByteCodeScanner {
    private Multimap<String, String> clazzGraph = HashMultimap.create();
    private Map<String, String> possibleEvents = new HashMap<>();

    private static final String OBJECT = Type.getInternalName(Object.class);

    private static final String BAST_POST_FUNCTION = "post";
    private static final String BAST_POST_FUNCTION_DESC = "(Ldev/latvian/mods/kubejs/script/ScriptType;Ljava/lang/String;)Z";

    public List<Class<?>> unresolvedEvents = new ArrayList<>();
    public Map<String, Class<? extends EventJS>> resolvedEvents;


    public void scan() {

        List<ClassNode> classNodes = ClasspathHelper.walkClassFiles(ProbeJS.class.getClassLoader())
            .map(it -> {
                try (InputStream in = Files.newInputStream(it)) {
                    ClassReader reader = new ClassReader(in);
                    ClassNode classNode = new ClassNode();
                    reader.accept(classNode, 0);
                    return classNode;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).toList();

        MethodInsnNodeFilter rootCall = new MethodInsnNodeFilter(BAST_POST_FUNCTION, BAST_POST_FUNCTION_DESC, Opcodes.INVOKEVIRTUAL, 1);
        for (ClassNode classNode : classNodes) {
            scanClass(classNode);
            scanEventPosts(classNode, rootCall);
        }


        while (!nodeToScan.empty()) {
            MethodInsnNodeFilter filter = nodeToScan.pop();
            for (ClassNode classNode : classNodes) {
                scanEventPosts(classNode, filter);
            }
        }

    }


    private void scanClass(ClassNode classNode) {
        if (classNode.superName != null && !OBJECT.equals(classNode.superName)) {
            clazzGraph.put(classNode.superName, classNode.name);
        }
    }

    private Stack<MethodInsnNodeFilter> nodeToScan = new Stack<>();

    private Set<MethodInsnNodeFilter> scanned = new HashSet<>();

    private void scanEventPosts(ClassNode classNode, MethodInsnNodeFilter filter) {
        scanned.add(filter);
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                //Possible event post call
                if (filter.test(instruction)) {
                    //当前这条指令(node)是直接调用的post（或者是其他和post相同参数）的方法
                    //其中事件id作为方法的倒数第n个参数，在倒数第n个压栈，所以向上偏移n条指令
                    AbstractInsnNode previous = instruction;
                    for (int i = 0; i < filter.getInsnLookOffset(); i++) {
                        if (previous == null) {
                            ProbeJS.LOGGER.warn("Skipping post call in {}.{}, offset out of bounds", classNode.name, method.name);
                            break;
                        }
                        previous = previous.getPrevious();
                    }
                    //如果上面那条是取常量，显然这个常量就是事件名字了
                    if (previous instanceof LdcInsnNode) {
                        Object cst = ((LdcInsnNode) previous).cst;
                        if (cst instanceof String) {
                            String owner = ((MethodInsnNode) instruction).owner;
                            possibleEvents.put((String) cst, owner);
                            continue;
                        }
                    }
                    //如果上面那条是取变量压栈的指令（ALOAD）
                    if (previous instanceof VarInsnNode && previous.getOpcode() == Opcodes.ALOAD) {
                        //取第几个变量
                        int var = ((VarInsnNode) previous).var;
                        boolean isStatic = (method.access & Opcodes.ACC_STATIC) != 0;
                        //不是静态方法
                        if (!isStatic) {
                            //变量表第一个参数是this
                            var--;
                            int argumentLength = Type.getType(method.desc).getArgumentTypes().length;
                            //一定要取的是方法的参数，如果是局部变量表示这玩意已经被修改了，那么就不好分析出具体的id了
                            if (var < argumentLength) {
                                int offset = argumentLength - var;
                                MethodInsnNodeFilter nodeFilter = new MethodInsnNodeFilter(method.name, method.desc, Opcodes.INVOKEVIRTUAL, offset);
                                //当前方法调用了post, 并且向post传递的参数就是方法的参数，那就递归接着搜索对这个方法的调用
                                if (!scanned.contains(nodeFilter) && !nodeToScan.contains(nodeFilter)) {
                                    nodeToScan.push(nodeFilter);
                                }
                            }
                            continue;
                        }

                    }
                    //如果那条指令神秘都不是，不做解析
                    ProbeJS.LOGGER.info("Skipping post call in {}.{}, could not get event id", classNode.name, method.name);

                }
            }
        }
    }

    public int resolveEvents() {
        Map<String, Class<?>> eventClasses = findSubTypesOf(EventJS.class);
        Set<String> visited = new HashSet<>(eventClasses.size());
        this.resolvedEvents = new HashMap<>(possibleEvents.size());

        for (Map.Entry<String, String> entry : possibleEvents.entrySet()) {
            Class<?> clazz = eventClasses.get(entry.getValue());
            if (clazz != null) {
                visited.add(entry.getValue());
                //noinspection unchecked
                resolvedEvents.put(entry.getKey(), (Class<? extends EventJS>) clazz);
            }
        }


        int possibleUnresolvedEventsCount = 0;
        for (String clazzName : eventClasses.keySet()) {
            if (!visited.contains(clazzName)) {
                unresolvedEvents.add(eventClasses.get(clazzName));
                //如果这个事件有没有子类的话，有可能它根本没有特定的事件，就不显示给玩家了
                if (clazzGraph.containsKey(clazzName)) {
                    possibleUnresolvedEventsCount++;
                }
            }
        }
        return possibleUnresolvedEventsCount;

    }

    public Map<String, Class<?>> findSubTypesOf(Class<?> clazz) {
        Map<String, Class<?>> result = new HashMap<>();
        String internalName = Type.getInternalName(clazz);
        findSubTypesImpl(internalName, result);
        return result;
    }


    private void findSubTypesImpl(String name, Map<String, Class<?>> out) {
        try {
            String className = Type.getObjectType(name).getClassName();
            Class<?> clazz = Class.forName(className);
            //不然会在别的地方 NoClassDefFoundError
            clazz.getName();
            clazz.getConstructors();
            clazz.getMethods();
            clazz.getFields();
            out.put(name, clazz);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            ProbeJS.LOGGER.error("Could not load class {} when resolving, may be missing mods, skipping", name);
        }

        Collection<String> collection = clazzGraph.get(name);
        for (String s : collection) {
            findSubTypesImpl(s, out);
        }
    }


}
