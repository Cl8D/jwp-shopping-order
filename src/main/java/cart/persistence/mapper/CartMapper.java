package cart.persistence.mapper;

import static cart.persistence.mapper.ProductMapper.convertProductWithId;

import cart.domain.cartitem.Cart;
import cart.domain.cartitem.CartItemWithId;
import cart.domain.member.Member;
import cart.persistence.dao.dto.CartItemDto;
import cart.persistence.entity.MemberEntity;
import java.util.List;
import java.util.stream.Collectors;

public class CartMapper {

    public static Cart convertCart(final CartItemDto cartItemDto) {
        final Member member = MemberMapper.convertMember(cartItemDto);
        final CartItemWithId cartItemWithId = convertCartItemWithId(cartItemDto);
        return new Cart(member, List.of(cartItemWithId));
    }

    public static Cart convertCart(List<CartItemDto> carItems, final MemberEntity memberEntity) {
        final Member member = MemberMapper.convertMember(memberEntity);
        final List<CartItemWithId> cartItemWithIds = carItems.stream()
            .map(CartMapper::convertCartItemWithId)
            .collect(Collectors.toUnmodifiableList());
        return new Cart(member, cartItemWithIds);
    }

    private static CartItemWithId convertCartItemWithId(final CartItemDto cartItemDto) {
        return new CartItemWithId(cartItemDto.getCartId(), cartItemDto.getProductQuantity(),
            convertProductWithId(cartItemDto));
    }
}
