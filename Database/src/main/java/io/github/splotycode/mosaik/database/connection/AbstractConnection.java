package io.github.splotycode.mosaik.database.connection;

import io.github.splotycode.mosaik.util.StringUtil;

public abstract class AbstractConnection<T extends AbstractConnection> implements Connection<T> {

    @Override
    public T connect(String host, String database) {
        if (host.contains(":")) {
            String[] split = host.split(":");
            if (split.length == 2) {
                if (StringUtil.isInteger(split[1])) {
                    return connect(split[0], database, Integer.valueOf(split[1]));
                } else {
                    throw new IllegalArgumentException("Invalid Host format Examples: localhost or localhost:" + getDefaultPort());
                }
            }
        }
        return connect(host, database, getDefaultPort());
    }

    @Override
    public T connect(String host, String user, String password, String database) {
        if (host.contains(":")) {
            String[] split = host.split(":");
            if (split.length == 2) {
                if (StringUtil.isInteger(split[1])) {
                    return connect(split[0], user, password, database, Integer.valueOf(split[1]));
                } else {
                    throw new IllegalArgumentException("Invalid Host format Examples: localhost or localhost:" + getDefaultPort());
                }
            }
        }
        return connect(host, user, password, database, getDefaultPort());
    }

}
