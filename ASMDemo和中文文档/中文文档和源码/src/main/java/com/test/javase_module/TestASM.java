package com.test.javase_module;


import static org.graalvm.compiler.bytecode.Bytecodes.DUP;
import static org.graalvm.compiler.bytecode.Bytecodes.INVOKESPECIAL;
import static org.graalvm.compiler.bytecode.Bytecodes.INVOKEVIRTUAL;
import static org.graalvm.compiler.bytecode.Bytecodes.IRETURN;
import static org.graalvm.compiler.bytecode.Bytecodes.LSTORE;
import static org.graalvm.compiler.bytecode.Bytecodes.LSUB;
import static org.graalvm.compiler.bytecode.Bytecodes.RETURN;
import static groovyjarjarasm.asm.Opcodes.ACC_ABSTRACT;
import static groovyjarjarasm.asm.Opcodes.ACC_FINAL;
import static groovyjarjarasm.asm.Opcodes.ACC_INTERFACE;
import static groovyjarjarasm.asm.Opcodes.ACC_PUBLIC;
import static groovyjarjarasm.asm.Opcodes.ACC_STATIC;
import static groovyjarjarasm.asm.Opcodes.ASM4;
import static groovyjarjarasm.asm.Opcodes.ATHROW;
import static groovyjarjarasm.asm.Opcodes.GETSTATIC;
import static groovyjarjarasm.asm.Opcodes.INVOKESTATIC;
import static groovyjarjarasm.asm.Opcodes.LLOAD;
import static groovyjarjarasm.asm.Opcodes.NEW;
import static groovyjarjarasm.asm.Opcodes.V1_5;

import com.utils.PathUtils;

import org.gradle.internal.impldep.org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import groovyjarjarasm.asm.AnnotationVisitor;
import groovyjarjarasm.asm.Attribute;
import groovyjarjarasm.asm.ClassReader;
import groovyjarjarasm.asm.ClassVisitor;
import groovyjarjarasm.asm.ClassWriter;
import groovyjarjarasm.asm.FieldVisitor;
import groovyjarjarasm.asm.MethodVisitor;

public class TestASM {


    public static class ClassPrinterVisitor extends ClassVisitor {

        public ClassPrinterVisitor() {
            super(ASM4);
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            System.out.println("类信息："+name + " extends " + superName + " {");
        }

        public void visitSource(String source, String debug) {
        }

        public void visitOuterClass(String owner, String name, String desc) {
        }


        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return null;
        }

        public void visitAttribute(Attribute attr) {
        }

        public void visitInnerClass(String name, String outerName, String innerName, int access) {
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            System.out.println(" " + desc + " " + name);
            return null;
        }

        public MethodVisitor visitMethod(int access, String name,String desc, String signature, String[] exceptions) {
            System.out.println("方法: " + name + desc);

            return null;
        }

        public void visitEnd() {
            System.out.println("}");
        }
    }


    /**
     * 为类添加方法
     */
    public static class AddFieldAdapter extends ClassVisitor {

        private int fAcc;
        private String fName;
        private String fDesc;
        private boolean isFieldPresent;

        public AddFieldAdapter(ClassVisitor cv, int fAcc, String fName, String fDesc) {
            super(ASM4, cv);
            this.fAcc = fAcc;
            this.fName = fName;
            this.fDesc = fDesc;
        }
        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if (name.equals(fName)) {
                isFieldPresent = true;
            }
            return cv.visitField(access, name, desc, signature, value);
        }
        @Override
        public void visitEnd() {
            if (!isFieldPresent) {
                FieldVisitor fv = cv.visitField(fAcc, fName, fDesc, null, null);
                if (fv != null) {
                    fv.visitEnd();
                }
            }
            cv.visitEnd();
        }
    }




    /**
     * 测试类访问
     * @throws IOException
     */
    private static void testClassVisitor() throws IOException {
        System.err.println("===================>>>>>>>>>Java工程运行结果");
        ClassPrinterVisitor cp = new ClassPrinterVisitor();
        ClassReader cr = new ClassReader("java.lang.Runnable");
        cr.accept(cp, 0);
    }


    /**
     生成一个类
     package pkg;
     public interface Comparable extends Mesurable {
     int LESS = -1;
     int EQUAL = 0;
     int GREATER = 1;
     int compareTo(Object o);
     }
     */
    private static void testClassWriter() throws IOException {

        ClassWriter cw = new ClassWriter(0);

        //生成class版本为1.5的类
        cw.visit(V1_5, ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE, "pkg/Comparable", null, "java/lang/Object", new String[] { "pkg/Mesurable" });

        //生成一个整形字段
        cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "LESS", "I", null, new Integer(-1)).visitEnd();
        cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "EQUAL", "I", null, new Integer(0)).visitEnd();
        cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "GREATER", "I", null, new Integer(1)).visitEnd();

        //生成方法
        cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "compareTo", "(Ljava/lang/Object;)I", null, null).visitEnd();


        cw.visitEnd();

        byte[] b = cw.toByteArray();

