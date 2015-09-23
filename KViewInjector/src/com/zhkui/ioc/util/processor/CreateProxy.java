package com.zhkui.ioc.util.processor;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

public class CreateProxy {

	private ProcessingEnvironment processingEnv;

	public CreateProxy(ProcessingEnvironment pEnv) {
		processingEnv = pEnv;
	}

	public void execute(IAnnotationProxy iap) {
		JavaFileObject jfo;
		try {
			jfo = processingEnv.getFiler().createSourceFile(iap.getFullName(),
					iap.getElement());
			Writer writer = jfo.openWriter();
			writer.write(iap.brewJavaCode());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			error(iap.getElement(), "Unable to write injector for type %s: %s",
					iap.getElement(), e.getMessage());
		}
	}

	private void error(Element element, String message, Object... args) {
		if (args.length > 0) {
			message = String.format(message, args);
		}
		processingEnv.getMessager().printMessage(Kind.ERROR, message, element);
	}

}
