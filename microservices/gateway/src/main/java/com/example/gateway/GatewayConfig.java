import org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RewritePathGatewayFilterFactory myRewritePathFilter() {
        return new RewritePathGatewayFilterFactory();
    }
}