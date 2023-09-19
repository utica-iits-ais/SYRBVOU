package edu.utica.jobsub.ar.bloc.sftp;

import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.file.remote.ClientCallbackWithoutResult;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

@Component
@Slf4j
public class BookVoucherSftp {
    private final SftpRemoteFileTemplate sftpRemoteFileTemplate;
    @Autowired
    public BookVoucherSftp(SftpRemoteFileTemplate sftpRemoteFileTemplate) {
        this.sftpRemoteFileTemplate = sftpRemoteFileTemplate;
    }
    public void uploadVoucherSendFile(Path csv) {
        sftpRemoteFileTemplate.executeWithClient((ClientCallbackWithoutResult<SftpClient>) sftp -> {
           try (SftpClient.CloseableHandle handle = sftp.open("DropOff/" + csv.getFileName().toString(), EnumSet.of(SftpClient.OpenMode.Write, SftpClient.OpenMode.Create))) {
               sftp.write(handle,0L, Files.readAllBytes(csv));
           } catch (IOException ioException) {
               log.error("Error writing CSV file to SFTP server.",ioException);
               throw new RuntimeException("Error writing CSV file to SFTP server.",ioException);
           }
        });
    }
}
