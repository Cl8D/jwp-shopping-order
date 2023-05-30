package cart.persistence.entity;

import java.time.LocalDateTime;

public class MemberCouponEntity {

    private final Long memberId;
    private final Long couponId;
    private final LocalDateTime issuedDate;
    private final LocalDateTime expiredDate;

    public MemberCouponEntity(final Long memberId, final Long couponId, final LocalDateTime issuedDate,
                              final LocalDateTime expiredDate) {
        this.memberId = memberId;
        this.couponId = couponId;
        this.issuedDate = issuedDate;
        this.expiredDate = expiredDate;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getCouponId() {
        return couponId;
    }

    public LocalDateTime getIssuedDate() {
        return issuedDate;
    }

    public LocalDateTime getExpiredDate() {
        return expiredDate;
    }
}
