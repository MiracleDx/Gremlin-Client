package org.dongx.plugins;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * GremlinConfig
 *
 * @author <a href="mailto:dongxiang886@gmail.com">Dongx</a>
 * @since 1.0.0
 */
@Service
@State(
        name = "GremlinConfig",
        storages = {@Storage("GremlinConfig.xml")}
)
public final class GremlinConfig implements PersistentStateComponent<GremlinConfig.State> {

    private State state = new State();

    public static GremlinConfig getInstance() {
        return com.intellij.openapi.application.ApplicationManager.getApplication().getService(GremlinConfig.class);
    }

    public static class State {
        public String host;
        public String port;
        public String username;
        public String password;
    }

    @Override
    @Nullable
    public GremlinConfig.State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    @Override
    public void noStateLoaded() {
        PersistentStateComponent.super.noStateLoaded();
    }

    @Override
    public void initializeComponent() {
        PersistentStateComponent.super.initializeComponent();
    }

    public void setHost(String host) {
        state.host = host;
    }

    public String getHost() {
        return state.host;
    }

    public void setPort(String port) {
        state.port = port;
    }

    public String getPort() {
        return state.port;
    }

    public String getUsername() {
        return state.username;
    }

    public void setUsername(String username) {
        state.username = username;
    }

    public String getPassword() {
        return state.password;
    }

    public void setPassword(String password) {
        state.password = password;
    }
}
