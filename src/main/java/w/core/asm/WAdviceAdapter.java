package w.core.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.List;

/**
 * @author Frank
 * @date 2024/6/22 19:33
 */
public class WAdviceAdapter extends AdviceAdapter {
    protected WAdviceAdapter(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(api, methodVisitor, access, name, descriptor);
    }

    protected int asmStoreStartTime(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        int startTimeVarIndex = newLocal(Type.LONG_TYPE);
        mv.visitVarInsn(LSTORE, startTimeVarIndex);
        return startTimeVarIndex;
    }

    protected int asmCalculateCost(MethodVisitor mv, int startTimeVarIndex) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        mv.visitVarInsn(LLOAD, startTimeVarIndex);
        mv.visitInsn(LSUB);
        int durationVarIndex = newLocal(Type.LONG_TYPE);
        mv.visitVarInsn(LSTORE, durationVarIndex);
        return durationVarIndex;
    }

    protected int asmStoreParamsString(MethodVisitor mv, int printFormat) {
        loadArgArray();
        if (printFormat == 1) {
            mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "toString", "([Ljava/lang/Object;)Ljava/lang/String;", false);
        } else if (printFormat == 2) {
            mv.visitMethodInsn(INVOKESTATIC, "w/Global", "toJson", "(Ljava/lang/Object;)Ljava/lang/String;", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, "w/Global", "toString", "(Ljava/lang/Object;)Ljava/lang/String;", false);
        }
        int paramsVarIndex = newLocal(Type.getType(String.class));
        mv.visitVarInsn(ASTORE, paramsVarIndex);
        return paramsVarIndex;
    }

    /**
     * return value toString and store in local variable, return the local variable index
     *
     * It's very useful for enhancement like watch out-watch.
     * @param mv
     * @param descriptor
     * @return
     */
    protected int asmStoreRetString(MethodVisitor mv, String descriptor, int printFormat) {
        int returnValueVarIndex = newLocal(Type.getType(String.class));
        return asmStoreRetString(mv, descriptor, printFormat, returnValueVarIndex);
    }

    protected int asmStoreRetString(MethodVisitor mv, String descriptor, int printFormat, int returnValueVarIndex) {
        Type returnType = Type.getReturnType(descriptor);
        switch (returnType.getSort()) {
            case Type.ARRAY:
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "toString", "([Ljava/lang/Object;)Ljava/lang/String;", false);
                break;
            case Type.DOUBLE:
            case Type.LONG:
                mv.visitInsn(DUP2);
                box(returnType);
                formatResult(printFormat);
                break;
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.INT:
            case Type.FLOAT:
            case Type.SHORT:
            case Type.BYTE:
                mv.visitInsn(DUP);
                box(returnType);
                formatResult(printFormat);
                break;
            case Type.OBJECT:
                mv.visitInsn(DUP);
                formatResult(printFormat);
                break;
            case Type.VOID:
            default:
                mv.visitLdcInsn("void");
        }
        mv.visitVarInsn(ASTORE, returnValueVarIndex);
        return returnValueVarIndex;
    }


    private void formatResult(int printFormat) {
        if (printFormat == 1) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
        } else if (printFormat == 2) {
            mv.visitMethodInsn(INVOKESTATIC, "w/Global", "toJson",   "(Ljava/lang/Object;)Ljava/lang/String;", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, "w/Global", "toString", "(Ljava/lang/Object;)Ljava/lang/String;", false);
        }
    }


    protected void asmGenerateStringBuilder(MethodVisitor mv, List<SbNode> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);

        for (SbNode subStringNode : list) {
            subStringNode.loadAndAppend(mv);
        }
    }
}
