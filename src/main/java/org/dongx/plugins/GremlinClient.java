package org.dongx.plugins;

import org.apache.tinkerpop.gremlin.driver.AuthProperties;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.util.ser.GraphBinaryMessageSerializerV1;

import java.util.Map;

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

        GraphBinaryMessageSerializerV1 serializerV1 = new GraphBinaryMessageSerializerV1();
        serializerV1.configure(Map.of("serializeResultToString", true), Map.of());

        Cluster cluster = Cluster.build()
                .addContactPoint(host)
                .port(port)
                .authProperties(authProperties)
                .serializer(serializerV1)
                .create();
        return cluster.connect().init();
    }
}
