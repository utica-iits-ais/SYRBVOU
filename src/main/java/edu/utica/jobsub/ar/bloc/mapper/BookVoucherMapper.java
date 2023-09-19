package edu.utica.jobsub.ar.bloc.mapper;

import edu.utica.jobsub.ar.bloc.model.BookVoucher;
import edu.utica.jobsub.ar.bloc.model.BookVoucherReportDto;
import edu.utica.jobsub.ar.bloc.model.BookVoucherSendDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookVoucherMapper {

    @Mapping(target = "providerCode",expression = "java(bookVoucher.getDetailCode() + bookVoucher.getTermCode())")
    @Mapping(target = "dummyPosition1",ignore = true)
    @Mapping(target = "dummyPosition2",ignore = true)
    BookVoucherSendDto bookVoucherToBookVoucherSendDto(BookVoucher bookVoucher);
    BookVoucherReportDto bookVoucherToBookVoucherReportDto(BookVoucher bookVoucher);
}
