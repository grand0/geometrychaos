package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim;

import javafx.animation.Interpolator;

import static java.lang.Math.PI;

public class CustomInterpolates {

    public static final Interpolator ACCELERATE = new Interpolator() {

        @Override
        protected double curve(double t) {
            return t * t;
        }
    };

    public static final Interpolator DECELERATE = new Interpolator() {

        @Override
        protected double curve(double t) {
            return 1.0 - (1.0 - t) * (1.0 - t);
        }
    };

    public static final Interpolator ACCELERATE_DECELERATE = new Interpolator() {

        @Override
        protected double curve(double t) {
            return Math.cos((t + 1) * PI) / 2.0 + 0.5;
        }
    };

    public static final Interpolator ANTICIPATE = new Interpolator() {

        @Override
        protected double curve(double t) {
            return 2 * (t * t * t) - (t * t);
        }
    };

    public static final Interpolator OVERSHOOT = new Interpolator() {
        @Override
        protected double curve(double t) {
            return 2 * (t - 1) * (t - 1) * (t - 1) + (t - 1) * (t - 1) + 1;
        }
    };

    public static final Interpolator ANTICIPATE_OVERSHOOT = new Interpolator() {
        @Override
        protected double curve(double t) {
            if (t < 0.5) {
                return 0.5 * (2 * (2 * t) * (2 * t) * (2 * t) - (2 * t) * (2 * t));
            } else {
                return 0.5 * (2 * (2 * t - 2) * (2 * t - 2) * (2 * t - 2) + (2 * t - 2) * (2 * t - 2)) + 1;
            }
        }
    };

    public static final Interpolator BOUNCE = new Interpolator() {
        @Override
        protected double curve(double t) {
            if (t < 0.31489) {
                return 8 * (1.1226 * t) * (1.1226 * t);
            } else if (t < 0.6599) {
                return 8 * (1.1226 * t - 0.54719) * (1.1226 * t - 0.54719) + 0.7;
            } else if (t < 0.85908) {
                return 8 * (1.1226 * t - 0.8526) * (1.1226 * t - 0.8526) + 0.9;
            } else {
                return 8 * (1.1226 * t - 1.0435) * (1.1226 * t - 1.0435) + 0.95;
            }
        }
    };

    public static final Interpolator SIN_CYCLE = new Interpolator() {
        @Override
        protected double curve(double t) {
            return Math.sin(2 * PI * t);
        }
    };
}
