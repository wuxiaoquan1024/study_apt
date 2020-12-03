package com.stduy.lib_processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * 存放一个Java文件相关信息
 */
public class ClazzFileBuild {

    private final Messager mMessager;
    private final Elements mElements;
    /**
     * 外部类完整路径
     */
    private String outClazzQualifiedName;

    private String packageName;

    private String simpleName;

    private ArrayList<FieldBuilder> list = new ArrayList<>();

    private ArrayList<OnClickMethodBuild> onClickList = new ArrayList<>();

    private TypeElement typeElement;

    public ClazzFileBuild(TypeElement typeElement, Elements elements, Messager messager) {
        this.typeElement = typeElement;
        this.mMessager = messager;
        this.mElements = elements;
        outClazzQualifiedName = typeElement.getQualifiedName().toString();
        packageName = elements.getPackageOf(typeElement).getQualifiedName().toString();
        simpleName = typeElement.getSimpleName().toString();
    }

    /**
     * 写入文件
     * @param filer
     */
    public void writeTo(Filer filer) {
        TypeSpec clazz = TypeSpec.classBuilder(simpleName + "$ViewBinder")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(getBinderMethod())
                .addMethod(getUnbinderMethod())
                .build();

        try {
            mMessager.printMessage(Diagnostic.Kind.NOTE, "packageName:" + packageName + "\t" + mElements.getPackageOf(typeElement).toString());

            JavaFile.builder(packageName, clazz)
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
        }
    }

    private MethodSpec getBinderMethod() {
        ParameterSpec host = ParameterSpec.builder(TypeName.get(typeElement.asType()), "host")
                .addModifiers(Modifier.FINAL)
                .build();

        MethodSpec.Builder binder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(host);
        for (FieldBuilder info : list) {
            info.addBindStatement(binder);
        }

        for (OnClickMethodBuild info : onClickList) {
            info.setOnClickListener(binder);
        }
        return binder.build();
    }

    private MethodSpec getUnbinderMethod() {
        MethodSpec.Builder binder = MethodSpec.methodBuilder("unbind")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(typeElement.asType()), "host");
        for (FieldBuilder info : list) {
            info.addUnbindStatement(binder);
        }

        for (OnClickMethodBuild info : onClickList) {
            info.unOnClickListener(binder);
        }
        return binder.build();
    }

    /**
     * 添加属性信息
     * @param info
     */
    public void addFiled(FieldBuilder info) {
        list.add(info);
    }

    /**
     * 添加点击信息
     * @param info
     */
    public void addOnClick(OnClickMethodBuild info) {
        onClickList.add(info);
    }


}
