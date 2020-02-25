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

import com.techshroom.jungle.Loaders;
import com.techshroom.jungle.SysPropConfigOption;
import com.techshroom.jungle.SysPropNamespace;

public class Config {
    private static final SysPropNamespace NS = SysPropNamespace.create("apparjacktus");

    private static final SysPropConfigOption<Boolean> LOG_NON_MATCHES = NS.create("log-non-matches",
        Loaders.forBoolean(), false);

    public static SysPropConfigOption<Boolean> logNonMatches() {
        return LOG_NON_MATCHES;
    }

    private static final SysPropConfigOption<Double> POWER_COMPARE_FUZZ = NS
        .create("power-cmp-fuzz",
            Loaders.forDoubleInRange(0, Double.MAX_VALUE), 0.001);

    /**
     * Configuration for the fuzziness when comparing power values, i.e.
     * how far can two values become before being considered different.
     */
    public static SysPropConfigOption<Double> powerCompareFuzz() {
        return POWER_COMPARE_FUZZ;
    }

    private Config() {
    }

}
