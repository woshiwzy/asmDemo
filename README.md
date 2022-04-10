## 前言

&nbsp;
**ASM** 是一款读写Java字节码的工具，可以达到跳过源码编写，编译，直接以字节码的形式创建类，修改已经存在类(或者jar中的class)的属性，方法等。 通常用来开发一些Java开发的辅助框架,其做法是在你编写的Java代码中注入一些特定代码（俗称字节码插装）达到特定目的，以Android开发为例最常用的方法通过字节码插装实现热修复，事件监听，埋点，开源框架等非常规操作,当然在Android开发中通常辅以Gradle插件一起使用，这个改天在写。


## 背景

早就听说过ASM和字节码插桩技术，但是工作中很少直接使用,因为近期有这个学习需求，特做此笔记，供有需要的同学依葫芦画瓢，也作为自己的参考笔记以备后用。

据我实操过程中发现至少有以下三个地方可以获得ASM API的地方 

1.ASM官网:https://asm.ow2.io/ 这里有从4.0到最新的9.3 所有版本，你可以下载到响应的jar包，还有使用手册https://asm.ow2.io/asm4-guide.pdf, 然后依赖到Java工程中即可,其API没有太大的差异。

2.jdk 自带的asm api(我的是JDK11)

3.gradle 自带的api(因为我是Android开发,我用了这种方法：使用的时候只需要添加依赖就行
```

dependencies {
    implementation gradleApi()

//    testImplementation 'org.ow2.asm:asm:7.1'
//    testImplementation 'org.ow2.asm:asm-commons:7.1'
}
```
为了更好的参考字节码建议在Android Studio安装 ASM 相关插件，如图所示，安装一下3种中的一种即可，建议安装第三种（**最多只能安装1种，否则你的Android studio 下次就无法重启了**）


![ASM_plug.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/4488b4613d8c4559b7e12fbdc70dd571~tplv-k3u1fbpfcp-watermark.image?)






## 四种ASM的常用使用场景,供有需要的同学做参考

### 1.生成一个完整的类(包含几种基本属性和方法)

```java

package com.study;

import java.util.ArrayList;

public class Human {
    private String name;
    private long age;
    protected int no;
    public static long score;
    public static final String real_name = "Sand哥";

    public Human() {
    }

    public int greet(String var1) {
        System.out.println(var1);
        ArrayList var2 = new ArrayList();
        StringBuilder var3 = new StringBuilder();
        var3.append("Hello java asm StringBuilder");
        long var4 = System.nanoTime();
        return 10 + 11;
    }

    public static void staticMethod(String var0) {
        System.out.println("Hello Java Asm!");
    }
}
```

用Java ASM 该如何生成(必要的地方有详细注释)


```java

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

```

##### **注意点**

##### 1.ClassWriter(COMPUTE_FRAMES|COMPUTE_MAXS),这个ClassWriter的flag参数，建议设置这两种之和,按照官网所说，性能虽然差点，但是可以自动更新操作数栈和方法调用帧计算。

##### 2.任何时候千万别忘记visitEnd。


##### 3.lvc 创建临时变量的时候lvs.newLocal(Type.getType("com/xx/YYY"));方法调用可能出错,改成如下方式 int time=lvs.newLocal(Type.getType(YYY.class));能正常运行，具体原因还没有来的研究。

##### 4.生成类小结
通过这样的方法生成的class文件可以打包后供他人使用了,面向Java对象编程变成面向字节码编程,当然这种用法还有可读性更好的Javapoet 方法，这里不做讨论

### 2.修改已经存在的类（添加属性，添加方法，修改方法等）
假设这个类代码如下(注意我们修改的事class文件)，asm代码将做3个地方修改

1.增加了一个phone字段
2.删除testA方法
3.将testC方法改成protected
4.新增一个getPhone方法

```
package com.test.javase_module;


public class TestFunction {

    private int a;
    public void testA(){
        System.out.println("I am A");
    }

    public void testB(){
        System.err.println("===>I am B");
    }

    public int testC(){
        return a;
    }
}
```
修改后的如下(修改后的class文件可以替换原来的文件重新打入jar包)

```
package com.test.javase_module;

public class TestFunction {
    private int a;
    //1.增加phone字段
    public String phone;

    public TestFunction() {
    }

    //2.已经删除了方法testA

    public void testB() {
        System.err.println("===>I am B");
    }

    //3.testC方法已经变成了protected
    protected int testC() {
        return this.a;
    }

    //4.增加了getPhone方法
    public String getPhone() {
        return this.phone;
    }
}
```

ASM代码如下
```
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

        //指定新生成的class路径的生成位置,这个路径你可以随便指定
        String path=PathUtils.getCurrentClassPath(TestASM.class)+ File.separator+"TestFunction3.class";
        System.err.println("类生成的位置:"+path);
        IOUtils.write(cw.toByteArray(),new FileOutputStream(path));
    }
```

### 3.实现方法注入(用途广泛)
```
* 在一个方法的开始处和方法结束处增加自己的代码
* 当然还可以用ASM自带的AdviceAdapter来实现更简单
* 原来的方法如下
     public void testB() {
         System.err.println("===>I am B");
     }
*插装后反编译如下
     public void testB() {
         long var1 = System.currentTimeMillis();
         System.err.println("===>I am B");
         long var3 = System.currentTimeMillis();
         System.out.println((new StringBuilder()).append("cost:").append(var3 - var1).toString());
     }
```

ASM 代码(**注意避开构造方法**)

```
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
```
### 4.注入方法调用

在方法的末尾插入了Tool.useTool()方法调用，这个方法具体内容可以自己随便写
```

* 在原有的方法中插入字节码指令中插入方法调用
* 在testInspectCode的方法基础上添加com.utils.Tool#useTool(long) 的方法调用
* 原来的方法
    public void testA(){
         System.out.println("I am A");
     }
* 插入字节码后反编译如下
   public void testA() {
         long var1 = System.currentTimeMillis();
         System.out.println("I am A");
         long var3 = System.currentTimeMillis();
         System.out.println((new StringBuilder()).append("cost:").append(var3 - var1).toString());
         Tool.useTool(var1);//在这里调用了第三方的方法,大批量的注入改用方法调用注入，可以节省注入的字节码量
     }
```

ASM代码

```
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
```

### 5.小结
ASM 功能其实很强大，官方文档中有更为丰富的介绍

github:https://github.com/woshiwzy/asmDemo
