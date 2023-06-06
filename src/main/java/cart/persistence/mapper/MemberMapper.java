package cart.persistence.mapper;

import static cart.persistence.mapper.MemberCouponMapper.convertMemberCoupons;

import cart.domain.member.EncryptedPassword;
import cart.domain.member.Member;
import cart.persistence.dao.dto.CartItemDto;
import cart.persistence.dao.dto.MemberCouponDto;
import cart.persistence.dao.dto.OrderDto;
import cart.persistence.entity.MemberEntity;
import java.util.List;

public class MemberMapper {

    public static Member convertMember(final MemberEntity memberEntity) {
        return Member.create(memberEntity.getId(), memberEntity.getName(),
            EncryptedPassword.create(memberEntity.getPassword()));
    }

    public static Member convertMember(final OrderDto orderDto) {
        return Member.create(orderDto.getMemberId(), orderDto.getMemberName(),
            EncryptedPassword.create(orderDto.getMemberPassword()));
    }

    public static Member convertMember(final CartItemDto cartItemDto) {
        return Member.create(cartItemDto.getMemberName(), EncryptedPassword.create(cartItemDto.getMemberPassword()));
    }

    public static Member convertMember(final List<MemberCouponDto> myCouponsByName, final MemberEntity memberEntity) {
        return Member.create(memberEntity.getName(), EncryptedPassword.create(memberEntity.getPassword()),
            convertMemberCoupons(myCouponsByName));
    }
}
