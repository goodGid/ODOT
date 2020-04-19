package goodgid.odot.controller;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import goodgid.odot.common.response.CommonResponse;
import goodgid.odot.common.response.enums.ReturnCode;
import goodgid.odot.service.healthcheck.HealthCheckService;

@RestController
@RequestMapping("/health/check")
public class HealthCheckController {
    private static Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    @Autowired
    private HealthCheckService healthCheckService;

    @GetMapping
    public CommonResponse<?> check(HttpServletResponse response) {
        if (!healthCheckService.check()) {
            return new CommonResponse<>(ReturnCode.SERVICE_UNAVAILABLE);
        }
        return new CommonResponse<>(ReturnCode.SUCCESS);
    }

}