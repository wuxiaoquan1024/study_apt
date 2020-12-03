package com.stduy.lib_processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.stduy.lib_annotations.BindView;
import com.stduy.lib_annotations.OnClickListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

/**
 * 添加注解器信息：
 *  1.在main 目录下新建resources/META-INF/services/javax.annotation.processing.Processor文件
 *  2.将需要添加注解器的完整类名添加到文件中。每一行代表一个注解器文件
 *
 * 使用AutoService 注解无用。 必须手动添加注册注解器信息
 */
//@AutoService(Processor.class)
public class BindViewProceesor extends AbstractProcessor {

    private Filer mFiler;

    private Messager messager;

    private HashMap<String, ClazzFileBuild> fileMap = new HashMap<>();

    private Map<String, String> mCompilerOptions;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        /*
         * 获得 android {defaultConfig {javaCompileOption{ annotationProcessorOptions {argments=["key1":"value1"]} }}} 下gradle 配置的编译参数
         */
        mCompilerOptions = processingEnvironment.getOptions();
        debug("compile option size:" + mCompilerOptions.size());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> set = new HashSet<>();
        set.add(BindView.class.getCanonicalName());
        set.add(OnClickListener.class.getCanonicalName());
        return set;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        processBindViewAnnotaiton(roundEnvironment);
        processOnClickAnnotaiton(roundEnvironment);
        for (ClazzFileBuild file : fileMap.values()) {
            file.writeTo(mFiler);
        }
        return true;
    }


    @Override
    public Set<String> getSupportedOptions() {
        HashSet<String> set = new HashSet<>();
        set.add("moduleName");
        return set;
    }

    private void processOnClickAnnotaiton(RoundEnvironment roundEnvironment) {
        Set<? extends Element> set = roundEnvironment.getElementsAnnotatedWith(OnClickListener.class);
        debug("process---------method----------" + set.size());
        for (Element e : set) {
            ElementKind kind = e.getKind();
            if (kind != ElementKind.METHOD) {
                continue;
            }

            Set<Modifier> modifiers = e.getModifiers();
            for (Modifier modifier : modifiers) {
                if (modifier == Modifier.PRIVATE) {
                    throw new IllegalArgumentException(" Modifier is private");
                }
            }

            ExecutableElement element = (ExecutableElement) e;
            getFile(element).addOnClick(new OnClickMethodBuild(element, messager));
        }
    }

    private void processBindViewAnnotaiton(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        debug("process-------------------" + elements.size());
        for (Element element : elements) {
            //获得修饰的元素类型
            ElementKind kind = element.getKind();
            if (kind != ElementKind.FIELD) {
                continue;
            }

            //将类型转换成 字段，方法对应类型
            VariableElement ve = (VariableElement) element;

            //获得元素上面的注解
            BindView bv = ve.getAnnotation(BindView.class);

            //获得名称
            String fieldName = ve.getSimpleName().toString();

            // 获取包名
            PackageElement packageOf = processingEnv.getElementUtils().getPackageOf(ve);

            //获得外围的元素。 比如根据全局变量获得类名
            TypeElement enclosingElement = (TypeElement) ve.getEnclosingElement();

            TypeName typeName = ClassName.get(ve.asType());

            String packageName = processingEnv.getElementUtils().getPackageOf(ve).getQualifiedName().toString();
            debug("--------" + ve.getSimpleName() + "\t" + ve.getConstantValue() + "\t" + bv.value() + "\t" + "" + enclosingElement.getSimpleName());
            debug("----aaaaaa----" + packageName + "\t" + processingEnv.getElementUtils().getDocComment(ve) + "\t" + "Type:" + typeName);

            getFile(ve).addFiled(new FieldBuilder(ve));
        }
    }

    private ClazzFileBuild getFile(Element element) {
        TypeElement e = (TypeElement) element.getEnclosingElement();
        String qualifiedName = e.getQualifiedName().toString();
        ClazzFileBuild file = fileMap.get(qualifiedName);
        if (file == null) {
            file = new ClazzFileBuild(e, processingEnv.getElementUtils(), messager);
            fileMap.put(qualifiedName, file);
        }
        return file;
    }

    private void debug(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

    private void error(String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg);
    }




}