package me.david.webapi.response.content;

import java.io.IOException;
import java.io.InputStream;

public interface ResponseContent {

    InputStream getInputStream() throws IOException;

}