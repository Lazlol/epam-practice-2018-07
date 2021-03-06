package com.compiler.commands;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import com.compiler.Compiler;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;

public class ChangeIdx implements Command {

  @Override
  public InsnList execute(String val) {
    int number = Integer.parseInt(val);
    InsnList list = new InsnList();
    Compiler.pushRightType(number, list);
    list.add(new MethodInsnNode(INVOKESTATIC, Compiler.className, "changeIdx", "(I)V", false));
    return list;
  }
}
