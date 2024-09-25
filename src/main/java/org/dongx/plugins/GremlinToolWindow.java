package org.dongx.plugins;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.RequestOptions;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * GremlinToolWindow
 *
 * @author <a href="mailto:dongxiang886@gmail.com">Dongx</a>
 * @since 1.0.0
 */
public class GremlinToolWindow {
    private JPanel mainPanel;
    private JTextField hostField;

    private JTextField portField;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextArea sqlTextArea;
    private JButton executeButton;
    private ConsoleView consoleView;
    private JComboBox<String> sqlHistoryComboBox;  // 用于展示 SQL 历史记录的下拉框

    // 用于存储 SQL 语句历史记录
    private List<String> sqlHistory = new ArrayList<>();
    private int historyIndex = -1;

    Client client;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public GremlinToolWindow(@NotNull Project project) {
        // 初始化UI
        createUIComponents(project);

        // 加载上次配置
        loadConfiguration();

        // 执行 SQL 按钮点击事件
        executeButton.addActionListener(e -> {
            String host = hostField.getText();
            String port = portField.getText();
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String query = sqlTextArea.getText();

            // 保存配置
            saveConfiguration();

            // 启动后台任务来执行 SQL 查询
            new Task.Backgroundable(project, "Executing Gremlin Query", true) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    // 建立或复用已有的数据库连接
                    if (client == null) {
                        client = GremlinClient.init(host, Integer.parseInt(port), username, password);
                    }

                    String queryTime = LocalDateTime.now().format(formatter);
                    if (client != null) {
                        // 执行 SQL 查询
                        String result = "查询无结果";
                        try {
                            List<Result> results = client.submit(query, RequestOptions.build().timeout(60000000L).create()).all().get();
                            if (!results.isEmpty()) {
                                result = results.get(0).getString();
                            }
                        } catch (InterruptedException | ExecutionException ex) {
                           result = ex.getLocalizedMessage();
                        }

                        // 通过在事件调度线程 (EDT) 中更新控制台
                        String finalResult = result;
                        SwingUtilities.invokeLater(() -> {
                            // 显示结果
                            // 将结果输出到控制台
                            consoleView.print(queryTime + "(query)" + ": " + query + "\n"
                                    + LocalDateTime.now().format(formatter) + "(result)" + ": " + finalResult + "\n\n", ConsoleViewContentType.NORMAL_OUTPUT);

                            // 保存 SQL 到历史记录中，并重置索引
                            if (!query.trim().isEmpty()) {
                                sqlHistory.add(query);
                                sqlHistoryComboBox.addItem(query);  // 将 SQL 添加到下拉框中
                                historyIndex = sqlHistory.size();  // 将索引重置到最新的位置
                                sqlHistoryComboBox.setSelectedIndex(sqlHistoryComboBox.getItemCount() - 1);
                            }
                        });
                    }
                }
            }.queue();
        });


        // 监听 SQL TextArea 的上下键事件以切换历史记录
        sqlTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    // 上键：查看上一条历史记录
                    if (historyIndex > 0) {
                        historyIndex--;
                        sqlTextArea.setText(sqlHistory.get(historyIndex));
                        sqlHistoryComboBox.setSelectedIndex(historyIndex);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    // 下键：查看下一条历史记录
                    if (historyIndex < sqlHistory.size() - 1) {
                        historyIndex++;
                        sqlTextArea.setText(sqlHistory.get(historyIndex));
                        sqlHistoryComboBox.setSelectedIndex(historyIndex);
                    } else if (historyIndex == sqlHistory.size() - 1) {
                        historyIndex++;
                        sqlTextArea.setText("");  // 到达最后时，清空输入框
                        sqlHistoryComboBox.setSelectedIndex(-1);
                    }
                }
            }
        });

        // 监听下拉框选择事件，自动填充到 SQL 文本框中
        sqlHistoryComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String selectedQuery = (String) sqlHistoryComboBox.getSelectedItem();
                if (selectedQuery != null) {
                    sqlTextArea.setText(selectedQuery);
                }
            }
        });
    }

    private void loadConfiguration() {
        GremlinConfig config = GremlinConfig.getInstance();
        hostField.setText(config.getHost());
        portField.setText(config.getPort());
        usernameField.setText(config.getUsername());
        passwordField.setText(config.getPassword());

        // String savedPassword = PasswordSafe.getInstance().getPassword(null, "databasePasswordKey");
    }

    private void saveConfiguration() {
        GremlinConfig config = GremlinConfig.getInstance();
        config.setHost(hostField.getText());
        config.setPort(portField.getText());
        config.setUsername(usernameField.getText());
        config.setPassword(new String(passwordField.getPassword()));

        // 使用 PasswordSafe 存储密码
        // PasswordSafe.getInstance().setPassword(null, new String(passwordField.getPassword()));
    }

    public JPanel getContent() {
        return mainPanel;
    }

    private void createUIComponents(@NotNull Project project) {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // HOST 输入
        hostField = new JTextField();
        mainPanel.add(new JLabel("Host:"));
        mainPanel.add(hostField);

        // PORT 输入
        portField = new JTextField();
        mainPanel.add(new JLabel("Port:"));
        mainPanel.add(portField);

        // 用户名输入
        usernameField = new JTextField();
        mainPanel.add(new JLabel("Username:"));
        mainPanel.add(usernameField);

        // 密码输入
        passwordField = new JPasswordField();
        mainPanel.add(new JLabel("Password:"));
        mainPanel.add(passwordField);

        // SQL 查询输入
        sqlTextArea = new JTextArea(10, 40);
        mainPanel.add(new JLabel("SQL Query:"));
        mainPanel.add(new JScrollPane(sqlTextArea));

        // SQL 历史记录下拉框
        sqlHistoryComboBox = new ComboBox<>();
        mainPanel.add(new JLabel("SQL History:"));
        mainPanel.add(sqlHistoryComboBox);

        // 执行按钮
        executeButton = new JButton("Execute SQL");
        mainPanel.add(executeButton);

        // 初始化 ConsoleView 并添加到面板中
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        openConsoleInWindow(project);
    }

    // 方法：通过 Run Content Manager 打开独立的控制台窗口
    public void openConsoleInWindow(Project project) {
        // 创建一个 JPanel 来包含 ConsoleView
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(consoleView.getComponent(), BorderLayout.CENTER);

        // 创建一个 RunContentDescriptor 来描述运行内容
        RunContentDescriptor descriptor = new RunContentDescriptor(
                consoleView, null, panel, "Gremlin Client Console", null
        );

        // 将控制台窗口显示在 Run Content Manager 中
        RunContentManager.getInstance(project).showRunContent(
                DefaultRunExecutor.getRunExecutorInstance(),
                descriptor
        );
    }

}
