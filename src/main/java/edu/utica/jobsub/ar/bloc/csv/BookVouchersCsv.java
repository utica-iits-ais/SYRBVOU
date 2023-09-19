package edu.utica.jobsub.ar.bloc.csv;

import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import edu.utica.jobsub.ar.bloc.model.BookVoucherReportDto;
import edu.utica.jobsub.ar.bloc.model.BookVoucherSendDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


@Component
public class BookVouchersCsv {

    private final ResourceLoader resourceLoader;

    @Autowired
    public BookVouchersCsv(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void createSendFile(Path sendFileCsv, List<BookVoucherSendDto> list) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        try (Writer writer = Files.newBufferedWriter(sendFileCsv)) {
            writer.write("821HDR1301");
            writer.write(System.getProperty("line.separator"));

            StatefulBeanToCsv<BookVoucherSendDto> beanToCsv =
                    new StatefulBeanToCsvBuilder<BookVoucherSendDto>(writer)
                            .withApplyQuotesToAll(false)
                            .build();
            beanToCsv.write(list);

            writer.write("821TRL1301");
        }
    }

    public void createReport(Path reportCsv, List<BookVoucherReportDto> list) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        try (Writer writer = Files.newBufferedWriter(reportCsv)) {
            StatefulBeanToCsv<BookVoucherReportDto> beanToCsv =
                    new StatefulBeanToCsvBuilder<BookVoucherReportDto>(writer)
                            .withMappingStrategy(reportCsvHeaderStrategy())
                            .withApplyQuotesToAll(false)
                            .build();
            beanToCsv.write(list);
        }
    }

    private HeaderColumnNameMappingStrategy<BookVoucherReportDto> reportCsvHeaderStrategy() throws IOException {
        HeaderColumnNameMappingStrategy<BookVoucherReportDto> strategy =
                new HeaderColumnNameMappingStrategyBuilder<BookVoucherReportDto>().build();
        strategy.setType(BookVoucherReportDto.class);
        try (Reader reader = new BufferedReader(new InputStreamReader(voucherReportCsvHeaderTemplate().getInputStream()))) {
            CsvToBean<BookVoucherReportDto> csvToBean = new CsvToBeanBuilder<BookVoucherReportDto>(reader)
                    .withType(BookVoucherReportDto.class)
                    .withMappingStrategy(strategy)
                    .build();
            csvToBean.parse();
        }
        return strategy;
    }

    private Resource voucherReportCsvHeaderTemplate() {
        return resourceLoader.getResource("classpath:/csv/BookVoucherReportCsvHeaderTemplate.csv");
    }
}
