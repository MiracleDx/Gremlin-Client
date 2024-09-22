package org.dongx.plugins;

import org.apache.tinkerpop.gremlin.driver.AuthProperties;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;

/**
 * GremlinClient
 *
 * @author <a href="mailto:dongxiang886@gmail.com">Dongx</a>
 * @since 1.0.0
 */
public class GremlinClient {

    private GremlinClient() {

    }

    public static Client init(String host, int port, String username, String password) {
        AuthProperties authProperties = new AuthProperties();
        authProperties.with(AuthProperties.Property.USERNAME, username);
        authProperties.with(AuthProperties.Property.PASSWORD, password);

        Cluster cluster = Cluster.build()
                .addContactPoint(host)
                .port(port)
                .authProperties(authProperties)
                .create();
        return cluster.connect().init();
    }
}
