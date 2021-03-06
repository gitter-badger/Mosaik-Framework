package io.github.splotycode.mosaik.webapi.handler.anotation.parameter;

import io.github.splotycode.mosaik.webapi.request.Request;
import io.github.splotycode.mosaik.webapi.handler.anotation.AnnotationHandlerData;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

public abstract class AnnotatedParameterResolver<A extends Annotation, R> implements ParameterResolver<R> {

    protected Class<? extends A> annotation;

    public AnnotatedParameterResolver(Class<? extends A> annotation) {
        this.annotation = annotation;
    }

    @Override
    public boolean transformable(Parameter parameter) {
        return parameter.isAnnotationPresent(annotation);
    }

    @Override
    public final R transform(Parameter parameter, Request request, AnnotationHandlerData handler, AnnotationHandlerData.SupAnnotationHandlerData method) {
        return transformAnnotation(parameter.getAnnotation(annotation), parameter, request, handler, method);
    }

    protected abstract R transformAnnotation(A annotation, Parameter parameter, Request request, AnnotationHandlerData handler, AnnotationHandlerData.SupAnnotationHandlerData method);

}
