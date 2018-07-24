package com.compiler;

import static jdk.internal.org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_STATIC;
import static jdk.internal.org.objectweb.asm.Opcodes.ALOAD;
import static jdk.internal.org.objectweb.asm.Opcodes.DUP2;
import static jdk.internal.org.objectweb.asm.Opcodes.DUP2_X1;
import static jdk.internal.org.objectweb.asm.Opcodes.GETSTATIC;
import static jdk.internal.org.objectweb.asm.Opcodes.GOTO;
import static jdk.internal.org.objectweb.asm.Opcodes.IADD;
import static jdk.internal.org.objectweb.asm.Opcodes.IALOAD;
import static jdk.internal.org.objectweb.asm.Opcodes.IASTORE;
import static jdk.internal.org.objectweb.asm.Opcodes.IFGE;
import static jdk.internal.org.objectweb.asm.Opcodes.IFNE;
import static jdk.internal.org.objectweb.asm.Opcodes.ILOAD;
import static jdk.internal.org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static jdk.internal.org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static jdk.internal.org.objectweb.asm.Opcodes.IREM;
import static jdk.internal.org.objectweb.asm.Opcodes.ISUB;
import static jdk.internal.org.objectweb.asm.Opcodes.PUTSTATIC;
import static jdk.internal.org.objectweb.asm.Opcodes.RETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.SIPUSH;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.ICONST_0;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldInsnNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.InsnNode;
import jdk.internal.org.objectweb.asm.tree.JumpInsnNode;
import jdk.internal.org.objectweb.asm.tree.LabelNode;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.VarInsnNode;

public class Compiler {

  public static String className;

  public static void main(String[] args) {
    compile("Compiler", "test8.bf", ".");
  }

  public static void pushRightType(int number, InsnList list) {
    if (number >= 0 && number < 6) {
      list.add(new InsnNode(ICONST_0 + number));
    } else if (number > -129 && number < 128) {
      list.add(new VarInsnNode(BIPUSH, number));
    } else {
      list.add(new VarInsnNode(SIPUSH, number));
    }
  }

