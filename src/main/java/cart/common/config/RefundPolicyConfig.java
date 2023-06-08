package cart.common.config;

import cart.domain.refund.RefundPolicy;
import cart.domain.refund.RefundPolicyComposite;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RefundPolicyConfig {

    @Bean
    public RefundPolicyComposite refundPolicyComposite(final List<RefundPolicy> refundPolicies) {
        return new RefundPolicyComposite(refundPolicies);
    }
}
