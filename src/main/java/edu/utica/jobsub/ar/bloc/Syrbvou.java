package edu.utica.jobsub.ar.bloc;

import com.sct.messaging.bif.banner.BannerBatchProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@Slf4j
public class Syrbvou extends BannerBatchProcessor implements CommandLineRunner {

    @Autowired
    private ProcessVouchers processVouchers;

    @Override
    public void processJob() {
        new SpringApplicationBuilder(Syrbvou.class)
                .web(WebApplicationType.NONE)
                .run(getJobName(),getJobNumber());
    }

    @Override
    public void run(String... args) {
        try {
            processVouchers.execute();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

}
