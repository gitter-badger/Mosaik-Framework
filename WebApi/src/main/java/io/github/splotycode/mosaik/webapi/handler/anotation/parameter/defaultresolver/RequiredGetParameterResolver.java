package io.github.splotycode.mosaik.webapi.handler.anotation.parameter.defaultresolver;

import io.github.splotycode.mosaik.webapi.handler.anotation.handle.HandleHelper;
import io.github.splotycode.mosaik.webapi.request.Request;
import io.github.splotycode.mosaik.webapi.handler.anotation.AnnotationHandlerData;
import io.github.splotycode.mosaik.webapi.handler.anotation.handle.RequiredGet;
import io.github.splotycode.mosaik.webapi.handler.anotation.parameter.AnnotatedParameterResolver;
import io.github.splotycode.mosaik.webapi.handler.anotation.parameter.ParameterResolveException;

import java.lang.reflect.Parameter;

public class RequiredGetParameterResolver extends AnnotatedParameterResolver<RequiredGet, Object> {

    public RequiredGetParameterResolver() {
        super(RequiredGet.class);
    }

    @Override
    protected Object transformAnnotation(RequiredGet annotation, Parameter parameter, Request request, AnnotationHandlerData handler, AnnotationHandlerData.SupAnnotationHandlerData method) {
        String strValue = request.getFirstGetParameter(annotation.value());
        if (strValue == null) throw new ParameterResolveException("Could not find " + annotation.value());

        Object value = HandleHelper.transformParameter(parameter, strValue);

        if (value == null) throw new ParameterResolveException("Invalid Data Type");
        return value;
    }
}
