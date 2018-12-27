package me.david.davidlib.parsing.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.david.davidlib.parsing.DomSourceType;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DomUrlInput implements DomInput {

    @Getter protected URL url;

    public DomUrlInput(URL url) {
        this.url = url;
    }

    protected URLConnection connection;

    @Override
    public byte[] getBytes() {
        try {
            return IOUtils.toByteArray(getStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getString() {
        try {
            return IOUtils.toString(getStream(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public InputStream getStream() {
        try (InputStream inputStream = getConnection().getInputStream()) {
            return inputStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public URLConnection getConnection() throws IOException {
        return connection == null ? (connection = url.openConnection()) : connection;
    }

    @Override
    public DomSourceType getType() {
        return DomSourceType.URL;
    }
}
