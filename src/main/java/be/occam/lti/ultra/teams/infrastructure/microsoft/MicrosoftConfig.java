package be.occam.lti.ultra.teams.infrastructure.microsoft;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MicrosoftConfig {

    @Bean
    public GraphServiceClient graphServiceClient(MicrosoftProperties microsoftProperties) {
        final ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(microsoftProperties.clientId()).tenantId(microsoftProperties.tenantId()).clientSecret(microsoftProperties.clientSecret()).build();

        if (null == microsoftProperties.scopes() || null == credential) {
            throw new RuntimeException("Unexpected error");
        }

        final GraphServiceClient graphClient = new GraphServiceClient(credential, microsoftProperties.scopes());
        return new GraphServiceClient(credential);
    }
}
