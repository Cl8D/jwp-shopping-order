package cart.persistence.mapper;

import cart.domain.coupon.Coupon;
import cart.domain.coupon.dto.CouponWithId;
import cart.persistence.dao.dto.MemberCouponDto;
import cart.persistence.entity.CouponEntity;

public class CouponMapper {

    public static CouponWithId convertCouponWithId(final CouponEntity couponEntity) {
        return new CouponWithId(couponEntity.getId(), convertCoupon(couponEntity));
    }

    public static Coupon convertCoupon(final CouponEntity couponEntity) {
        return Coupon.create(couponEntity.getName(),
            couponEntity.getDiscountRate(), couponEntity.getPeriod(), couponEntity.getExpiredAt());
    }

    public static Coupon convertCoupon(final MemberCouponDto memberCouponDto) {
        return Coupon.create(memberCouponDto.getCouponName(), memberCouponDto.getDiscountRate(),
            memberCouponDto.getCouponPeriod(), memberCouponDto.getExpiredAt());
    }

    public static CouponEntity convertCouponEntity(final Coupon coupon) {
        return new CouponEntity(coupon.name(), coupon.discountRate(), coupon.period(), coupon.expiredAt());
    }
}
