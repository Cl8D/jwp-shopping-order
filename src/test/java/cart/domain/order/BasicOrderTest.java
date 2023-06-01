package cart.domain.order;

import static org.assertj.core.api.Assertions.assertThat;

import cart.domain.cartitem.CartItemWithId;
import cart.domain.member.EncryptedPassword;
import cart.domain.member.Member;
import cart.domain.member.dto.MemberWithId;
import cart.domain.product.Product;
import cart.domain.product.dto.ProductWithId;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BasicOrderTest {

    @Test
    @DisplayName("총 주문 금액을 계산하여 반환한다.")
    void getTotalPrice() {
        // given
        final MemberWithId 져니 = new MemberWithId(1L,
            Member.create("journey", EncryptedPassword.create("password")));
        final CartItemWithId 치킨_장바구니_아이템 = new CartItemWithId(1L, 10, new ProductWithId(1L,
            new Product("치킨", 20000, "chicken_image_url", false)));
        final CartItemWithId 피자_장바구니_아이템 = new CartItemWithId(2L, 5, new ProductWithId(2L,
            new Product("피자", 30000, "pizza_image_url", false)));
        final BasicOrder 주문 = new BasicOrder(져니, 3000, LocalDateTime.now(), List.of(치킨_장바구니_아이템, 피자_장바구니_아이템));

        // when
        final Integer 총_금액 = 주문.getTotalPrice();

        // then
        assertThat(총_금액)
            .isEqualTo(350_000);
    }

    @Test
    @DisplayName("할인된 총 주문 금액을 계산하여 반환한다.")
    void getDiscountedTotalPrice() {
        // given
        final MemberWithId 져니 = new MemberWithId(1L,
            Member.create("journey", EncryptedPassword.create("password")));
        final CartItemWithId 치킨_장바구니_아이템 = new CartItemWithId(1L, 10, new ProductWithId(1L,
            new Product("치킨", 20000, "chicken_image_url", false)));
        final CartItemWithId 피자_장바구니_아이템 = new CartItemWithId(2L, 5, new ProductWithId(2L,
            new Product("피자", 30000, "pizza_image_url", false)));
        final BasicOrder 주문 = new BasicOrder(져니, 3000, LocalDateTime.now(), List.of(치킨_장바구니_아이템, 피자_장바구니_아이템));

        // when
        final Integer 할인된_금액 = 주문.getDiscountedTotalPrice();

        // then
        assertThat(할인된_금액)
            .isEqualTo(350_000);
    }
}
