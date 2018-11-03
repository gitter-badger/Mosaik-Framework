package me.david.webapi.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.david.webapi.response.content.ResponseContent;
import me.david.webapi.response.content.StringResponseContent;
import me.david.webapi.server.Request;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Getter
@EqualsAndHashCode
public class Response {

    @Getter private HttpVersion httpVersion = HttpVersion.VERSION_1_1;
    private Map<String, String> headers = new HashMap<>();
    @Setter private ResponseContent content;
    private InputStream rawContent;
    @Setter private int responseCode = 200;

    public Response(ResponseContent content) {
        this.content = content;
    }

    public Response setHeader(HttpHeaders httpHeader, String value) {
        headers.put(httpHeader.name().toLowerCase().replace('_', '-'), value);
        return this;
    }

    public Response setHeader(String httpHeader, String value) {
        headers.put(httpHeader, value);
        return this;
    }

    public Response setContentType(ContentType contentType) {
        setHeader(HttpHeaders.CONTENT_TYPE, contentType.value());
        return this;
    }

    public void finish(Request request) {
        if (content == null) {
            content = new StringResponseContent("No Content Provided");
        }
        try {
            rawContent = content.getInputStream();
            setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(rawContent.available()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (request != null && request.isKeepAlive()) {
            setHeader(HttpHeaders.CONNECTION, "keep-alive");
        }
    }



}