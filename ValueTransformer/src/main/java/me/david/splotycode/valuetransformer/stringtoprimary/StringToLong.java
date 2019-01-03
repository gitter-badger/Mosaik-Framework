package me.david.splotycode.valuetransformer.stringtoprimary;

import me.david.davidlib.util.core.link.transformer.ValueTransformer;
import me.david.davidlib.util.core.link.transformer.TransformException;

public class StringToLong extends ValueTransformer<String, Long> {

    @Override
    public Long transform(String input) throws TransformException {
        try {
            return Long.valueOf(input);
        } catch (NumberFormatException ex) {
            throw new TransformException("Wrong Number Format: " + input, ex);
        }
    }

}
