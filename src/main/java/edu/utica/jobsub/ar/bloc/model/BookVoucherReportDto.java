package edu.utica.jobsub.ar.bloc.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookVoucherReportDto {

    @CsvBindByName(column = "Banner ID")
    private String bannerId;
    @CsvBindByName(column = "First Name")
    private String firstName;
    @CsvBindByName(column = "MI")
    private String mi;
    @CsvBindByName(column = "Last Name")
    private String lastName;
    @CsvBindByName(column = "Term Code")
    private String termCode;
    @CsvBindByName(column = "Term Description")
    private String termDescription;
    @CsvBindByName(column = "Detail Code")
    private String detailCode;
    @CsvBindByName(column = "Amount")
    private Double amount;
    @CsvBindByName(column = "Preferred Email")
    private String prefEmail;
    @CsvBindByName(column = "Student Type")
    private String stypCode;
    @CsvBindByName(column = "Reporting Transaction From Date")
    @CsvDate(value = "MM/dd/yyyy")
    private LocalDate transactionFromDate;
    @CsvBindByName(column = "Reporting Transaction To Date")
    @CsvDate(value = "MM/dd/yyyy")
    private LocalDate transactionToDate;

}
