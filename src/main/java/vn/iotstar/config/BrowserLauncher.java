package vn.iotstar.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class BrowserLauncher {

    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void launchBrowser() {
        String port = environment.getProperty("local.server.port", environment.getProperty("server.port", "8080"));
        String contextPath = environment.getProperty("server.servlet.context-path", "");
        String url = "http://localhost:" + port + contextPath + "/";

        log.info("==================================================================");
        log.info("  APPLICATION STARTED SUCCESSFULLY!");
        log.info("  Access URL: {}", url);
        log.info("  Opening browser automatically...");
        log.info("==================================================================");

        openUrlInBrowser(url);
    }

    private void openUrlInBrowser(String url) {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                log.info("Browser launched via Desktop API: {}", url);
                return;
            }
        } catch (Exception ignored) {
        }

        try {
            Runtime runtime = Runtime.getRuntime();
            if (os.contains("win")) {
                runtime.exec(new String[]{"cmd", "/c", "start", url});
            } else if (os.contains("mac")) {
                runtime.exec(new String[]{"open", url});
            } else if (os.contains("nix") || os.contains("nux")) {
                runtime.exec(new String[]{"xdg-open", url});
            }
            log.info("Browser launched via command line: {}", url);
        } catch (IOException e) {
            log.warn("Could not automatically launch browser: {}", e.getMessage());
        }
    }
}
