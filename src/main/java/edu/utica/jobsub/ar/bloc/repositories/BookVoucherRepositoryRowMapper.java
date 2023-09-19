package edu.utica.jobsub.ar.bloc.repositories;

import edu.utica.jobsub.ar.bloc.model.BookVoucher;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class BookVoucherRepositoryRowMapper implements RowMapper<BookVoucher> {

    private final LocalDate transactionFromDate;
    private final LocalDate transactionToDate;

    public BookVoucherRepositoryRowMapper(LocalDate transactionFromDate, LocalDate transactionToDate) {
        this.transactionFromDate = transactionFromDate;
        this.transactionToDate = transactionToDate;
    }

    @Override
    public BookVoucher mapRow(ResultSet rs, int rowNum) throws SQLException {
        BookVoucher bookVoucher = new BookVoucher();
        bookVoucher.setPidm(rs.getInt("pidm"));
        bookVoucher.setBannerId(rs.getString("banner_id"));
        bookVoucher.setFirstName(rs.getString("first_name"));
        bookVoucher.setMi(rs.getString("mi"));
        bookVoucher.setLastName(rs.getString("last_name"));
        bookVoucher.setTermCode(rs.getString("term_code"));
        bookVoucher.setTermDescription(rs.getString("term_description"));
        bookVoucher.setDetailCode(rs.getString("detail_code"));
        bookVoucher.setAmount(rs.getDouble("amount"));
        bookVoucher.setPrefEmail(rs.getString("pref_email"));
        bookVoucher.setStypCode(rs.getString("styp_code"));
        bookVoucher.setRunType(rs.getString("run_type"));
        bookVoucher.setVoucherSentInd(rs.getString("voucher_sent_ind"));
        bookVoucher.setTransactionFromDate(transactionFromDate);
        bookVoucher.setTransactionToDate(transactionToDate);
        return bookVoucher;
    }

}
