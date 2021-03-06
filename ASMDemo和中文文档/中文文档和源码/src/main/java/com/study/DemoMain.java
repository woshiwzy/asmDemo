package com.study;

import static org.graalvm.compiler.bytecode.Bytecodes.LSUB;
import static groovyjarjarasm.asm.ClassWriter.COMPUTE_FRAMES;
import static groovyjarjarasm.asm.ClassWriter.COMPUTE_MAXS;
import static groovyjarjarasm.asm.Opcodes.ACC_FINAL;
import static groovyjarjarasm.asm.Opcodes.ACC_PRIVATE;
import static groovyjarjarasm.asm.Opcodes.ACC_PROTECTED;
import static groovyjarjarasm.asm.Opcodes.ACC_PUBLIC;
import static groovyjarjarasm.asm.Opcodes.ACC_STATIC;
import static groovyjarjarasm.asm.Opcodes.ALOAD;
import static groovyjarjarasm.asm.Opcodes.ARETURN;
import static groovyjarjarasm.asm.Opcodes.ASM4;
import static groovyjarjarasm.asm.Opcodes.ASTORE;
import static groovyjarjarasm.asm.Opcodes.ATHROW;
import static groovyjarjarasm.asm.Opcodes.DUP;
import static groovyjarjarasm.asm.Opcodes.GETFIELD;
import static groovyjarjarasm.asm.Opcodes.GETSTATIC;
import static groovyjarjarasm.asm.Opcodes.IADD;
import static groovyjarjarasm.asm.Opcodes.INVOKESPECIAL;
import static groovyjarjarasm.asm.Opcodes.INVOKESTATIC;
import static groovyjarjarasm.asm.Opcodes.INVOKEVIRTUAL;
import static groovyjarjarasm.asm.Opcodes.IREM;
import static groovyjarjarasm.asm.Opcodes.IRETURN;
import static groovyjarjarasm.asm.Opcodes.LLOAD;
import static groovyjarjarasm.asm.Opcodes.LSTORE;
import static groovyjarjarasm.asm.Opcodes.NEW;
import static groovyjarjarasm.asm.Opcodes.RETURN;
import static groovyjarjarasm.asm.Opcodes.V1_8;


import com.test.javase_module.TestASM;
import com.utils.PathUtils;


import org.graalvm.compiler.bytecode.Bytecodes;
import org.gradle.internal.impldep.org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import groovyjarjarasm.asm.ClassReader;
import groovyjarjarasm.asm.ClassVisitor;
import groovyjarjarasm.asm.ClassWriter;
import groovyjarjarasm.asm.FieldVisitor;
import groovyjarjarasm.asm.Label;
import groovyjarjarasm.asm.MethodVisitor;
import groovyjarjarasm.asm.Type;
import groovyjarjarasm.asm.commons.LocalVariablesSorter;


public class DemoMain {

