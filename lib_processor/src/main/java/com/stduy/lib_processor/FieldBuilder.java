package com.stduy.lib_processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.stduy.lib_annotations.BindView;

import java.lang.reflect.Type;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class FieldBuilder {

    /**
     * 属性名称
     */
    private String name;

    /**
     * 注解的值
     */
    private int id;

    /**
     * 获得属性的Element类型
     */
    private VariableElement element;

    /**
     * 获得属性的类型
     */
    private TypeName fieldType;

    public FieldBuilder(VariableElement element) {
        this.element = element;
        name = element.getSimpleName().toString();
        BindView an = element.getAnnotation(BindView.class);
        id = an.value();
        fieldType = ClassName.get(element.asType());
    }

    public String getName() {
        return name;
    }

    /**
     * 获得BindView 注解绑定的值
     * @return
     */
    public int getId() {
        return id;
    }

    public VariableElement getElement() {
        return element;
    }

    /**
     * 获取外部元素的完整路径
     * @return
     */
    public String getEnclosedQualifiedName() {
        TypeElement e = (TypeElement) element.getEnclosingElement();
        return e.getQualifiedName().toString();
    }

    public String getEnclosedQSimpleName() {
        TypeElement e = (TypeElement) element.getEnclosingElement();
        return e.getSimpleName().toString();
    }

    /**
     * 添加绑定语句
     * @param builder
     */
    public void addBindStatement(MethodSpec.Builder builder) {
        builder.addStatement("host.$N = ($T) host.findViewById($L)", getName(), fieldType, id);
    }

    /**
     * 添加解绑语句
     * @param builder
     */
    public void addUnbindStatement(MethodSpec.Builder builder) {
        builder.addStatement("host.$N = null", getName());
    }

}
