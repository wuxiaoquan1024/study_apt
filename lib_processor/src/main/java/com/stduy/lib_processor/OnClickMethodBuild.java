package com.stduy.lib_processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.stduy.lib_annotations.OnClickListener;

import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.swing.text.View;
import javax.tools.Diagnostic;

public class OnClickMethodBuild {

    private ExecutableElement element;

    private String methodName;

    private int[] viewIds;

    public OnClickMethodBuild(ExecutableElement element, Messager messager) {
        this.element = element;
        methodName = element.getSimpleName().toString();
        OnClickListener annotation = element.getAnnotation(OnClickListener.class);
        viewIds = annotation.ids();
        List<? extends VariableElement> parameters = element.getParameters();
        if (parameters.size() > 1) {
            throw new IllegalArgumentException("Method parameters should only one");
        }
        methodName = element.getSimpleName().toString();
        messager.printMessage(Diagnostic.Kind.NOTE, methodName);
    }

    public void setOnClickListener(MethodSpec.Builder builder) {
        buildIdsParams(builder);
        TypeSpec onClilckAC = buildAnonymousClass();
        ClassName onClickListener = ClassName.get("android.view.View", "OnClickListener");
        CodeBlock onClickParams = CodeBlock.builder()
                .add("\n")
                .add("$T listener = $L;", onClickListener, onClilckAC) //添加局部变量
                .add("\n")
                .build();
        builder.addCode(onClickParams);
        builder.beginControlFlow("for (int id : $N)", "ids")
//                .addCode("$N.findViewById($N).setOnClickListener($N);", "host", "id", "listener")
                .addCode("$N.findViewById($N).setOnClickListener($L);", "host", "id", onClilckAC) // 使用匿名内部类作为实参传递参数
                .addCode("\n")
                .endControlFlow();
    }

    public void unOnClickListener(MethodSpec.Builder builder) {
        buildIdsParams(builder);
        builder.addCode("\n");
        builder.beginControlFlow("for (int id : $N)", "ids")
                .addCode("$N.findViewById($N).setOnClickListener(null);", "host", "id") // 使用匿名内部类作为实参传递参数
                .addCode("\n")
                .endControlFlow();
    }

    /**
     * 将OnClick的注解中的ID转换成数组局部变量
     * @param builder
     */
    private void buildIdsParams(MethodSpec.Builder builder) {
        CodeBlock.Builder idsBuilder = CodeBlock.builder()
                .add("$T ids = {", int[].class);
        for (int i = 0; i < viewIds.length; i++) {
            idsBuilder.add("$L", viewIds[i]);
            if (i < viewIds.length - 1) {
                idsBuilder.add(", ");
            }
        }
        idsBuilder.add("};");
        builder.addCode(idsBuilder.build());
    }

    /**
     * 创建匿名内部类。， 可以直接使用匿名内部类初始化对象。 在code中不需要在添加new XXX()
     * @return
     */
    private TypeSpec buildAnonymousClass() {
        ClassName view = ClassName.get("android.view", "View");

        ClassName onClickListener = ClassName.get(view.canonicalName(), "OnClickListener");

        ParameterSpec vParams = ParameterSpec.builder(view, "v")
                .build();
        CodeBlock code = CodeBlock.builder()
                .add("$N.$N($N);", "host", methodName, "v")
                .build();

        MethodSpec onClick = MethodSpec.methodBuilder("onClick")
                .addAnnotation(Override.class)
                .addParameter(vParams)
                .addModifiers(Modifier.PUBLIC)
                .addCode(code)
                .build();

        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(onClickListener)
                .addMethod(onClick)
                .build();

    }
}