//        cw.newClass()

        String path=PathUtils.getCurrentClassPath(TestASM.class)+ File.separator+"Comparable.class";
        System.err.println("类生成的位置:"+path);

        IOUtils.write(b,new FileOutputStream(path));

    }


    /**
     * 测试修改类
     * @throws IOException
     */
    public static void testChangeClass() throws IOException {

        System.err.println("===================>>>>>>>>>Java工程运行结果");

//        AddFieldAdapter cp=new AddFieldAdapter(new ClassVisitor(ASM4) {
//
//            @Override
//            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
//                return super.visitMethod(access, name, descriptor, signature, exceptions);
//            }
//        }, ACC_PUBLIC, "addMethod", Type.getDescriptor(Integer.class));

        ClassReader cr = new ClassReader("com.test.javase_module.TestFunction");
        final ClassWriter cw=new ClassWriter(cr,0);

//        cr.accept(cw, 0);//可以直接接受一个writer,实现复制
        cr.accept(new ClassVisitor(ASM4,cw) {//接受一个带classWriter的visitor，实现定制化方法拷贝或者属性删除字段

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

                System.out.println("visit method:"+name+"====> "+descriptor);
                if("testA".equals(name)){//拷贝的过程中删除一个方法
                    return null;
                }

                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }

            @Override
            public void visitEnd() {
                //拷贝过程中，增加一个字段（注意不能重复）
                cv.visitField(ACC_PUBLIC, "age", "I", null, null).visitEnd();

                //拷贝的过程中增加一个方法
                cv.visitMethod(ACC_PUBLIC+ACC_STATIC,"getAge","(V)I",null,new String[]{"java.io.IOException"}).visitEnd();

                super.visitEnd();
            }
        },0);


        String path=PathUtils.getCurrentClassPath(TestASM.class)+ File.separator+"TestFunction3.class";
        System.err.println("类生成的位置:"+path);
        IOUtils.write(cw.toByteArray(),new FileOutputStream(path));

    }




    public static class InspectAdapter extends MethodVisitor {

        public InspectAdapter(MethodVisitor mv) {
            super(ASM4, mv);
        }


        public void visitInsn(int opcode) {
        //这里是访问语句结束，在return结束之前添加语句

//            其中的 owner 必须被设定为所转换类的名字。现在必须在任意 RETURN 之前添加其他四条
//            指令，还要在任何 xRETURN 或 ATHROW 之前添加，它们都是终止该方法执行过程的指令。这些
//            指令没有任何参数，因此在 visitInsn 方法中访问。于是，可以重写这一方法，以增加指令：
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                //在方法return之前添加代码

                mv.visitMethodInsn(INVOKESTATIC,"java/lang/System",  "currentTimeMillis", "()J",false);
                mv.visitIntInsn(LSTORE,3);

                mv.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream");
                mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                mv.visitInsn(DUP);

                mv.visitMethodInsn(INVOKESPECIAL,"java/lang/StringBuilder","<init>","()V",false);
                mv.visitLdcInsn("cost:");//就是传入一个字符串常量
                mv.visitMethodInsn(INVOKEVIRTUAL,"java/lang/StringBuilder","append","(Ljava/lang/String;)Ljava/lang/StringBuilder",false);
                mv.visitVarInsn(LLOAD, 3);
                mv.visitVarInsn(LLOAD,1);
                mv.visitInsn(LSUB);
//
                mv.visitMethodInsn(INVOKEVIRTUAL,"java/lang/StringBuilder","append","(J)Ljava/lang/StringBuilder",false);
                mv.visitMethodInsn(INVOKEVIRTUAL,"java/lang/StringBuilder","toString","()Ljava/lang/String",false);
                mv.visitMethodInsn(INVOKEVIRTUAL,"java/io/PrintStream","println","(Ljava/lang/String;)V",false);

            }

            mv.visitInsn(opcode);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            //方法开始（可以在此处添加代码，在原来的方法之前执行）
            mv.visitMethodInsn(INVOKESTATIC,"java/lang/System","currentTimeMillis", "()J");
            mv.visitIntInsn(LSTORE,1);
//            mv.visitFieldInsn(GETSTATIC, owner, "timer", "J");
//            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J");
//            mv.visitInsn(LSUB);
//            mv.visitFieldInsn(PUTSTATIC, owner, "timer", "J");
        }


        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            //更新操作数栈
            super.visitMaxs(maxStack, maxLocals);
        }

    }


    public static class ThirdMethodCall extends MethodVisitor {

        public ThirdMethodCall(MethodVisitor mv) {
            super(ASM4, mv);
        }

        public void visitInsn(int opcode) {
            //这里是访问语句结束，在return结束之前添加语句

//            其中的 owner 必须被设定为所转换类的名字。现在必须在任意 RETURN 之前添加其他四条
//            指令，还要在任何 xRETURN 或 ATHROW 之前添加，它们都是终止该方法执行过程的指令。这些
//            指令没有任何参数，因此在 visitInsn 方法中访问。于是，可以重写这一方法，以增加指令：
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                //在方法return之前添加代码

                mv.visitMethodInsn(INVOKESTATIC,"java/lang/System",  "currentTimeMillis", "()J",false);
                mv.visitIntInsn(LSTORE,3);

                mv.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream");
                mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                mv.visitInsn(DUP);

                mv.visitMethodInsn(INVOKESPECIAL,"java/lang/StringBuilder","<init>","()V",false);
                mv.visitLdcInsn("cost:");//就是传入一个字符串常量
                mv.visitMethodInsn(INVOKEVIRTUAL,"java/lang/StringBuilder","append","(Ljava/lang/String;)Ljava/lang/StringBuilder",false);
                mv.visitVarInsn(LLOAD, 3);
                mv.visitVarInsn(LLOAD,1);
                mv.visitInsn(LSUB);
//
                mv.visitMethodInsn(INVOKEVIRTUAL,"java/lang/StringBuilder","append","(J)Ljava/lang/StringBuilder",false);
                mv.visitMethodInsn(INVOKEVIRTUAL,"java/lang/StringBuilder","toString","()Ljava/lang/String",false);
                mv.visitMethodInsn(INVOKEVIRTUAL,"java/io/PrintStream","println","(Ljava/lang/String;)V",false);

                mv.visitVarInsn(LLOAD, 3);
                mv.visitMethodInsn(INVOKESTATIC,"com/utils/Tool","useTool","(J)V",false);


            }

            mv.visitInsn(opcode);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            //方法开始（可以在此处添加代码，在原来的方法之前执行）
            mv.visitMethodInsn(INVOKESTATIC,"java/lang/System","currentTimeMillis", "()J",false);
            mv.visitIntInsn(LSTORE,1);

        }


        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            //更新操作数栈， 自己更新操作数栈很
            super.visitMaxs(maxStack, maxLocals);
        }
    }

    /**
     * 测试修改方法
     */
    public static void testChangeMethod() throws IOException{

        ClassReader cr = new ClassReader("com.test.javase_module.TestFunction");


        //在使用 new ClassWriter(0)时，不会自动计算任何东西。必须自行计算帧、局部变 量与操作数栈的大小。

        //在使用 new ClassWriter(ClassWriter.COMPUTE_MAXS)时，将为你计算局部变量与操作数栈部分的大小。
        // 还是必须调用 visitMaxs，但可以使用任何参数：它们将被忽略并重新计算。使用这一选项时，仍然必须自行计算这些帧。

        //在 new ClassWriter(ClassWriter.COMPUTE_FRAMES)时，一切都是自动计算。不再需要调用 visitFrame，
        // 但仍然必须调用 visitMaxs（参数将被忽略并重新计算）


        ClassWriter cw=new ClassWriter(cr,ClassWriter.COMPUTE_FRAMES+ClassWriter.COMPUTE_MAXS);
        cr.accept(new ClassVisitor(ASM4, cw) {

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
//                return super.visitMethod(access, name, descriptor, signature, exceptions);

//                //已经写好的类字节码方法开始和结束插装===================start
//                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
//                return new MethodAdapterVisitor(mv, access, name, descriptor, "TestFunction");
//                //已经写好的类字节码方法开始和结束插装===================end

                MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
                if (mv != null) {
                    mv = new InspectAdapter(mv);
                }
                return mv;


            }
        },0);
        String path=PathUtils.getCurrentClassPath(TestASM.class)+ File.separator+"TestFunction4.class";
        System.err.println("类生成的位置:"+path);
        IOUtils.write(cw.toByteArray(),new FileOutputStream(path));
    }


    /**
     * 测试修改方法
     * 添加第三方方法调用
     */
    public static void testChangeMethod2() throws IOException{

        ClassReader cr = new ClassReader("com.test.javase_module.TestFunction");
        ClassWriter cw=new ClassWriter(cr,0);
        cr.accept(new ClassVisitor(ASM4, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

                MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
                if (mv != null) {
                    mv = new ThirdMethodCall(mv);
                }
                return mv;
            }
        },0);
        String path=PathUtils.getCurrentClassPath(TestASM.class)+ File.separator+"TestFunction4.class";
        System.err.println("类生成的位置:"+path);
        IOUtils.write(cw.toByteArray(),new FileOutputStream(path));
    }



    public static void main(String[] args) throws IOException {
//        testClassWriter();
//        testChangeClass();
//        testChangeMethod();
        testChangeMethod2();


    }
}