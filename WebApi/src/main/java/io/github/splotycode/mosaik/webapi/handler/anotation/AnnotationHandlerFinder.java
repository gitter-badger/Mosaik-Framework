package io.github.splotycode.mosaik.webapi.handler.anotation;

import io.github.splotycode.mosaik.util.StringUtil;
import io.github.splotycode.mosaik.util.condition.ClassConditions;
import io.github.splotycode.mosaik.util.condition.Conditions;
import io.github.splotycode.mosaik.util.logger.Logger;
import io.github.splotycode.mosaik.util.reflection.ClassCollector;
import io.github.splotycode.mosaik.util.reflection.classregister.ListClassRegister;
import io.github.splotycode.mosaik.webapi.config.WebConfig;
import io.github.splotycode.mosaik.webapi.handler.HandlerFinder;
import io.github.splotycode.mosaik.webapi.handler.HttpHandler;
import io.github.splotycode.mosaik.webapi.handler.anotation.check.*;
import io.github.splotycode.mosaik.webapi.server.AbstractWebServer;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.util.*;

public class AnnotationHandlerFinder extends ListClassRegister<Object> implements HandlerFinder {

    private static Logger logger = Logger.getInstance(AnnotationHandlerFinder.class);

    private ClassCollector classCollector = ClassCollector.newInstance()
                                            .setNoDisableds(true)
                                            .setOnlyClasses(true)
                                            .addCostom(Conditions.or(
                                                    item -> Arrays.stream(handlerAnnotation).anyMatch(item::isAnnotationPresent),
                                                    ClassConditions.anyMethod(method -> Arrays.stream(handlerAnnotation).anyMatch(method::isAnnotationPresent))
                                            ));


    @Getter private static Class<Annotation>[] handlerAnnotation = new Class[]{
            Handler.class, AddTransformer.class,
            First.class, GetMustBe.class,
            Last.class, Mapping.class,
            NeedGetMethod.class, NeedGetParameter.class,
            NeedMethod.class, NeedPostMethod.class,
            NeedPostParameter.class, PostMustBe.class,
            Priority.class
    };
    @Getter private AbstractWebServer webServer;

    public AnnotationHandlerFinder(AbstractWebServer webServer) {
        super(new ArrayList<>());
        this.webServer = webServer;
    }

    @Override
    public Collection<? extends HttpHandler> search() {
        List<AnnotationHandler> handlers = new ArrayList<>();
        if (webServer.getConfig().getData(WebConfig.SEARCH_ANNOTATION_HANDLERS)) {
            for (Object obj : classCollector.collectAllInstances()) {
                add(obj, handlers);
            }
        }
        for (Object obj : getAll()) {
            add(obj, handlers);
        }
        logger.info("Found Annotation Handlers (" + handlers.size() + "/" + classCollector.totalResults() + "): " + StringUtil.join(handlers, handler -> handler.getHandlerObj().getClass().getSimpleName(), ", "));
        return handlers;
    }

    private void add(Object obj, Collection<AnnotationHandler> handlers) {
        AnnotationHandler handler = new AnnotationHandler(obj, webServer);
        handlers.add(handler);
    }

}
