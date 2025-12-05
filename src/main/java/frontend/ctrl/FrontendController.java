package frontend.ctrl;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import frontend.data.Sms;
import jakarta.servlet.http.HttpServletRequest;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

@Controller
@RequestMapping(path = "/sms")
public class FrontendController {

    private static final Counter smsPageViews = Counter.build()
            .name("sms_page_views_total")
            .help("Total number of times the SMS form page was loaded")
            .labelNames("device_type")
            .register();

    private static final Counter smsPredictionsTotal = Counter.build()
            .name("sms_predictions_total")
            .help("Total number of SMS predictions triggered via UI")
            .labelNames("result")
            .register();

    private static final Histogram smsPredictionLatencySeconds = Histogram.build()
            .name("sms_prediction_latency_seconds")
            .help("Latency of SMS prediction calls from the frontend in seconds")
            .buckets(0.05, 0.1, 0.25, 0.5, 1.0, 2.0, 5.0)
            .register();

    private String modelHost;

    private RestTemplateBuilder rest;

    public FrontendController(RestTemplateBuilder rest, Environment env) {
        this.rest = rest;
        this.modelHost = env.getProperty("MODEL_HOST");
        assertModelHost();
    }

    private void assertModelHost() {
        if (modelHost == null || modelHost.strip().isEmpty()) {
            System.err.println("ERROR: ENV variable MODEL_HOST is null or empty");
            System.exit(1);
        }
        modelHost = modelHost.strip();
        if (modelHost.indexOf("://") == -1) {
            var m = "ERROR: ENV variable MODEL_HOST is missing protocol, like \"http://...\" (was: \"%s\")\n";
            System.err.printf(m, modelHost);
            System.exit(1);
        } else {
            System.out.printf("Working with MODEL_HOST=\"%s\"\n", modelHost);
        }
    }

    @GetMapping("")
    public String redirectToSlash(HttpServletRequest request) {
        // relative REST requests in JS will end up on / and not on /sms
        return "redirect:" + request.getRequestURI() + "/";
    }

    @GetMapping("/")
    public String index(Model m, HttpServletRequest request) {
        m.addAttribute("hostname", modelHost);

        String ua = request.getHeader("User-Agent");
        String deviceType = classifyDeviceType(ua);
        smsPageViews.labels(deviceType).inc();

        return "sms/index";
    }

    @PostMapping({ "", "/" })
    @ResponseBody
    public Sms predict(@RequestBody Sms sms) {
        System.out.printf("Requesting prediction for \"%s\" ...\n", sms.sms);
        Histogram.Timer timer = smsPredictionLatencySeconds.startTimer();
        try {
            sms.result = getPrediction(sms);

            // count prediction by result label (spam/ham)
            String resultLabel = sms.result != null ? sms.result.trim() : "unknown";
            smsPredictionsTotal.labels(resultLabel).inc();

            System.out.printf("Prediction: %s\n", sms.result);
            return sms;
        } finally {
            timer.observeDuration();
        }
    }

    private String getPrediction(Sms sms) {
        try {
            var url = new URI(modelHost + "/predict");
            var c = rest.build().postForEntity(url, sms, Sms.class);
            return c.getBody().result.trim();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String classifyDeviceType(String userAgent) {
        if (userAgent == null) {
            return "unknown";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "mobile";
        }
        if (ua.contains("tablet") || ua.contains("ipad")) {
            return "tablet";
        }
        return "desktop";
    }

}