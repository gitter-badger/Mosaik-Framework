package io.github.splotycode.mosaik.valuetransformer;

import lombok.Getter;
import io.github.splotycode.mosaik.util.reflection.ReflectionUtil;

import java.lang.reflect.Type;

public abstract class ValueTransformer<I, O> {

    @Getter private Class<I> inputClass;
    @Getter private Class<O> outputClass;

    public abstract O transform(I input) throws TransformException;

    public ValueTransformer() {
        Type[] generics = ReflectionUtil.getGenerics(getClass());
        inputClass = (Class<I>) generics[0];
        outputClass = (Class<O>) generics[1];
    }

    public boolean valid(I input, Class<? extends O> output) {
        return ReflectionUtil.isAssignable(inputClass, input.getClass()) && ReflectionUtil.isAssignable(outputClass, output);
    }

}
