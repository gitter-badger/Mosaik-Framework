package me.david.webapi.response.content.string;

import me.david.davidlib.util.io.ByteArrayInputStream;
import me.david.davidlib.util.io.Charsets;
import me.david.webapi.response.content.ResponseContent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class StaticStringContent implements ResponseContent {

    private Charset charset;
    private String str;

    public StaticStringContent(String str) {
        charset = Charsets.UTF8;
        this.str = str;
    }

    public StaticStringContent(String str, String charset) {
        this.charset = Charset.forName(charset);
        this.str = str;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(str.getBytes(charset));
    }
}
