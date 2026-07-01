package net.togogo.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.togogo.client.view.SceneCache;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JavaFX 入口，负责初始化 Spring 容器和导航到登录页
 */
public class FxApplication extends Application {

    private static ConfigurableApplicationContext springContext;
    private AppContext appContext;
    private ExecutorService executorService;
    private Runnable onLoginSuccess;
    private Runnable onLogout;

    @Override
    public void start(Stage primaryStage) {
        springContext = SpringApplication.run(ClientApplication.class, new String[0]);
        executorService = Executors.newFixedThreadPool(4);

        UserServiceClient userServiceClient = springContext.getBean(UserServiceClient.class);
        BookServiceClient bookServiceClient = springContext.getBean(BookServiceClient.class);
        TokenStore tokenStore = springContext.getBean(TokenStore.class);

        appContext = new AppContext(userServiceClient, bookServiceClient, tokenStore, executorService, primaryStage);

        onLoginSuccess = () -> {
            SceneCache.clear();
            primaryStage.setScene(SceneCache.getMainScene(appContext, onLogout));
        };

        onLogout = () -> {
            appContext.logout();
            SceneCache.clear();
            primaryStage.setScene(SceneCache.getLoginScene(appContext, onLoginSuccess));
        };

        primaryStage.setTitle("图书管理系统");
        primaryStage.setWidth(1100);
        primaryStage.setHeight(750);
        primaryStage.setScene(SceneCache.getLoginScene(appContext, onLoginSuccess));
        primaryStage.show();
    }

    @Override
    public void stop() {
        executorService.shutdown();
        if (appContext != null) {
            appContext.logout();
        }
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
