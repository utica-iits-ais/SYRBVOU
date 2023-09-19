package edu.utica.jobsub.ar.bloc.config.database;

import com.sct.messaging.bif.BatchResourceHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@Configuration
public class DataSourceConfiguration {

    @Bean
    public SingleConnectionDataSource getDataSource() {
        return new SingleConnectionDataSource(BatchResourceHolder.getConnection(),true);
    }

}
