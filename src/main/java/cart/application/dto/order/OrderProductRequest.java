package cart.application.dto.order;

public class OrderProductRequest {

    private final Long productId;
    private final Integer quantity;

    public OrderProductRequest() {
        this(null, null);
    }

    public OrderProductRequest(final Long productId, final Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
