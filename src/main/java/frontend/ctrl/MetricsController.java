package frontend.ctrl;

import java.io.IOException;
import java.io.Writer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class MetricsController {

    private final CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    @GetMapping(value = "/metrics")
    public void metrics(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(TextFormat.CONTENT_TYPE_004);

        try (Writer writer = response.getWriter()) {
            TextFormat.write004(writer, registry.metricFamilySamples());
        }
    }
}
