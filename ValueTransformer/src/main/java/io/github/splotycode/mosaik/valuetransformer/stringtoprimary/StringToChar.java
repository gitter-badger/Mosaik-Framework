package io.github.splotycode.mosaik.valuetransformer.stringtoprimary;

import io.github.splotycode.mosaik.valuetransformer.TransformException;
import io.github.splotycode.mosaik.valuetransformer.ValueTransformer;

public class StringToChar extends ValueTransformer<String, Character> {

    @Override
    public Character transform(String input) throws TransformException {
        char[] chars = input.toCharArray();
        if (chars.length != 1) throw new TransformException("Need exactly one char");
        return chars[0];
    }

}
