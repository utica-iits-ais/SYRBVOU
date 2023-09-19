package edu.utica.jobsub.ar.bloc.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookVoucher {

    private Integer pidm;
    private String bannerId;
    private String firstName;
    private String mi;
    private String lastName;
    private String termCode;
    private String termDescription;
    private String detailCode;
    private Double amount;
    private String prefEmail;
    private String stypCode;
    private String runType;
    private String voucherSentInd;
    private LocalDate transactionFromDate;
    private LocalDate transactionToDate;

}
