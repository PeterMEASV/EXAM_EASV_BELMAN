package exam_easv_belman.GUI.util;

import javafx.application.Platform;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Timer;
import java.util.TimerTask;

public class TimerManager {
    private int cooldown = 5000;
    private Circle objStatus;
    private Timer inactivityTimer;
    private TimerTask inactivityTimerTask;
    private static final Color ACTIVE_COLOR = Color.rgb(35, 200, 35);
    private static final Color INACTIVE_COLOR = Color.rgb(201, 173,23);

    public TimerManager(Circle objStatus) {
        this.objStatus = objStatus;
    }

    public void initialize() {
        setStatusActive();
        setupInactivityTimer();
    }

    private void setStatusActive() {
        objStatus.setFill(ACTIVE_COLOR);
        initializeStatus(ACTIVE_COLOR);
    }

    private void setStatusInactive() {
        objStatus.setFill(INACTIVE_COLOR);
        initializeStatus(INACTIVE_COLOR);
    }

    private void initializeStatus(Color color){
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(4);
        objStatus.setEffect(glow);
    }

    private void setupInactivityTimer() {
        cleanup(); // Clean up existing timer
        inactivityTimer = new Timer(true);
        resetInactivityTimer();

        Platform.runLater(() -> {
            objStatus.getScene().getWindow().addEventFilter(MouseEvent.ANY, event -> resetInactivityTimer());
            objStatus.getScene().getWindow().addEventFilter(KeyEvent.ANY, event -> resetInactivityTimer());
        });
    }

    private void resetInactivityTimer() {
        if (inactivityTimerTask != null) {
            inactivityTimerTask.cancel();
        }

        setStatusActive();

        // Create new timer if the current one is cancelled
        if (inactivityTimer == null || isTimerCancelled()) {
            inactivityTimer = new Timer(true);
        }

        inactivityTimerTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> setStatusInactive());
            }
        };

        try {
            inactivityTimer.schedule(inactivityTimerTask, cooldown); // 120000milli = 2 minutes
        } catch (IllegalStateException e) {
            // If scheduling fails, create new timer and try again
            inactivityTimer = new Timer(true);
            inactivityTimer.schedule(inactivityTimerTask, cooldown);
        }
    }

    private boolean isTimerCancelled() {
        try {
            inactivityTimer.schedule(new TimerTask() {
                @Override
                public void run() {}
            }, 0);
            return false;
        } catch (IllegalStateException e) {
            return true;
        }
    }

    public void cleanup() {
        if (inactivityTimer != null) {
            inactivityTimer.cancel();
            inactivityTimer = null;
        }
        if (inactivityTimerTask != null) {
            inactivityTimerTask.cancel();
            inactivityTimerTask = null;
        }
    }
}