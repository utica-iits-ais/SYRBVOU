package edu.utica.jobsub.ar.bloc;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import edu.utica.jobsub.ar.bloc.config.job.BannerJob;
import edu.utica.jobsub.ar.bloc.csv.BookVouchersCsv;
import edu.utica.jobsub.ar.bloc.mapper.BookVoucherMapper;
import edu.utica.jobsub.ar.bloc.model.BookVoucher;
import edu.utica.jobsub.ar.bloc.model.BookVoucherReportDto;
import edu.utica.jobsub.ar.bloc.model.BookVoucherSendDto;
import edu.utica.jobsub.ar.bloc.services.BookVoucherService;
import edu.utica.jobsub.ar.bloc.sftp.BookVoucherSftp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProcessVouchers {

    private final BannerJob job;
    private final String csvPath;
    private final BookVoucherService bookVoucherService;
    private final BookVoucherMapper bookVoucherMapper;
    private final BookVouchersCsv bookVouchersCsv;
    private final BookVoucherSftp bookVoucherSftp;
    @Autowired
    public ProcessVouchers(
            BannerJob job,
            @Value("${csv.output.path}") String csvPath,
            BookVoucherService bookVoucherService,
            BookVoucherMapper bookVoucherMapper,
            BookVouchersCsv bookVouchersCsv,
            BookVoucherSftp bookVoucherSftp)
    {
        this.job = job;
        this.csvPath = csvPath;
        this.bookVoucherService = bookVoucherService;
        this.bookVoucherMapper = bookVoucherMapper;
        this.bookVouchersCsv = bookVouchersCsv;
        this.bookVoucherSftp = bookVoucherSftp;
    }

    public void execute() throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        if (job.getParameters().isEmpty() || job.getParameters().values().stream().anyMatch(Objects::isNull)) {
            log.warn("Null values in job submission parameters. All parameters are required in the book voucher job. Ending program.");
            return;
        }

        List<BookVoucher> bookVouchers =
                bookVoucherService.findBookVouchers();

        // convert vouchers for report CSV DTO from bookVoucher model
        List<BookVoucherReportDto> bookVouchersReport =
                bookVouchers
                        .stream()
                        .map(b -> bookVoucherMapper.bookVoucherToBookVoucherReportDto(b))
                        .collect(Collectors.toList());

        // Generate Report CSV using BookVoucherReportDto List
        bookVouchersCsv.createReport(
                Paths.get(csvPath,job.getJobName().toLowerCase() + "_" + job.getJobNumber() + ".csv"),
                bookVouchersReport
        );

        // get vouchers to send to Follett via SFTP in CSV file. Either run type F (Full) each day or I (Incremental Run) during day && voucher hasn't been sent. Map voucher to voucher send DTO using mapStruct.
        List<BookVoucherSendDto> bookVouchersSend =
                bookVouchers
                        .stream()
                        .filter(b -> b.getAmount() > 0)
                        .filter(b -> (b.getRunType().equals("F") || (b.getRunType().equals("I") && b.getVoucherSentInd().equals("N"))))
                        .map(b -> bookVoucherMapper.bookVoucherToBookVoucherSendDto(b))
                        .collect(Collectors.toList());

        // Generate Send Vouchers CSV to SFTP to Follett
        DateTimeFormatter sendFileDateTimeFormatter = new DateTimeFormatterBuilder().appendPattern("MMddyyyyHHmm").toFormatter();
        Path sendFileCsv = Paths.get(csvPath,job.getJobName() + "_" + LocalDateTime.now().format(sendFileDateTimeFormatter) + ".csv");
        if (bookVouchersSend.size() > 0) {
            bookVouchersCsv.createSendFile(
                    sendFileCsv,
                    bookVouchersSend
            );
        }

        // If it exists SFTP file to Follett and then delete
        if (Files.exists(sendFileCsv)) {
            bookVoucherSftp.uploadVoucherSendFile(sendFileCsv);
            Files.copy(sendFileCsv,Paths.get(csvPath,job.getJobName().toLowerCase() + "_send_" + job.getJobNumber() + ".csv"), StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.COPY_ATTRIBUTES);
            Files.delete(sendFileCsv);
        }

        // For all of the vouchers that are being sent insert a record into the SYRBVOE tracking table.
        bookVouchers
                .stream()
                .filter(b -> b.getAmount() > 0)
                .filter(b -> (b.getRunType().equals("F") || (b.getRunType().equals("I") && b.getVoucherSentInd().equals("N"))))
                .forEach(b -> bookVoucherService.saveVoucherTracking(b));

        // At the end of this program insert the run date into SYRBVOU for run_type sql query F daily, or I incremental for any other runs within day
        bookVoucherService.insertRun();

    }

}
