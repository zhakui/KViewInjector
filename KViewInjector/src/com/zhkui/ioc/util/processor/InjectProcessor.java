package com.zhkui.ioc.util.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

import com.zhkui.ioc.util.annotation.InjectView;

@SupportedAnnotationTypes("com.zhkui.util.ioc.InjectView")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class InjectProcessor extends AbstractProcessor {

	private TypeElement anthorElement;
	private CreateProxy createProxy;
	private Map<String, IAnnotationProxy> mProxyMap = new HashMap<String, IAnnotationProxy>();

	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		Elements elementUtiles = processingEnv.getElementUtils();
		anthorElement = elementUtiles
				.getTypeElement("com.zhkui.util.ioc.InjectView");
		createProxy = new CreateProxy(processingEnv);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		Set<? extends Element> elements = roundEnv
				.getElementsAnnotatedWith(anthorElement);
		for (Element element : elements) {
			processInjec(element);
		}
		return true;
	}

	public void processInjec(Element element) {
		if (element.getKind() == ElementKind.CLASS) {
			addProxyInfo((TypeElement) element);
		} else if (element.getKind() == ElementKind.FIELD) {
			addViewInfo((VariableElement) element);
		}
	}

	private void addProxyInfo(TypeElement element) {
		TypeElement classElement = element;
		PackageElement packageElement = (PackageElement) element
				.getEnclosingElement();
		String qfClassName = classElement.getQualifiedName().toString();
		String className = classElement.getSimpleName().toString();
		String packageName = packageElement.getQualifiedName().toString();

		int layoutId = classElement.getAnnotation(InjectView.class).value();

		InjectorInfo injectorInfo = (InjectorInfo) (mProxyMap.get(qfClassName));
		if (injectorInfo != null) {
			injectorInfo.setLayouId(layoutId);
		} else {
			injectorInfo = new InjectorInfo(packageName, className);
			injectorInfo.setElement(classElement);
			injectorInfo.setLayouId(layoutId);
			mProxyMap.put(qfClassName, injectorInfo);
		}
		for (String key : mProxyMap.keySet()) {
			IAnnotationProxy proxyInfo = mProxyMap.get(key);
			createProxy.execute(proxyInfo);
		}
	}

	private void addViewInfo(VariableElement element) {
		VariableElement varElement = element;

		TypeElement typeElement = (TypeElement) varElement
				.getEnclosingElement();
		PackageElement packageElement = (PackageElement) element
				.getEnclosingElement();

		String qfClassname = typeElement.getQualifiedName().toString();
		String packageName = packageElement.getQualifiedName().toString();
		String fieldName = varElement.getSimpleName().toString();
		int id = varElement.getAnnotation(InjectView.class).value();
		String fieldType = varElement.asType().toString();
		String className = getClassName(typeElement, packageName);

		InjectorInfo injectorInfo = (InjectorInfo) (mProxyMap.get(qfClassname));
		if (injectorInfo == null) {
			injectorInfo = new InjectorInfo(packageName, className);
			mProxyMap.put(qfClassname, injectorInfo);
			injectorInfo.setElement(typeElement);
		}
		injectorInfo.putViewInfo(id, new ViewInfo(id, fieldName, fieldType));
	}

	private String getClassName(TypeElement type, String packageName) {
		int packageLen = packageName.length() + 1;
		return type.getQualifiedName().toString().substring(packageLen)
				.replace('.', '$');
	}
}
