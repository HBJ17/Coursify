package com.crs.controller;

import com.crs.service.AuthService;
import com.crs.ui.PanelFactory;
import com.crs.ui.ScreenNavigator;
import com.crs.ui.Session;
import java.awt.Component;

/** Checks the login in the background, then opens the student or admin screen depending on the role. */
public class LoginController extends BaseController {
    private final AuthService authService;
    private final Session session;
    private final ScreenNavigator navigator;

    public LoginController(Component parent, AuthService authService, Session session, ScreenNavigator navigator) {
        super(parent);
        this.authService = authService;
        this.session = session;
        this.navigator = navigator;
    }

    /** afterAttempt runs when the attempt finishes (success or failure), e.g. to re-enable the button. */
    public void login(String id, String password, Runnable afterAttempt) {
        if (id.isBlank() || password.isEmpty()) {
            showInfo("Please enter your ID and password.");
            afterAttempt.run();
            return;
        }
        runAsync(() -> authService.login(id.trim().toUpperCase(), password),
                person -> {
                    afterAttempt.run();
                    session.login(person);
                    navigator.showScreen(session.isAdmin() ? PanelFactory.ADMIN : PanelFactory.STUDENT);
                },
                error -> {
                    afterAttempt.run();
                    showError(error);
                });
    }
}
