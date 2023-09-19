package edu.utica.jobsub.ar.bloc.services;

import edu.utica.jobsub.ar.bloc.config.job.BannerJob;
import edu.utica.jobsub.ar.bloc.model.BookVoucher;
import edu.utica.jobsub.ar.bloc.repositories.BookVoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookVoucherService {

    private final BookVoucherRepository bookVoucherRepository;
    private final BannerJob bannerJob;

    @Autowired
    public BookVoucherService(BookVoucherRepository bookVoucherRepository, BannerJob bannerJob) {
        this.bookVoucherRepository = bookVoucherRepository;
        this.bannerJob = bannerJob;
    }

    public List<BookVoucher> findBookVouchers() {
        LocalDate transactionFromDate = LocalDate.parse(bannerJob.getParameters().get("01").toString(),bannerJob.getParameterDateTimeFormatter());
        LocalDate transactionToDate = LocalDate.parse(bannerJob.getParameters().get("02").toString(),bannerJob.getParameterDateTimeFormatter());
        String termCode = bannerJob.getParameters().get("03").toString();
        String detailCode = bannerJob.getParameters().get("04").toString();
        return bookVoucherRepository.findBookVouchers(
                transactionFromDate,
                transactionToDate,
                termCode,
                detailCode);
    }

    @Transactional
    public void insertRun() {
        String termCode = bannerJob.getParameters().get("03").toString();
        String detailCode = bannerJob.getParameters().get("04").toString();
        bookVoucherRepository.insertRun(termCode,detailCode);
    }

    @Transactional
    public void saveVoucherTracking(BookVoucher b) {
        if (bookVoucherRepository.voucherTrackingRecordExists(b.getPidm(),b.getTermCode(),b.getDetailCode())) {
            bookVoucherRepository.updateVoucherTracking(b.getPidm(),b.getTermCode(),b.getDetailCode(),b.getAmount());
            return;
        }
        bookVoucherRepository.insertVoucherTracking(b.getPidm(),b.getTermCode(),b.getDetailCode(),b.getAmount());
    }

}
