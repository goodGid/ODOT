package goodgid.odot.service.healthcheck;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import goodgid.odot.repository.oltp.HealthCheckMapper;

@Service
public class HealthCheckServiceImpl implements HealthCheckService {

    @Autowired
    public HealthCheckMapper healthCheckMapper;

    @Override
    public boolean check() {
        Integer rtn = healthCheckMapper.check();
        if (rtn == null || rtn.intValue() != 1) {
            return false;
        }
        return true;
    }
}