  public static void compile(String name, String pathToCompile, String pathToWrite) {
    className = name;
    ClassNode myClass = new ClassNode();
    myClass.version = Opcodes.V1_8;
    myClass.access = ACC_PUBLIC;
    myClass.name = className;
    myClass.superName = "java/lang/Object";
    FieldNode intArray = new FieldNode(ACC_PRIVATE + ACC_STATIC, "array", "[I", null, null);
    FieldNode idx = new FieldNode(ACC_PRIVATE + ACC_STATIC, "idx", "I", null, 0);
    FieldNode size = new FieldNode(ACC_PRIVATE + ACC_STATIC, "size", "I", null, 30000);
    FieldNode max = new FieldNode(ACC_PRIVATE + ACC_STATIC, "maxData", "I", null, 256);
    FieldNode InputStream = new FieldNode(ACC_PRIVATE + ACC_STATIC, "reader",
        "Ljava/io/InputStreamReader;", null, null);

    MethodNode constructor = new MethodNode(ACC_PUBLIC, "<init>", "()V", null, null);
    InsnList bytecode = constructor.instructions;
    bytecode.add(new VarInsnNode(ALOAD, 0));
    bytecode.add(new MethodInsnNode(INVOKESPECIAL, myClass.superName, "<init>", "()V", false));
    bytecode.add(new InsnNode(RETURN));

    MethodNode changeIdx = new MethodNode(ACC_PUBLIC + ACC_STATIC, "changeIdx", "(IZ)V", null,
        null);
    bytecode = changeIdx.instructions;
    bytecode.add(getIdxChanger());

    MethodNode changeData = new MethodNode(ACC_PUBLIC + ACC_STATIC, "changeData", "(IZ)V", null,
        null);
    bytecode = changeData.instructions;
    bytecode.add(getDataChanger());

    MethodNode print = new MethodNode(ACC_PUBLIC + ACC_STATIC, "print", "()V", null, null);
    bytecode = print.instructions;
    bytecode.add(getPrinter());

    MethodNode input = new MethodNode(ACC_PUBLIC + ACC_STATIC, "read", "()V", null, null);
    bytecode = input.instructions;
    bytecode.add(getReader());

    MethodNode main = Parser.getMain(pathToCompile);

    myClass.fields.add(intArray);
    myClass.fields.add(idx);
    myClass.fields.add(size);
    myClass.fields.add(max);
    myClass.fields.add(InputStream);

    myClass.methods.add(constructor);
    myClass.methods.add(changeIdx);
    myClass.methods.add(changeData);
    myClass.methods.add(input);
    myClass.methods.add(print);
    myClass.methods.add(main);

    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    myClass.accept(writer);

    byte[] bytes = writer.toByteArray();
    try (FileOutputStream output = new FileOutputStream(
        new File(pathToWrite + "\\" + className + ".class"))) {
      output.write(bytes);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static InsnList getIdxChanger() {
    InsnList list = new InsnList();
    list.add(new VarInsnNode(ILOAD, 1));
    LabelNode idxBackward = new LabelNode();
    LabelNode idxIfs = new LabelNode();
    list.add(new JumpInsnNode(IFNE, idxBackward));
    list.add(new FieldInsnNode(GETSTATIC, className, "idx", "I"));
    list.add(new VarInsnNode(ILOAD, 0));
    list.add(new InsnNode(IADD));
    list.add(new JumpInsnNode(GOTO, idxIfs));
    list.add(idxBackward);
    list.add(new FieldInsnNode(GETSTATIC, className, "idx", "I"));
    list.add(new VarInsnNode(ILOAD, 0));
    list.add(new InsnNode(ISUB));
    list.add(idxIfs);
    list.add(new FieldInsnNode(GETSTATIC, className, "maxData", "I"));
    list.add(new InsnNode(IREM));
    list.add(new FieldInsnNode(PUTSTATIC, className, "idx", "I"));
    LabelNode idxEnd = new LabelNode();
    list.add(new FieldInsnNode(GETSTATIC, className, "idx", "I"));
    list.add(new JumpInsnNode(IFGE, idxEnd));
    list.add(new FieldInsnNode(GETSTATIC, className, "size", "I"));
    list.add(new FieldInsnNode(GETSTATIC, className, "idx", "I"));
    list.add(new InsnNode(IADD));
    list.add(new FieldInsnNode(PUTSTATIC, className, "idx", "I"));
    list.add(idxEnd);
    list.add(new InsnNode(RETURN));
    return list;
  }

  private static InsnList getDataChanger() {
    InsnList list = new InsnList();
    list.add(new FieldInsnNode(GETSTATIC, className, "array", "[I")); // ref
    list.add(new FieldInsnNode(GETSTATIC, className, "idx", "I"));    //ref , idx
    list.add(new InsnNode(DUP2)); //ref,idx,ref,idx
    list.add(new VarInsnNode(ILOAD, 1));// ref,idx,ref,idx,var1
    LabelNode dataBackward = new LabelNode();
    LabelNode dataIfs = new LabelNode();
    list.add(new JumpInsnNode(IFNE, dataBackward));//ref,idx,ref,idx
    list.add(new InsnNode(IALOAD)); //ref , idx , val
    list.add(new VarInsnNode(ILOAD, 0));// ref ,idx, val ,var0
    list.add(new InsnNode(IADD));//ref,idx,val1
    list.add(new JumpInsnNode(GOTO, dataIfs));
    list.add(dataBackward);
    list.add(new InsnNode(IALOAD));
    list.add(new VarInsnNode(ILOAD, 0));
    list.add(new InsnNode(ISUB));
    list.add(dataIfs);
    list.add(new FieldInsnNode(GETSTATIC,className,"maxData","I"));
    list.add(new InsnNode(IREM));
    list.add(new InsnNode(IASTORE));
    LabelNode dataEnd = new LabelNode();
    list.add(new FieldInsnNode(GETSTATIC, className, "array", "[I"));
    list.add(new FieldInsnNode(GETSTATIC, className, "idx", "I"));
    list.add(new InsnNode(IALOAD));
    list.add(new JumpInsnNode(IFGE, dataEnd));
    list.add(new FieldInsnNode(GETSTATIC, className, "maxData", "I"));
    list.add(new FieldInsnNode(GETSTATIC, className, "array", "[I"));
    list.add(new FieldInsnNode(GETSTATIC, className, "idx", "I"));
    list.add(new InsnNode(DUP2_X1));
    list.add(new InsnNode(IALOAD));
    list.add(new InsnNode(IADD));
    list.add(new InsnNode(IASTORE));
    list.add(dataEnd);
    list.add(new InsnNode(RETURN));
    return list;

  }

  private static InsnList getPrinter() {
    InsnList list = new InsnList();
    list.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
    list.add(new FieldInsnNode(GETSTATIC, className, "array", "[I"));
    list.add(new FieldInsnNode(GETSTATIC, className, "idx", "I"));
    list.add(new InsnNode(IALOAD));
    list
        .add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false));
    list.add(new InsnNode(RETURN));
    return list;
  }

  private static InsnList getReader() {
    InsnList list = new InsnList();
    list.add(new FieldInsnNode(GETSTATIC, className, "array", "[I"));
    list.add(new FieldInsnNode(GETSTATIC, className, "idx", "I"));
    list
        .add(new FieldInsnNode(GETSTATIC, className, "reader", "Ljava/io/InputStreamReader;"));
    list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/InputStreamReader", "read", "()I", false));
    list.add(new InsnNode(IASTORE));
    list.add(new InsnNode(RETURN));
    return list;
  }
}
