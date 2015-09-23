package com.zhkui.ioc.util.processor;

import javax.lang.model.element.TypeElement;

public interface IAnnotationProxy {

	public String getFullName();

	public TypeElement getElement();

	public String brewJavaCode();
}
