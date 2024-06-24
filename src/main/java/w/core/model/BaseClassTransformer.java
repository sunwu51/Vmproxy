package w.core.model;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.SimpleCompiler;
import w.Global;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Frank
 * @date 2023/12/21 23:45
 */
@Getter
@Setter
public abstract class BaseClassTransformer implements ClassFileTransformer {
    protected UUID uuid = UUID.randomUUID();

    protected String className;

    protected String traceId;

    protected int status;



    public abstract byte[] transform(Class<?> className, byte[] origin) throws Exception;

    public abstract String desc();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] origin) throws IllegalClassFormatException {
        if (className == null) return origin;
        className = className.replace("/", ".");
        if (Objects.equals(this.className, className)) {
            try{
                byte[] r = transform(classBeingRedefined, origin);
                Global.info(className + " transformer " + uuid +  " added success <(^－^)>");
                return r;
            } catch (Exception e) {
                Global.error(className + " transformer " + uuid + " added fail -(′д｀)-: ", e);
                // async to delete, because current thread holds the class lock
                CompletableFuture.runAsync(() -> Global.deleteTransformer(uuid));
            }
        }
        return null;
    }


    protected byte[] compileMethod(String methodBody) throws CompileException {
        SimpleCompiler compiler = new SimpleCompiler();
        String packageName = className.substring(0, className.lastIndexOf("."));
        String simpleClassName = className.substring(className.lastIndexOf(".") +1);
        compiler.cook("package " + packageName +";\n import java.util.*;\n public class " + simpleClassName + " {" + methodBody + "}");
        return compiler.getBytecodes().get(className);
    }

    protected byte[] compileDynamicCodeBlock(String content)  throws CompileException {
        SimpleCompiler compiler = new SimpleCompiler();
        compiler.cook("package w;\n import java.util.*;\n public class Dynamic { " + content +" }");
        return compiler.getBytecodes().get("w.Dynamic");
    }

    protected static boolean stringArrEquals(String[] a1, String[] a2) {
        if (a1 == null && a2 == null) { return  true;}
        if (a1 == null || a2 == null) { return false; }
        return Arrays.toString(a1).equals(Arrays.toString(a2));
    }

    protected String paramTypesToDescriptor(List<String> paramTypes) {
        StringBuilder s = new StringBuilder();
        for (String paramType : paramTypes) {
            s.append(paramTypeToDescriptor(paramType));
        }
        return "(" + s + ")";
    }

    protected String paramTypeToDescriptor(String paramType) {
        if (paramType == null || paramType.length() == 0 || paramType.contains("<")) {
            throw new IllegalArgumentException("error type");
        }
        switch (paramType) {
            case "int":
                return "I";
            case "long":
                return "J";
            case "float":
                return "F";
            case "boolean":
                return "Z";
            case "double":
                return "D";
            case "byte":
                return "B";
            case "short":
                return "S";
            case "char":
                return "C";
            default:
                if (paramType.endsWith("[]")) {
                    return "[" + paramTypeToDescriptor(paramType.substring(0, paramType.length() - 2));
                }
                return "L" + paramType.replace(".", "/") + ";";
        }
    }
}