    /**
     * ?????????????????????
     * ????????????com.study.Human
     * ????????????????????????????????????????????????????????????????????????????????????
     * package com.study;
     *
     * import java.util.ArrayList;
     *
     * public class Human {
     *     private String name;
     *     private long age;
     *     protected int no;
     *     public static long score;
     *     public static final String real_name = "Sand???";
     *
     *     public Human() {
     *     }
     *
     *     public int greet(String var1) {
     *         System.out.println(var1);
     *         ArrayList var2 = new ArrayList();
     *         StringBuilder var3 = new StringBuilder();
     *         var3.append("Hello java asm StringBuilder");
     *         long var4 = System.nanoTime();
     *         return 10 + 11;
     *     }
     *
     *     public static void staticMethod(String var0) {
     *         System.out.println("Hello Java Asm!");
     *     }
     * }
     */
    public static void testCreateAClass()throws Exception{

        //???????????????????????????COMPUTE_FRAMES???COMPUTE_MAXS???2??????????????????asm????????????????????????
        ClassWriter cw=new ClassWriter(COMPUTE_FRAMES|COMPUTE_MAXS);
        //????????????public?????????????????????com.study.Human
        cw.visit(V1_8,ACC_PUBLIC,"com/study/Human",null,"java/lang/Object",null);

        //?????????????????????????????? public Human()
        MethodVisitor mv=cw.visitMethod(ACC_PUBLIC,"<init>","()V",null,null);
        mv.visitVarInsn(ALOAD,0);
        mv.visitMethodInsn(INVOKESPECIAL,"java/lang/Object","<init>","()V",false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0,0);//??????????????????
        mv.visitEnd();//????????????visitEnd

        //??????????????????
        //1.??????String?????????????????????:private String name;
        FieldVisitor fv= cw.visitField(ACC_PRIVATE,"name","Ljava/lang/String;",null,null);
        fv.visitEnd();//????????????end
        //2.??????Long???????????????private long age
        fv=cw.visitField(ACC_PRIVATE,"age","J",null,null);
        fv.visitEnd();

        //3.??????Int????????????:protected int no
        fv=cw.visitField(ACC_PROTECTED,"no","I",null,null);
        fv.visitEnd();

        //4.???????????????????????????public static long score
        fv=cw.visitField(ACC_PUBLIC+ACC_STATIC,"score","J",null,null);

        //5.???????????????public static final String real_name = "Sand???"
        fv=cw.visitField(ACC_PUBLIC+ACC_STATIC+ACC_FINAL,"real_name","Ljava/lang/String;",null,"Sand???");
        fv.visitEnd();

        //6.??????????????????greet
        mv=cw.visitMethod(ACC_PUBLIC,"greet","(Ljava/lang/String;)I",null,null);
        mv.visitCode();
        mv.visitIntInsn(ALOAD,0);
        mv.visitIntInsn(ALOAD,1);

        //6.1 ?????????????????? System.out.println("Hello");
        mv.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;");
//        mv.visitLdcInsn("Hello");//??????????????????
        mv.visitIntInsn(ALOAD,1);//????????????
        mv.visitMethodInsn(INVOKEVIRTUAL,"java/io/PrintStream","println","(Ljava/lang/String;)V",false);//????????????
        //6.2 ??????????????????
        LocalVariablesSorter lvs=new LocalVariablesSorter(ACC_PUBLIC,"(Ljava/lang/String;)I",mv);
        //??????ArrayList ??????
        //new ArrayList ,????????????????????????
        mv.visitTypeInsn(NEW,"java/util/ArrayList");
        mv.visitInsn(DUP);//?????????
        //?????????????????????????????????????????????????????????????????????????????????????????????????????????1???????????????????????????????????????
        mv.visitMethodInsn(INVOKESPECIAL,"java/util/ArrayList","<init>","()V",false);

        int time=lvs.newLocal(Type.getType(List.class));

        mv.visitVarInsn(ASTORE,time);
        mv.visitVarInsn(ALOAD,time);

        //??????StringBuilder??????
        mv.visitTypeInsn(NEW,"java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL,"java/lang/StringBuilder","<init>","()V",false);

        //?????????????????????lvs.newLocal???????????????Type.geteType("?????????") ????????????????????????Type.geteType("XXX.class???)?????????
        time=lvs.newLocal(Type.getType(StringBuilder.class));
        mv.visitVarInsn(ASTORE,time);
        mv.visitVarInsn(ALOAD,time);

        mv.visitLdcInsn("Hello java asm StringBuilder");
        mv.visitMethodInsn(INVOKEVIRTUAL,"java/lang/StringBuilder","append","(Ljava/lang/String;)Ljava/lang/StringBuilder;",false);

        mv.visitMethodInsn(INVOKESTATIC,"java/lang/System","nanoTime","()J",false);
        time=lvs.newLocal(Type.LONG_TYPE);
        mv.visitVarInsn(LSTORE,time);
        mv.visitLdcInsn(10);
        mv.visitLdcInsn(11);
        mv.visitInsn(IADD);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0,0);
        mv.visitEnd();

        //??????????????????
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "staticMethod", "(Ljava/lang/String;)V", null, null);
        //???????????????????????????????????????
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("Hello Java Asm!");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        //????????????????????????
        String path= PathUtils.getCurrentClassPath(DemoMain.class)+ File.separator+"Human.class";
        //????????????byte??????
        byte[] classByteData=cw.toByteArray();
        //?????????????????????class??????,???????????????????????????????????????????????????????????????
        IOUtils.write(classByteData,new FileOutputStream(path));
        System.err.println("??????????????????:"+path);
    }

    /**
     * ??????????????????
     * 1.??????TestFunction.testA??????
     * 2.???TestFunction ????????????phone???????????????????????????getPhone??????
     * 3.???TestFunction testC ???????????????protected
     * ????????????-------------------------------------------------------
     * package com.test.javase_module;
     *
     *
     * public class TestFunction {
     *
     *     private int a;
     *     public void testA(){
     *         System.out.println("I am A");
     *     }
     *
     *     public void testB(){
     *         System.err.println("===>I am B");
     *     }
     *
     *     public int testC(){
     *         return a;
     *     }
     * }
     * ???????????????--------------------------------------------------------
     * package com.test.javase_module;
     *
     * public class TestFunction {
     *     private int a;
     *
     *     //???????????????phone??????
     *     public String phone;
     *
     *     public TestFunction() {
     *     }
     *
     *     //testA????????????????????????
     *
     *     public void testB() {
     *         System.err.println("===>I am B");
     *     }
     *
     *     //testC ???????????????protected
     *     protected int testC() {
     *         return this.a;
     *     }
     *
     *     //?????????????????????getPhone??????
     *     public String getPhone() {
     *         return this.phone;
     *     }
     * }
     * @throws Exception
     */
    private static void testModifyCalss()throws Exception{
        ClassReader cr = new ClassReader("com.test.javase_module.TestFunction");
        final ClassWriter cw=new ClassWriter(cr,0);
//        cr.accept(cw, 0);//????????????????????????writer,????????????
        cr.accept(new ClassVisitor(ASM4,cw) {//???????????????classWriter???visitor??????????????????????????????????????????????????????
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                System.out.println("visit method:"+name+"====> "+descriptor);

                if("testA".equals(name)){//????????????????????????????????????
                    return null;
                }

                if("testC".equals(name)){//???testC public????????????protect
                    access=ACC_PROTECTED;
                }
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }

            @Override
            public void visitEnd() {
                //????????????????????????????????????????????????????????????visitEnd??????????????????????????????????????????????????????????????????????????????
                //??????????????????????????????????????????,??????????????????visitEnd
                FieldVisitor fv = cv.visitField(ACC_PUBLIC, "phone", "Ljava/lang/String;", null, null);
                fv.visitEnd();//????????????visitEnd

                //??????????????????
                MethodVisitor mv=cv.visitMethod(ACC_PUBLIC,"getPhone","()Ljava/lang/String;",null,null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD,"com/test/javase_module/TestFunction","phone","Ljava/lang/String;");
                mv.visitInsn(IRETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();//????????????visitEnd
                super.visitEnd();//???????????????visiEnd?????????
            }
        },0);

        //??????????????????class?????????????????????
        String path=PathUtils.getCurrentClassPath(TestASM.class)+ File.separator+"TestFunction3.class";
        System.err.println("??????????????????:"+path);
        IOUtils.write(cw.toByteArray(),new FileOutputStream(path));
    }

    /**
     * ?????????????????????
     * ??????????????????????????????????????????????????????????????????
     * ??????????????????ASM?????????AdviceAdapter??????????????????
     * ?????????????????????
     *     public void testB() {
     *         System.err.println("===>I am B");
     *     }
     *????????????????????????
     *     public void testB() {
     *         long var1 = System.currentTimeMillis();
     *         System.err.println("===>I am B");
     *         long var3 = System.currentTimeMillis();
     *         System.out.println((new StringBuilder()).append("cost:").append(var3 - var1).toString());
     *     }
     *
     */
    public static void testInspectCode() throws IOException {
        ClassReader cr = new ClassReader("com.test.javase_module.TestFunction");
        //--------------------------------------------------------------------------------
        //1.????????? new ClassWriter(0)???????????????????????????????????????????????????????????????????????? ??????????????????????????????
        //--------------------------------------------------------------------------------
        //2.????????? new ClassWriter(ClassWriter.COMPUTE_MAXS)??????????????????????????????????????????????????????????????????
        // ?????????????????? visitMaxs?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        //--------------------------------------------------------------------------------
        //3.??? new ClassWriter(ClassWriter.COMPUTE_FRAMES)??????????????????????????????????????????????????? visitFrame???
        // ????????????????????? visitMaxs???????????????????????????????????????
        //--------------------------------------------------------------------------------

        ClassWriter cw=new ClassWriter(cr,ClassWriter.COMPUTE_FRAMES+ClassWriter.COMPUTE_MAXS);
        cr.accept(new ClassVisitor(ASM4, cw) {

            @Override
            public MethodVisitor visitMethod(int access, final String name, String descriptor, String signature, String[] exceptions) {
//                //??????????????????????????????????????????????????????===================start
//                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
//                return new MethodAdapterVisitor(mv, access, name, descriptor, "TestFunction");
//                //??????????????????????????????????????????????????????===================end
                MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);

                return new MethodVisitor(ASM4,mv) {

                    @Override
                    public void visitLineNumber(int line, Label start) {
                        System.out.println("????????????????????????:"+line+"?????????????????????????????????");
                        super.visitLineNumber(line, start);
                    }
                    @Override
                    public void visitParameter(String name, int access) {
                        super.visitParameter(name, access);
                    }

                    public void visitInsn(int opcode) {
                        //?????????????????????????????????return????????????????????????
                        //????????? owner ??????????????????????????????????????????????????????????????? RETURN ????????????????????????
                        //???????????????????????? xRETURN ??? ATHROW ????????????????????????????????????????????????????????????????????????
                        //???????????????????????????????????? visitInsn ????????????????????????????????????????????????????????????????????????

                        if (!"<init>".equals(name) && (opcode >= Bytecodes.IRETURN && opcode <= Bytecodes.RETURN) || opcode == ATHROW) {
                            //?????????return??????????????????
                            mv.visitMethodInsn(INVOKESTATIC,"java/lang/System",  "currentTimeMillis", "()J",false);
                            mv.visitIntInsn(Bytecodes.LSTORE,3);

                            mv.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream");
                            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                            mv.visitInsn(Bytecodes.DUP);

                            mv.visitMethodInsn(Bytecodes.INVOKESPECIAL,"java/lang/StringBuilder","<init>","()V",false);
                            mv.visitLdcInsn("cost:");//?????????????????????????????????
                            mv.visitMethodInsn(Bytecodes.INVOKEVIRTUAL,"java/lang/StringBuilder","append","(Ljava/lang/String;)Ljava/lang/StringBuilder",false);
                            mv.visitVarInsn(LLOAD, 3);
                            mv.visitVarInsn(LLOAD,1);
                            mv.visitInsn(LSUB);
//
                            mv.visitMethodInsn(Bytecodes.INVOKEVIRTUAL,"java/lang/StringBuilder","append","(J)Ljava/lang/StringBuilder",false);
                            mv.visitMethodInsn(Bytecodes.INVOKEVIRTUAL,"java/lang/StringBuilder","toString","()Ljava/lang/String",false);
                            mv.visitMethodInsn(Bytecodes.INVOKEVIRTUAL,"java/io/PrintStream","println","(Ljava/lang/String;)V",false);
                        }
                        mv.visitInsn(opcode);
                    }

                    @Override
                    public void visitCode() {
                        super.visitCode();
                        //??????????????????????????????????????????????????????????????????????????????
                        System.out.println("????????????=========>"+name);
                        if(!"<init>".equals(name)){//????????????????????????????????????
                            mv.visitMethodInsn(INVOKESTATIC,"java/lang/System","currentTimeMillis", "()J",false);
                            mv.visitIntInsn(Bytecodes.LSTORE,1);
                        }
                    }
                };

            }
        },0);
        String path=PathUtils.getCurrentClassPath(DemoMain.class)+ File.separator+"TestFunction4.class";
        System.err.println("??????????????????:"+path);
        IOUtils.write(cw.toByteArray(),new FileOutputStream(path));

    }

    /**
     * ?????????????????????2
     * ???????????????????????????????????????????????????????????????
     * ???testInspectCode????????????????????????com.utils.Tool#useTool(long) ???????????????
     * ???????????????
     *     public void testA(){
     *         System.out.println("I am A");
     *     }
     * ??????????????????
     *   public void testA() {
     *         long var1 = System.currentTimeMillis();
     *         System.out.println("I am A");
     *         long var3 = System.currentTimeMillis();
     *         System.out.println((new StringBuilder()).append("cost:").append(var3 - var1).toString());
     *         Tool.useTool(var1);
     *     }
     *
     */
    public static void testInspectCode2()throws Exception{

        ClassReader cr = new ClassReader("com.test.javase_module.TestFunction");
        ClassWriter cw=new ClassWriter(cr,0);
        cr.accept(new ClassVisitor(ASM4, cw) {
            @Override
            public MethodVisitor visitMethod(int access, final String name, String descriptor, String signature, String[] exceptions) {

                MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodVisitor(ASM4,mv) {

                    public void visitInsn(int opcode) {
               //?????????????????????????????????return????????????????????????
               //????????? owner ??????????????????????????????????????????????????????????????? RETURN ????????????????????????
               //???????????????????????? xRETURN ??? ATHROW ????????????????????????????????????????????????????????????????????????
                //???????????????????????????????????? visitInsn ????????????????????????????????????????????????????????????????????????
                        if("<init>".equals(name)){
                            return;
                        }

                        if ((opcode >= Bytecodes.IRETURN && opcode <= Bytecodes.RETURN) || opcode == ATHROW) {
                            //?????????return??????????????????

                            mv.visitMethodInsn(INVOKESTATIC,"java/lang/System",  "currentTimeMillis", "()J",false);
                            mv.visitIntInsn(Bytecodes.LSTORE,3);

                            mv.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream");
                            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                            mv.visitInsn(Bytecodes.DUP);

                            mv.visitMethodInsn(Bytecodes.INVOKESPECIAL,"java/lang/StringBuilder","<init>","()V",false);
                            mv.visitLdcInsn("cost:");//?????????????????????????????????
                            mv.visitMethodInsn(Bytecodes.INVOKEVIRTUAL,"java/lang/StringBuilder","append","(Ljava/lang/String;)Ljava/lang/StringBuilder",false);
                            mv.visitVarInsn(LLOAD, 3);
                            mv.visitVarInsn(LLOAD,1);
                            mv.visitInsn(LSUB);
//
                            mv.visitMethodInsn(Bytecodes.INVOKEVIRTUAL,"java/lang/StringBuilder","append","(J)Ljava/lang/StringBuilder",false);
                            mv.visitMethodInsn(Bytecodes.INVOKEVIRTUAL,"java/lang/StringBuilder","toString","()Ljava/lang/String",false);
                            mv.visitMethodInsn(Bytecodes.INVOKEVIRTUAL,"java/io/PrintStream","println","(Ljava/lang/String;)V",false);

                            mv.visitVarInsn(LLOAD, 1);
                            mv.visitMethodInsn(INVOKESTATIC,"com/utils/Tool","useTool","(J)V",false);

                        }
                        mv.visitInsn(opcode);
                    }

                    @Override
                    public void visitCode() {
                        super.visitCode();
                        //??????????????????????????????????????????????????????????????????????????????
                        if(!"<init>".equals(name)){
                            mv.visitMethodInsn(INVOKESTATIC,"java/lang/System","currentTimeMillis", "()J",false);
                            mv.visitIntInsn(Bytecodes.LSTORE,1);
                        }
                    }

                };
            }
        },0);

        String path=PathUtils.getCurrentClassPath(DemoMain.class)+ File.separator+"TestFunction4.class";
        System.err.println("??????????????????:"+path);
        IOUtils.write(cw.toByteArray(),new FileOutputStream(path));
    }

    public static void main(String[] args) throws Exception {
//         testCreateAClass();//???????????????
         testModifyCalss();//??????????????????????????????????????????
//         testInspectCode();//???????????????????????????????????????????????????
//         testInspectCode2();//????????????????????????????????????????????????????????????????????????????????????
    }

}
