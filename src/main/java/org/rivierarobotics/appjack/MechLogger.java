/*
 * This file is part of apparjacktus, licensed under the GNU General Public License (GPLv3).
 *
 * Copyright (c) Riviera Robotics <https://github.com/Team5818>
 * Copyright (c) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.rivierarobotics.appjack;

import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collector;

/**
 * Mechanism logger. Provides utilities for tracking mechanism changes.
 */
public final class MechLogger {

    private static final double POWER_CMP_FUZZ = Config.powerCompareFuzz().get();
    private final Logger delegate;
    private final String tagsProcessed;
    private final Map<String, Object> state = new HashMap<>();
    private double lastPower;
    private Double lastSetpoint;

    MechLogger(Logger delegate, List<String> tags) {
        this.delegate = delegate;
        this.tagsProcessed = tags.stream().collect(Collector.of(
                MechLogger::newTagJoiner,
                StringJoiner::add,
                StringJoiner::merge,
                StringJoiner::toString
        ));
    }

    private static boolean basicallyEqual(double a, double b) {
        return a - POWER_CMP_FUZZ < b && b < a + POWER_CMP_FUZZ;
    }

    private static StringJoiner newTagJoiner() {
        return new StringJoiner(",", "tags=", ", ")
                .setEmptyValue("");
    }

    private void logPower(double power) {
        delegate.info(tagsProcessed + "op=power_change, power=" + power);
    }

    private void logSetpoint(double setpoint) {
        delegate.info(tagsProcessed + "op=setpoint_change, setpoint=" + setpoint);
    }

    private void logStateChange(String name, Object value) {
        delegate.info(tagsProcessed + "op=condition_change, name=" + name + ", value=" + value);
    }

    /**
     * Log a change in power, if different enough.
     * @param power the new mechanism power
     */
    public void powerChange(double power) {
        double lastLastPower = this.lastPower;
        this.lastPower = power;
        if (basicallyEqual(lastLastPower, power)) {
            return;
        }
        if (basicallyEqual(lastLastPower, 0.0) || basicallyEqual(power, 0.0)) {
            // switching from 0 -> any power, or vice versa
            logPower(power);
        } else if (Math.abs(lastLastPower - power) >= 0.05) {
            // change in power of >=0.05
            logPower(power);
        }
    }

    /**
     * Clear the last setpoint for this mechanism. Should be called when the setpoint
     * is reached, so that the logger is aware that the mechanism may move away from it.
     */
    public void clearSetpoint() {
        lastSetpoint = null;
    }

    /**
     * Log a setpoint change, if different enough.
     * @param setpoint the new setpoint
     */
    public void setpointChange(double setpoint) {
        if (lastSetpoint != null && basicallyEqual(lastSetpoint, setpoint)) {
            return;
        }
        lastSetpoint = setpoint;
        logSetpoint(setpoint);
    }

    /**
     * Log a general change in the state of the mechanism.
     *
     * Will not log anything if they are {@linkplain Objects#equals(Object, Object) equal}.
     *
     * @param name the name / key for the state
     * @param value the new value of the state
     */
    public void stateChange(String name, Object value) {
        if (Objects.equals(state.get(name), value)) {
            return;
        }
        state.put(name, value);
        logStateChange(name, value);
    }
}
