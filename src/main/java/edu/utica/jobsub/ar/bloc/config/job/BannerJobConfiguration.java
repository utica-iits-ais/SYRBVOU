package edu.utica.jobsub.ar.bloc.config.job;

import com.sct.messaging.bif.BatchResourceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BannerJobConfiguration {

    private ApplicationArguments applicationArguments;
    @Autowired
    BannerJobConfiguration(ApplicationArguments applicationArguments) {
        this.applicationArguments = applicationArguments;
    }

    @Bean
    public BannerJob getBannerJob() {
        return new BannerJob(applicationArguments.getSourceArgs()[0],applicationArguments.getSourceArgs()[1],BatchResourceHolder.getJobParameterMap());
    }

}
