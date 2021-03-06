package io.github.splotycode.mosaik.util.reflection.classregister;

import lombok.Setter;
import net.jodah.typetools.TypeResolver;

import java.util.Collection;

public class ListClassRegister<T> implements IListClassRegister<T> {

    @Setter private Collection<T> collection;
    private Class<T> clazz;

    public ListClassRegister() {
        this(null);
    }

    public ListClassRegister(Collection<T> collection) {
        this.collection = collection;
        clazz = (Class<T>) TypeResolver.resolveRawArguments(ListClassRegister.class, getClass())[0];
    }

    public ListClassRegister(Collection<T> collection, Class<T> clazz) {
        this.collection = collection;
        this.clazz = clazz;
    }

    @Override
    public Collection<T> getList() {
        return collection;
    }

    @Override
    public Class<T> getObjectClass() {
        return clazz;
    }

}
