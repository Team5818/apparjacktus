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

package org.rivierarobotics.appjack.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.ContextAwareBase;
import edu.wpi.first.wpilibj.DriverStation;
import org.rivierarobotics.appjack.Config;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPOutputStream;

/**
 * A simple logback configuration good for limited disk space.
 *
 * To use, provide it as the configuration implementation via the {@link java.util.ServiceLoader}.
 */
public final class SimpleRobotLogConfiguration extends ContextAwareBase implements Configurator {
    @Override
    public void configure(LoggerContext loggerContext) {
        addInfo("Using apparjacktus's logging configuration!");
        var ds = DriverStation.getInstance();
        ds.waitForData();
        if (!Config.logNonMatches().get() && ds.getMatchType() == DriverStation.MatchType.None) {
            // Do not store data for non-matches, to avoid filling the logs
            addWarn("Not configuring loggers, non-match detected.");
            return;
        }

        var layout = new PatternLayout();
        layout.setPattern("");
        layout.setContext(loggerContext);
        layout.start();

        var encoder = new LayoutWrappingEncoder<ILoggingEvent>();
        encoder.setContext(loggerContext);
        encoder.setLayout(layout);

        try {
            var fileOutputStream = Files.newOutputStream(
                Path.of("mechanisms" + getUniqueMatchName() + ".log.gz"),
                StandardOpenOption.APPEND, StandardOpenOption.CREATE
            );
            var outputStream = new GZIPOutputStream(fileOutputStream);

            var appender = new OutputStreamAppender<ILoggingEvent>();
            appender.setName("mechanisms-log");
            appender.setOutputStream(outputStream);
            appender.setContext(loggerContext);
            appender.setEncoder(encoder);
            appender.start();

            loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String getUniqueMatchName() {
        var ds = DriverStation.getInstance();
        if (ds.getMatchType() == DriverStation.MatchType.None) {
            // use a timestamp
            return "none-" + DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").format(Instant.now());
        } else {
            // build a label like caln-qual5.1
            var result = new StringBuilder();
            result.append(ds.getEventName()).append('-');
            switch (ds.getMatchType()) {
                case Practice:
                    result.append("practice");
                    break;
                case Qualification:
                    result.append("qual");
                    break;
                case Elimination:
                    result.append("elim");
            }
            result.append(ds.getMatchNumber()).append('.').append(ds.getReplayNumber());
            return result.toString();
        }
    }
}
