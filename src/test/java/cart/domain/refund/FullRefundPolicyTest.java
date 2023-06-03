package cart.domain.refund;

import static org.assertj.core.api.Assertions.assertThat;

import cart.domain.cartitem.dto.CartItemWithId;
import cart.domain.member.EncryptedPassword;
import cart.domain.member.Member;
import cart.domain.member.dto.MemberWithId;
import cart.domain.order.BasicOrder;
import cart.domain.product.Product;
import cart.domain.product.dto.ProductWithId;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FullRefundPolicyTest {

    private FullRefundPolicy fullRefundPolicy;

    @BeforeEach
    void setUp() {
        fullRefundPolicy = new FullRefundPolicy();
    }

    @ParameterizedTest(name = "주문한 지 3일 이내인지 확인한다.")
    @CsvSource(value = {"0:true", "2:true", "3:false", "4:false"}, delimiter = ':')
    void isAvailable(final int day, final boolean expected) {
        // given
        final MemberWithId 져니 = new MemberWithId(1L,
            Member.create("journey", EncryptedPassword.create("password")));
        final CartItemWithId 치킨_장바구니_아이템 = new CartItemWithId(1L, 10, new ProductWithId(1L,
            new Product("치킨", 20000, "chicken_image_url", false)));
        final CartItemWithId 피자_장바구니_아이템 = new CartItemWithId(2L, 5, new ProductWithId(2L,
            new Product("피자", 30000, "pizza_image_url", false)));
        final BasicOrder 주문 = new BasicOrder(져니, 3000, LocalDateTime.now().minusDays(day),
            List.of(치킨_장바구니_아이템, 피자_장바구니_아이템), true);
        final LocalDateTime currentTime = LocalDateTime.now();

        // when
        final boolean result = fullRefundPolicy.isAvailable(주문, currentTime);

        // then
        Assertions.assertThat(result)
            .isSameAs(expected);
    }

    @Test
    @DisplayName("할인 금액을 전체 금액 그대로 반환한다.")
    void calculatePrice() {
        // when
        final int result = fullRefundPolicy.calculatePrice(10000);

        // then
        assertThat(result)
            .isEqualTo(10000);
    }
}
