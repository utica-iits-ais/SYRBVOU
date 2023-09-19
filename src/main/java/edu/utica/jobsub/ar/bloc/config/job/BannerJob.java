package edu.utica.jobsub.ar.bloc.config.job;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;
import java.util.Objects;

public class BannerJob {

    private String jobName;
    private String jobNumber;
    private Map parameters;
    private final DateTimeFormatter parameterDateTimeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d-MMM-yyyy").toFormatter();

    public BannerJob(String jobName, String jobNumber, Map parameters) {
        this.jobName = jobName;
        this.jobNumber = jobNumber;
        this.parameters = parameters;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public DateTimeFormatter getParameterDateTimeFormatter() {
        return parameterDateTimeFormatter;
    }

    @Override
    public String toString() {
        return "BannerJob{" +
                "jobName='" + jobName + '\'' +
                ", jobNumber='" + jobNumber + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BannerJob bannerJob = (BannerJob) o;
        return Objects.equals(jobName, bannerJob.jobName) && Objects.equals(jobNumber, bannerJob.jobNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName, jobNumber);
    }
}
