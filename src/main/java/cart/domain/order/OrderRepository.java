package cart.domain.order;

import java.util.List;

public interface OrderRepository {

    Long save(final Order order);

    Long saveWithCoupon(final Order order);

    Long countByMemberId(final Long memberId);

    Order getById(final Long id);

    List<Order> findByMemberName(final String memberName);

    void updateNotValidById(final Long id);
}
