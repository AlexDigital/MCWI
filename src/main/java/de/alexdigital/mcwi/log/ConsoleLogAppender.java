package de.alexdigital.mcwi.log;

import de.alexdigital.mcwi.McWebinterface;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;

@Plugin(name = "ConsoleLogAppender", category = "Core", elementType = "appender", printObject = true)
public class ConsoleLogAppender extends AbstractAppender {

    public ConsoleLogAppender() {
        super("ConsoleLogAppender", null,
                PatternLayout.createLayout(
                        "[%d{HH:mm:ss} %level]: %msg",
                        null, null, null, null), false);
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public void append(LogEvent e) {
        McWebinterface.getInstance().getWebServer().getSocketIO().getServer()
                .getBroadcastOperations().sendEvent("console", e.getMessage().getFormattedMessage().trim());
    }

}
