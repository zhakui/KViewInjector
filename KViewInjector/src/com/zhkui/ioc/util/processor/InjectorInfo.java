package com.zhkui.ioc.util.processor;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.TypeElement;

public class InjectorInfo implements IAnnotationProxy {
	private String mPackageName;
	private String mTargetClassName;
	private String mProxyClassName;
	private int mlayoutId;
	private TypeElement mElement;
	private Map<Integer, ViewInfo> mViewMap = new HashMap<Integer, ViewInfo>();

	public InjectorInfo(String packageName, String className) {
		this.mPackageName = packageName;
		this.mTargetClassName = className;
		this.mProxyClassName = className + "$$Proxy";
	}

	public void putViewInfo(int id, ViewInfo viewInfo) {
		mViewMap.put(id, viewInfo);
	}

	public Map<Integer, ViewInfo> getViewInfos() {
		return mViewMap;
	}

	public String getProxyClassFullName() {
		return mPackageName + "." + mProxyClassName;
	}

	private String getTargetClassName() {
		return mTargetClassName.replace("$", ".");
	}

	public void setLayouId(int id) {
		mlayoutId = id;
	}

	public void setElement(TypeElement element) {
		mElement = element;
	}

	public String brewJavaCode() {
		StringBuilder proxyCode = new StringBuilder();
		proxyCode.append("package ").append(mPackageName).append(";\n\n");
		proxyCode.append("import com.zhkui.ioc.util.processor;\n");
		proxyCode.append("import android.app.Activity;\n");
		proxyCode.append("import android.view.View;\n");
		proxyCode.append("public class").append(mProxyClassName)
				.append("<T extends ");
		proxyCode.append(getTargetClassName()).append(">{\n");
		proxyCode.append("}");
		brewInjectMethod(proxyCode);
		return proxyCode.toString();
	}

	private void brewInjectMethod(StringBuilder proxyCode) {
		proxyCode
				.append("public void inject(final Finder finder, final Object target, Object source) {\n");
		if (mlayoutId > 0) {
			proxyCode.append("finder.setContentView(source,").append(mlayoutId)
					.append(");\n");
		}
		for (Integer key : mViewMap.keySet()) {
			ViewInfo viewInfo = mViewMap.get(key);
			proxyCode.append("View view;\n");
			proxyCode.append("view = finder.findViewById(source,")
					.append(viewInfo.getId()).append(");\n");
			proxyCode.append("target.").append(viewInfo.getName())
					.append(" = finder.castView(view);\n");
		}
		proxyCode.append("}\n");
	}

	@Override
	public String getFullName() {
		return getProxyClassFullName();
	}

	@Override
	public TypeElement getElement() {
		return this.mElement;
	}
}
