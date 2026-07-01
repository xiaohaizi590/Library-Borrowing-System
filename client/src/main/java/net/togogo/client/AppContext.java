package net.togogo.client;

import javafx.stage.Stage;
import net.togogo.dto.LoginResponse;

import java.util.concurrent.ExecutorService;

/**
 * 全局共享上下文，持有各 View 共用的依赖
 */
public class AppContext {
    private final UserServiceClient userServiceClient;
    private final BookServiceClient bookServiceClient;
    private final TokenStore tokenStore;
    private final ExecutorService executorService;
    private final Stage primaryStage;
    private LoginResponse currentUser;

    public AppContext(UserServiceClient userServiceClient, BookServiceClient bookServiceClient,
                      TokenStore tokenStore, ExecutorService executorService, Stage primaryStage) {
        this.userServiceClient = userServiceClient;
        this.bookServiceClient = bookServiceClient;
        this.tokenStore = tokenStore;
        this.executorService = executorService;
        this.primaryStage = primaryStage;
    }

    public UserServiceClient getUserServiceClient() { return userServiceClient; }
    public BookServiceClient getBookServiceClient() { return bookServiceClient; }
    public TokenStore getTokenStore() { return tokenStore; }
    public ExecutorService getExecutorService() { return executorService; }
    public Stage getPrimaryStage() { return primaryStage; }

    public LoginResponse getCurrentUser() { return currentUser; }
    public void setCurrentUser(LoginResponse user) { this.currentUser = user; }

    public void logout() {
        currentUser = null;
        tokenStore.clearToken();
    }
}
