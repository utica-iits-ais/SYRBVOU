package edu.utica.jobsub.ar.bloc.repositories;

import edu.utica.jobsub.ar.bloc.config.job.BannerJob;
import edu.utica.jobsub.ar.bloc.model.BookVoucher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

@Repository
public class BookVoucherRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired
    public BookVoucherRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, BannerJob bannerJob) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    /**
     * Methods returns list of BookVoucher from database query.
     * @author Michael Stockman
     * @return List BookVouchers
     */
    public List<BookVoucher> findBookVouchers(
            LocalDate transactionFromDate,
            LocalDate transactionToDate,
            String termCode,
            String detailCode
    ) {
        List<BookVoucher> list =
                namedParameterJdbcTemplate.query(
                        """
                        with vouchers as (
                        select tbraccd_pidm pidm
                              ,spriden_id banner_id
                              ,spriden_first_name first_name
                              ,spriden_mi mi
                              ,spriden_last_name last_name
                              ,tbraccd_term_code term_code
                              ,stvterm_desc term_description
                              ,tbraccd_detail_code detail_code
                              ,sum(nvl(tbraccd_amount,0)) amount
                              ,f_get_preferred_email_uc(tbraccd_pidm) pref_email
                              ,sykstur.f_styp(tbraccd_pidm,tbraccd_term_code) styp_code
                          from tbraccd, spriden, stvterm s
                         where tbraccd_pidm = spriden_pidm
                            and spriden_change_ind is null
                            and tbraccd_entry_date between :transactionFromDate and :transactionToDate
                            and stvterm_code = :termCode
                            and tbraccd_term_code = stvterm_code
                            and tbraccd_detail_code = :detailCode
                        group by tbraccd_pidm
                                ,spriden_id
                                ,spriden_first_name
                                ,spriden_mi
                                ,spriden_last_name
                                ,tbraccd_term_code
                                ,stvterm_desc
                                ,tbraccd_detail_code
                        order by spriden_last_name
                                ,spriden_first_name),
                        run as (
                        select decode(count(*),0,'F','I') run_type
                          from syrbvou
                         where syrbvou_term_code = :termCode
                           and syrbvou_detail_code = :detailCode
                           and trunc(syrbvou_run_date) = trunc(sysdate))
                        select pidm
                              ,banner_id
                              ,first_name
                              ,mi
                              ,last_name
                              ,term_code
                              ,term_description
                              ,detail_code
                              ,amount
                              ,pref_email
                              ,styp_code
                              ,run_type
                              ,decode(run_type,'F','N'
                                              ,'I',(select decode(count(*),0,'N','Y')
                                                          from syrbvoe
                                                         where syrbvoe_pidm = pidm
                                                           and syrbvoe_term_code = term_code
                                                           and syrbvoe_provider_code = detail_code
                                                           and syrbvoe_amount = amount)
                                     ) voucher_sent_ind
                          from vouchers,run
                        """,
                        new MapSqlParameterSource()
                                .addValue("transactionFromDate",transactionFromDate)
                                .addValue("transactionToDate",transactionToDate)
                                .addValue("termCode",termCode)
                                .addValue("detailCode",detailCode),
                        new BookVoucherRepositoryRowMapper(transactionFromDate,transactionToDate)
                );
        return list;
    }

    /**
     * Inserts Book Voucher Send run date into tracking table, used to determine Full or Incremental run.
     * In original SYRBVOU SQR program this was stored in a file under SCROMER's directory
     */
    public void insertRun(String termCode, String detailCode) {
        namedParameterJdbcTemplate.update(
                "insert into syrbvou(syrbvou_term_code,syrbvou_detail_code,syrbvou_run_date) values (:termCode,:detailCode,sysdate)",
                new MapSqlParameterSource()
                        .addValue("termCode",termCode)
                        .addValue("detailCode",detailCode)

        );
    }

    /**
     * Check if voucher tracking record already exists
     * @param pidm
     * @param termCode
     * @param detailCode
     * @return
     */
    public boolean voucherTrackingRecordExists(int pidm, String termCode, String detailCode) {
        int voucherTrackingCnt = namedParameterJdbcTemplate.queryForObject(
                """
                select count(*)
                  from syrbvoe
                 where syrbvoe_pidm = :pidm
                   and syrbvoe_term_code = :termCode
                   and syrbvoe_provider_code = :detailCode
                """,
                new MapSqlParameterSource()
                        .addValue("pidm",pidm)
                        .addValue("termCode",termCode)
                        .addValue("detailCode",detailCode),
                Integer.class);
        return voucherTrackingCnt > 0;
    }

    /**
     * Voucher records that are sent are inserted in the SYRBVOE table so only new changes are sent on incremental runs.
     * @author Michael Stockman
     * @param pidm
     * @param termCode
     * @param detailCode
     * @param amount
     */
    public void insertVoucherTracking(int pidm, String termCode, String detailCode, double amount) {
        namedParameterJdbcTemplate.update(
                """
                insert into syrbvoe(syrbvoe_pidm
                                   ,syrbvoe_term_code
                                   ,syrbvoe_provider_code
                                   ,syrbvoe_amount
                                   ,syrbvoe_activity_date)
                             values(:pidm
                                   ,:termCode
                                   ,:detailCode
                                   ,:amount
                                   ,sysdate)
                """,
                new MapSqlParameterSource()
                        .addValue("pidm",pidm)
                        .addValue("termCode",termCode)
                        .addValue("detailCode",detailCode)
                        .addValue("amount",amount)
        );
    }

    /**
     * Voucher records that get sent are updated in SYRBVOE tracking table if they already exist.
     * A slight deviation from the update statement in the orginal SYRBVOU.SQR program.
     * The update only occurs when the amount for the record is different, and only then does the update occur and the activity date is updated.
     * If that particular amount sent already exists, I thought it makes more sense to preserve the activity date it originally had until a new amount value exists.
     * @author Michael Stockman
     * @param pidm
     * @param termCode
     * @param detailCode
     * @param amount
     */
    public void updateVoucherTracking(int pidm, String termCode, String detailCode, double amount) {
        namedParameterJdbcTemplate.update(
                """
                    update syrbvoe 
                       set syrbvoe_amount = :amount
                          ,syrbvoe_activity_date = sysdate
                     where syrbvoe_pidm = :pidm
                       and syrbvoe_term_code = :termCode
                       and syrbvoe_provider_code = :detailCode
                       and syrbvoe_amount <> :amount
                    """,
                new MapSqlParameterSource()
                        .addValue("pidm",pidm)
                        .addValue("termCode",termCode)
                        .addValue("detailCode",detailCode)
                        .addValue("amount",amount)
        );
    }

}
