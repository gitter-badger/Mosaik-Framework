package io.github.splotycode.mosaik.webapi.handler.anotation;

import io.github.splotycode.mosaik.util.Pair;
import io.github.splotycode.mosaik.webapi.handler.HttpHandler;
import io.github.splotycode.mosaik.webapi.handler.anotation.parameter.ParameterResolveException;
import io.github.splotycode.mosaik.webapi.handler.anotation.parameter.ParameterResolver;
import io.github.splotycode.mosaik.webapi.request.HandleRequestException;
import io.github.splotycode.mosaik.webapi.request.Request;
import io.github.splotycode.mosaik.webapi.response.content.ResponseContent;
import io.github.splotycode.mosaik.webapi.server.AbstractWebServer;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AnnotationHandler implements HttpHandler {

    @Getter private Object handlerObj;
    private AnnotationHandlerData global;
    private List<AnnotationHandlerData.SupAnnotationHandlerData> subs = new ArrayList<>();

    public AnnotationHandler(Object handlerObj, AbstractWebServer server) {
        this.handlerObj = handlerObj;
        global = new AnnotationHandlerData(handlerObj.getClass().getAnnotations());

        for (Method method : handlerObj.getClass().getMethods()) {
            for (Class<Annotation> annotation : AnnotationHandlerFinder.getHandlerAnnotation()) {
                if (method.isAnnotationPresent(annotation)) {
                    subs.add(
                            new AnnotationHandlerData.SupAnnotationHandlerData(
                                method.getDeclaredAnnotations(), method, server
                            )
                    );
                    break;
                }
            }
        }
    }

    @Override
    public boolean valid(Request request) {
        return global.valid(request) && subs.stream().anyMatch(sub -> sub.valid(request));
    }

    @Override
    public boolean handle(Request request) throws HandleRequestException {
        if (global.getLoadingError() != null) {
            throw new HandleRequestException("Trying to work with crashed Handler: " + handlerObj.getClass().getSimpleName(), global.getLoadingError());
        }
        global.applyCashingConfiguration(request.getResponse());

        for (AnnotationHandlerData.SupAnnotationHandlerData sup : subs.stream().filter(sub -> sub.valid(request)).sorted(Comparator.comparingInt(AnnotationHandlerData::getPriority)).collect(Collectors.toList())) {
            if (sup.getLoadingError() != null) {
                throw new HandleRequestException("Count not use Handler Method: " + sup.getDisplayName() + " because it fails loading on startup", sup.getLoadingError());
            }
            Object[] objects = new Object[sup.getParameters().size()];
            try {
                int i = 0;
                for (Pair<ParameterResolver, Parameter> pair : sup.getParameters()) {
                    try {
                        objects[i] = pair.getOne().transform(pair.getTwo(), request, global, sup);
                    } catch (ParameterResolveException ex) {
                        throw new HandleRequestException("Failed to transform parameter", ex);
                    }
                    i++;
                }
            } catch (Throwable ex) {
                throw new HandleRequestException("Could not prepare parameters for Method: " + sup.getTargetMethod().getName(), ex);
            }
            sup.applyCashingConfiguration(request.getResponse());
            try {
                Object result = sup.getTargetMethod().invoke(handlerObj, objects);
                if (sup.isReturnContext()) {
                    request.getResponse().setContent((ResponseContent) result);
                } else if (result != null){
                    boolean cancel = (boolean) result;
                    if (cancel) return true;
                }
            } catch (Throwable ex) {
                throw new HandleRequestException("Could not invoke Method: " + sup.getTargetMethod().getName(), ex);
            }
        }
        return false;
    }

    @Override
    public int priority() {
        return global.getPriority();
    }
}
