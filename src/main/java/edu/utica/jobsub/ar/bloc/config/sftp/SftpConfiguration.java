package edu.utica.jobsub.ar.bloc.config.sftp;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.apache.sshd.common.signature.BuiltinSignatures;
import org.apache.sshd.common.signature.Signature;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SftpConfiguration {
    private String host;
    private int port;
    private String user;
    private Resource privateKey;

    @Autowired
    public SftpConfiguration(
            @Value("${sftp.host}") String host,
            @Value("${sftp.port}") int port,
            @Value("${sftp.user}") String user,
            @Value("${sftp.privateKey}") Resource privateKey
    ) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.privateKey = privateKey;
    }

    @Bean
    // This externally provided sshClient bean is only needed because the Follett server presents and uses the deprecated DSA key
    // The version of OpenSSH used by Mina SSHD SFTP doesn't like this and the signature needs to be manually configured
    // Which means the SshClient needs to be setup and passed to the Spring DefaultSftpSessionFactory
    // Without that requirement you could just setup the SessionFactory bean
    public SshClient sshClient() throws Exception {

        SshClient client = SshClient.setUpDefaultClient();
        // Load the keyPairs from the resource and set the key identity provider
        KeyPairResourceLoader keyPairResourceLoader = SecurityUtils.getKeyPairResourceParser();
        Iterable<KeyPair> keyPairs = keyPairResourceLoader.loadKeyPairs(null,privateKey.getURL(),null);
        client.setKeyIdentityProvider(KeyIdentityProvider.wrapKeyPairs(keyPairs));

        // Need to manually add the DSA key signature since it is deprecated by OpenSsh used in this library but is being presented by Follett sftp server. This is similar to -o HostKeyAlgorithms=+ssh-dss
        List<NamedFactory<Signature>> signatureFactories = client.getSignatureFactories();
        List<BuiltinSignatures> signatures = new ArrayList<>();
        signatures.add(BuiltinSignatures.dsa);
        signatureFactories.addAll(NamedFactory.setUpBuiltinFactories(false,signatures));

        return client;
    }

    @Bean
    public SessionFactory<SftpClient.DirEntry> sftpSessionFactory() throws Exception {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(sshClient(),true);
        factory.setHost(host);
        factory.setPort(port);
        factory.setUser(user);
        return new CachingSessionFactory(factory);
    }
    @Bean
    public SftpRemoteFileTemplate getSftpRemoteFileTemplate() throws Exception {
        return new SftpRemoteFileTemplate(sftpSessionFactory());
    }


}
