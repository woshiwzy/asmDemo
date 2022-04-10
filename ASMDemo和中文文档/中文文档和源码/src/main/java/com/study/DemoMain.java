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
     * 测试生成一个类
     * 全路径为com.study.Human
     * 生成各种成员变量，并生成默认构造方法，成员方法，静态方法
     * package com.study;
     *
     * import java.util.ArrayList;
     *
     * public class Human {
     *     private String name;
     *     private long age;
     *     protected int no;
     *     public static long score;
     *     public static final String real_name = "Sand哥";
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

        //新建一个类生成器，COMPUTE_FRAMES，COMPUTE_MAXS这2个参数能够让asm自动更新操作数栈
        ClassWriter cw=new ClassWriter(COMPUTE_FRAMES|COMPUTE_MAXS);
        //生成一个public的类，类路径是com.study.Human
        cw.visit(V1_8,ACC_PUBLIC,"com/study/Human",null,"java/lang/Object",null);

        //生成默认的构造方法： public Human()
        MethodVisitor mv=cw.visitMethod(ACC_PUBLIC,"<init>","()V",null,null);
        mv.visitVarInsn(ALOAD,0);
        mv.visitMethodInsn(INVOKESPECIAL,"java/lang/Object","<init>","()V",false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0,0);//更新操作数栈
        mv.visitEnd();//一定要有visitEnd

        //生成成员变量
        //1.生成String类型的成员变量:private String name;
        FieldVisitor fv= cw.visitField(ACC_PRIVATE,"name","Ljava/lang/String;",null,null);
        fv.visitEnd();//不要忘记end
        //2.生成Long类型成员：private long age
        fv=cw.visitField(ACC_PRIVATE,"age","J",null,null);
        fv.visitEnd();

        //3.生成Int类型成员:protected int no
        fv=cw.visitField(ACC_PROTECTED,"no","I",null,null);
        fv.visitEnd();

        //4.生成静态成员变量：public static long score
        fv=cw.visitField(ACC_PUBLIC+ACC_STATIC,"score","J",null,null);

        //5.生成常量：public static final String real_name = "Sand哥"
        fv=cw.visitField(ACC_PUBLIC+ACC_STATIC+ACC_FINAL,"real_name","Ljava/lang/String;",null,"Sand哥");
        fv.visitEnd();

        //6.生成成员方法greet
        mv=cw.visitMethod(ACC_PUBLIC,"greet","(Ljava/lang/String;)I",null,null);
        mv.visitCode();
        mv.visitIntInsn(ALOAD,0);
        mv.visitIntInsn(ALOAD,1);

        //6.1 调用静态方法 System.out.println("Hello");
        mv.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;");
//        mv.visitLdcInsn("Hello");//加载字符常量
        mv.visitIntInsn(ALOAD,1);//加载形参
        mv.visitMethodInsn(INVOKEVIRTUAL,"java/io/PrintStream","println","(Ljava/lang/String;)V",false);//打印形参
        //6.2 创建局部变量
        LocalVariablesSorter lvs=new LocalVariablesSorter(ACC_PUBLIC,"(Ljava/lang/String;)I",mv);
        //创建ArrayList 对象
        //new ArrayList ,分配内存不初始化
        mv.visitTypeInsn(NEW,"java/util/ArrayList");
        mv.visitInsn(DUP);//压入栈
        //弹出一个对象所在的地址，进行初始化操作，构造函数默认为空，此时栈大小为1（到目前只有一个局部变量）
        mv.visitMethodInsn(INVOKESPECIAL,"java/util/ArrayList","<init>","()V",false);

        int time=lvs.newLocal(Type.getType(List.class));

        mv.visitVarInsn(ASTORE,time);
        mv.visitVarInsn(ALOAD,time);

        //创建StringBuilder对象
        mv.visitTypeInsn(NEW,"java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL,"java/lang/StringBuilder","<init>","()V",false);

        //这里需要注意在lvs.newLocal的时候使用Type.geteType("类路径") 会报错，需要改成Type.geteType("XXX.class“)的方式
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

        //生成静态方法
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "staticMethod", "(Ljava/lang/String;)V", null, null);
        //生成静态方法中的字节码指令
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("Hello Java Asm!");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        //设置必要的类路径
        String path= PathUtils.getCurrentClassPath(DemoMain.class)+ File.separator+"Human.class";
        //获取类的byte数组
        byte[] classByteData=cw.toByteArray();
        //把类数据写入到class文件,这样你就可以把这个类文件打包供其他的人使用
        IOUtils.write(classByteData,new FileOutputStream(path));
        System.err.println("类生成的位置:"+path);
    }

    /**
     * 测试给修改类
     * 1.删除TestFunction.testA方法
     * 2.为TestFunction 增加一个phone字段，并且增加一个getPhone方法
     * 3.将TestFunction testC 方法修改成protected
     * 原来的类-------------------------------------------------------
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
     * 修改后的类--------------------------------------------------------
     * package com.test.javase_module;
     *
     * public class TestFunction {
     *     private int a;
     *
     *     //增加了一个phone字段
     *     public String phone;
     *
     *     public TestFunction() {
     *     }
     *
     *     //testA方法已经被删除了
     *
     *     public void testB() {
     *         System.err.println("===>I am B");
     *     }
     *
     *     //testC 方法变成了protected
     *     protected int testC() {
     *         return this.a;
     *     }
     *
     *     //并且增加了一个getPhone方法
     *     public String getPhone() {
     *         return this.phone;
     *     }
     * }
     * @throws Exception
     */
    private static void testModifyCalss()throws Exception{
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

                if("testC".equals(name)){//将testC public方法变成protect
                    access=ACC_PROTECTED;
                }
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }

            @Override
            public void visitEnd() {
                //特别注意的是：要为类增加属性和方法，放到visitEnd中，避免破坏之前已经排列好的类结构，在结尾添加新结构
                //增加一个字段（注意不能重复）,注意最后都要visitEnd
                FieldVisitor fv = cv.visitField(ACC_PUBLIC, "phone", "Ljava/lang/String;", null, null);
                fv.visitEnd();//不能缺少visitEnd

                //增加一个方法
                MethodVisitor mv=cv.visitMethod(ACC_PUBLIC,"getPhone","()Ljava/lang/String;",null,null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD,"com/test/javase_module/TestFunction","phone","Ljava/lang/String;");
                mv.visitInsn(IRETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();//不能缺少visitEnd
                super.visitEnd();//注意原本的visiEnd不能少
            }
        },0);

        //指定新生成的class路径的生成位置
        String path=PathUtils.getCurrentClassPath(TestASM.class)+ File.separator+"TestFunction3.class";
        System.err.println("类生成的位置:"+path);
        IOUtils.write(cw.toByteArray(),new FileOutputStream(path));
    }

    /**
     * 测试字节码插装
     * 在一个方法的开始处和方法结束处增加自己的代码
     * 当然还可以用ASM自带的AdviceAdapter来实现更简单
     * 原来的方法如下
     *     public void testB() {
     *         System.err.println("===>I am B");
     *     }
     *插装后反编译如下
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
        //1.在使用 new ClassWriter(0)时，不会自动计算任何东西。必须自行计算帧、局部变 量与操作数栈的大小。
        //--------------------------------------------------------------------------------
        //2.在使用 new ClassWriter(ClassWriter.COMPUTE_MAXS)时，将为你计算局部变量与操作数栈部分的大小。
        // 还是必须调用 visitMaxs，但可以使用任何参数：它们将被忽略并重新计算。使用这一选项时，仍然必须自行计算这些帧。
        //--------------------------------------------------------------------------------
        //3.在 new ClassWriter(ClassWriter.COMPUTE_FRAMES)时，一切都是自动计算。不再需要调用 visitFrame，
        // 但仍然必须调用 visitMaxs（参数将被忽略并重新计算）
        //--------------------------------------------------------------------------------

        ClassWriter cw=new ClassWriter(cr,ClassWriter.COMPUTE_FRAMES+ClassWriter.COMPUTE_MAXS);
        cr.accept(new ClassVisitor(ASM4, cw) {

            @Override
            public MethodVisitor visitMethod(int access, final String name, String descriptor, String signature, String[] exceptions) {
//                //已经写好的类字节码方法开始和结束插装===================start
//                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
//                return new MethodAdapterVisitor(mv, access, name, descriptor, "TestFunction");
//                //已经写好的类字节码方法开始和结束插装===================end
                MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);

                return new MethodVisitor(ASM4,mv) {

                    @Override
                    public void visitLineNumber(int line, Label start) {
                        System.out.println("经过这个测试行数:"+line+"可以对应到源代码的行数");
                        super.visitLineNumber(line, start);
                    }
                    @Override
                    public void visitParameter(String name, int access) {
                        super.visitParameter(name, access);
                    }

                    public void visitInsn(int opcode) {
                        //这里是访问语句结束，在return结束之前添加语句
                        //其中的 owner 必须被设定为所转换类的名字。现在必须在任意 RETURN 之前添加其他四条
                        //指令，还要在任何 xRETURN 或 ATHROW 之前添加，它们都是终止该方法执行过程的指令。这些
                        //指令没有任何参数，因此在 visitInsn 方法中访问。于是，可以重写这一方法，以增加指令：

                        if (!"<init>".equals(name) && (opcode >= Bytecodes.IRETURN && opcode <= Bytecodes.RETURN) || opcode == ATHROW) {
                            //在方法return之前添加代码
                            mv.visitMethodInsn(INVOKESTATIC,"java/lang/System",  "currentTimeMillis", "()J",false);
                            mv.visitIntInsn(Bytecodes.LSTORE,3);

                            mv.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream");
                            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                            mv.visitInsn(Bytecodes.DUP);

                            mv.visitMethodInsn(Bytecodes.INVOKESPECIAL,"java/lang/StringBuilder","<init>","()V",false);
                            mv.visitLdcInsn("cost:");//就是传入一个字符串常量
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
                        //方法开始（可以在此处添加代码，在原来的方法之前执行）
                        System.out.println("方法名字=========>"+name);
                        if(!"<init>".equals(name)){//不要在构造方法中添加代码
                            mv.visitMethodInsn(INVOKESTATIC,"java/lang/System","currentTimeMillis", "()J",false);
                            mv.visitIntInsn(Bytecodes.LSTORE,1);
                        }
                    }
                };

            }
        },0);
        String path=PathUtils.getCurrentClassPath(DemoMain.class)+ File.separator+"TestFunction4.class";
        System.err.println("类生成的位置:"+path);
        IOUtils.write(cw.toByteArray(),new FileOutputStream(path));

    }

    /**
     * 测试字节码插装2
     * 在原有的方法中插入字节码指令中插入方法调用
     * 在testInspectCode的方法基础上添加com.utils.Tool#useTool(long) 的方法调用
     * 原来的方法
     *     public void testA(){
     *         System.out.println("I am A");
     *     }
     * 插入字节码后
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
               //这里是访问语句结束，在return结束之前添加语句
               //其中的 owner 必须被设定为所转换类的名字。现在必须在任意 RETURN 之前添加其他四条
               //指令，还要在任何 xRETURN 或 ATHROW 之前添加，它们都是终止该方法执行过程的指令。这些
                //指令没有任何参数，因此在 visitInsn 方法中访问。于是，可以重写这一方法，以增加指令：
                        if("<init>".equals(name)){
                            return;
                        }

                        if ((opcode >= Bytecodes.IRETURN && opcode <= Bytecodes.RETURN) || opcode == ATHROW) {
                            //在方法return之前添加代码

                            mv.visitMethodInsn(INVOKESTATIC,"java/lang/System",  "currentTimeMillis", "()J",false);
                            mv.visitIntInsn(Bytecodes.LSTORE,3);

                            mv.visitFieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream");
                            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                            mv.visitInsn(Bytecodes.DUP);

                            mv.visitMethodInsn(Bytecodes.INVOKESPECIAL,"java/lang/StringBuilder","<init>","()V",false);
                            mv.visitLdcInsn("cost:");//就是传入一个字符串常量
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
                        //方法开始（可以在此处添加代码，在原来的方法之前执行）
                        if(!"<init>".equals(name)){
                            mv.visitMethodInsn(INVOKESTATIC,"java/lang/System","currentTimeMillis", "()J",false);
                            mv.visitIntInsn(Bytecodes.LSTORE,1);
                        }
                    }

                };
            }
        },0);

        String path=PathUtils.getCurrentClassPath(DemoMain.class)+ File.separator+"TestFunction4.class";
        System.err.println("类生成的位置:"+path);
        IOUtils.write(cw.toByteArray(),new FileOutputStream(path));
    }

    public static void main(String[] args) throws Exception {
//         testCreateAClass();//生成一个类
         testModifyCalss();//删除类方法，增加属性，和方法
//         testInspectCode();//在已经存在的方法前后插入自定义代码
//         testInspectCode2();//在加入第三方方法调用，这样负载的操作转变为方法调用的代码
    }

}
