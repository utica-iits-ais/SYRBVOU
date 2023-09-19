package edu.utica.jobsub.ar.bloc.model;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvNumber;
import lombok.Data;

import java.time.LocalDate;

@Data
/**
 * DTO class for Voucher Send CSV that gets SFTP to Follett.
 * Original CSV from SYRBVOU.SQR had two dummy positions that were included in row results, that had no data.
 * These are still be included in this new version because I don't currently know why they existed, but the process is working as is.
 */
public class BookVoucherSendDto {

    @CsvBindByPosition(position = 0)
    private String bannerId;
    @CsvBindByPosition(position = 1)
    private String dummyPosition1;
    @CsvBindByPosition(position = 2)
    private String firstName;
    @CsvBindByPosition(position = 3)
    private String mi;
    @CsvBindByPosition(position = 4)
    private String lastName;
    @CsvBindByPosition(position = 5)
    @CsvNumber(value = "0.000000")
    private Double amount;
    @CsvBindByPosition(position = 6)
    private String providerCode;
    @CsvBindByPosition(position = 7)
    @CsvDate(value = "MM/dd/yyyy")
    private LocalDate transactionFromDate;
    @CsvBindByPosition(position = 8)
    @CsvDate(value= "MM/dd/yyyy")
    private LocalDate transactionToDate;
    @CsvBindByPosition(position = 9)
    private String dummyPosition2;
    @CsvBindByPosition(position = 10)
    private String prefEmail;

}
