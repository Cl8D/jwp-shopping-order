package cart.persistence.entity;

public class OrderCouponEntity {

    private final Long id;
    private final Long orderId;
    private final Long couponId;

    public OrderCouponEntity(final Long orderId, final Long couponId) {
        this(null, orderId, couponId);
    }

    public OrderCouponEntity(final Long id, final Long orderId, final Long couponId) {
        this.id = id;
        this.orderId = orderId;
        this.couponId = couponId;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getCouponId() {
        return couponId;
    }
}
