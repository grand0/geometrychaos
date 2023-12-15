package ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.game;

import javafx.geometry.Point2D;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.ui.GameField;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.PlayerState;

public class Player {

    public static final double VELOCITY = 300.0;
    public static final double PLAYER_SIZE = 20;
    public static final int DEFAULT_HEALTH_POINTS = 3;
    public static final long DAMAGE_COOLDOWN_NS = 1_000_000_000L;
    public static final double DASH_VELOCITY = 900.0;
    public static final long DASH_DURATION_NS = 200_000_000L;
    public static final long DASH_DEFENSE_DURATION_NS = 400_000_000L;
    public static final long DASH_COOLDOWN_NS = 600_000_000L;
    public static final Point2D DEFAULT_SPAWN_POSITION = new Point2D(GameField.FIELD_WIDTH / 4.0, GameField.FIELD_HEIGHT / 2.0);

    private final int playerId;
    private final String username;
    private PlayerState state = PlayerState.IN_ROOM;

    private double positionX;
    private double positionY;
    private double velocityX;
    private double velocityY;
    private int healthPoints;

    private long lastUpdateTime = System.nanoTime();
    private long lastDamageTakenTime = 0;
    private long lastDashTime = 0;
    private boolean wasDashing = false;

    public Player(int playerId, String username) {
        this.playerId = playerId;
        this.username = username;
        this.positionX = DEFAULT_SPAWN_POSITION.getX();
        this.positionY = DEFAULT_SPAWN_POSITION.getY();
        this.healthPoints = DEFAULT_HEALTH_POINTS;
    }

    public void update() {
        if (!isDashing() && wasDashing) {
            wasDashing = false;
            setVelocityX(0);
            setVelocityY(0);
        }

        double delta = (System.nanoTime() - lastUpdateTime) / 1_000_000_000.0;
        double velocity = isDashing() ? DASH_VELOCITY : VELOCITY;
        Point2D normalizedVelocity = getNormalizedVelocity();
        positionX += normalizedVelocity.getX() * velocity * delta;
        positionY += normalizedVelocity.getY() * velocity * delta;
        positionX = clamp(positionX, 0, GameField.FIELD_WIDTH);
        positionY = clamp(positionY, 0, GameField.FIELD_HEIGHT);
        lastUpdateTime = System.nanoTime();
    }

    public Point2D getNormalizedVelocity() {
        Point2D velocityVector = new Point2D(velocityX, velocityY);
        return velocityVector.normalize();
    }

    public double getPositionX() {
        return positionX;
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(double velocityX) {
        if (!isDashing()) {
            this.velocityX = velocityX;
        }
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(double velocityY) {
        if (!isDashing()) {
            this.velocityY = velocityY;
        }
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public void setHealthPoints(int healthPoints) {
        this.healthPoints = healthPoints;
    }

    public void restoreHealthPoints() {
        this.healthPoints = DEFAULT_HEALTH_POINTS;
    }

    public void restoreInitialState() {
        restoreHealthPoints();
        this.positionX = DEFAULT_SPAWN_POSITION.getX();
        this.positionY = DEFAULT_SPAWN_POSITION.getY();
        this.velocityX = 0;
        this.velocityY = 0;
        lastDamageTakenTime = 0;
        lastDashTime = 0;
        wasDashing = false;
    }

    public boolean isDamageCooldownActive() {
        return System.nanoTime() - lastDamageTakenTime <= DAMAGE_COOLDOWN_NS;
    }

    public void damage() {
        if (!isDamageCooldownActive() && !isUnderDashDefense() && this.healthPoints > 0) {
            this.healthPoints--;
            lastDamageTakenTime = System.nanoTime();
        }
    }

    public boolean canDash() {
        return !isDashing() && System.nanoTime() - lastDashTime > DASH_COOLDOWN_NS;
    }

    public boolean isDashing() {
        return System.nanoTime() - lastDashTime <= DASH_DURATION_NS;
    }

    public boolean isUnderDashDefense() {
        return System.nanoTime() - lastDashTime <= DASH_DEFENSE_DURATION_NS;
    }

    public void dash() {
        if (canDash()) {
            lastDashTime = System.nanoTime();
            wasDashing = true;
            if (velocityX == 0 && velocityY == 0) {
                velocityX = 1;
            }
        }
    }

    public long getLastDashTime() {
        return lastDashTime;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getUsername() {
        return username;
    }

    public PlayerState getState() {
        return state;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }
}
