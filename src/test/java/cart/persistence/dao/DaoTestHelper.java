package cart.persistence.dao;

import cart.persistence.entity.CouponEntity;
import cart.persistence.entity.MemberCouponEntity;
import cart.persistence.entity.MemberEntity;
import cart.persistence.entity.OrderEntity;
import cart.persistence.entity.ProductEntity;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({OrderDao.class, MemberDao.class, CouponDao.class, MemberCouponDao.class, ProductDao.class})
public class DaoTestHelper extends DaoTest {

    @Autowired
    OrderDao orderDao;
    @Autowired
    MemberDao memberDao;
    @Autowired
    CouponDao couponDao;
    @Autowired
    MemberCouponDao memberCouponDao;
    @Autowired
    ProductDao productDao;

    Long 치킨_저장() {
        final ProductEntity 치킨 = new ProductEntity("치킨", "chicken_image_url", 20000);
        return productDao.insert(치킨);
    }

    Long 피자_저장() {
        final ProductEntity 피자 = new ProductEntity("피자", "pizza_image_url", 30000);
        return productDao.insert(피자);
    }


    Long 져니_저장() {
        final MemberEntity 져니 = new MemberEntity("journey", "password");
        return memberDao.insert(져니);
    }

    Long 신규_가입_쿠폰_저장() {
        final LocalDateTime now = LocalDateTime.now();
        final CouponEntity 신규_가입_축하_쿠폰 = new CouponEntity("신규 가입 축하 쿠폰", 20, 10, now.plusDays(10));
        return couponDao.insert(신규_가입_축하_쿠폰);
    }

    Long 져니_쿠폰_저장(final long 저장된_져니_아이디, final Long 저장된_신규_가입_축하_쿠폰_아이디) {
        final LocalDateTime 쿠폰_발급_시간 = LocalDateTime.now();
        final LocalDateTime 쿠폰_만료_시간 = 쿠폰_발급_시간.plusDays(10);
        final MemberCouponEntity 사용자_쿠폰_저장_엔티티 = new MemberCouponEntity(저장된_져니_아이디, 저장된_신규_가입_축하_쿠폰_아이디,
            쿠폰_발급_시간, 쿠폰_만료_시간, false);
        return memberCouponDao.insert(사용자_쿠폰_저장_엔티티);
    }

    Long 주문_저장(final Long 저장된_져니_아이디) {
        final OrderEntity 주문_엔티티 = new OrderEntity(저장된_져니_아이디, 10000, 9000, 3000, LocalDateTime.now());
        return orderDao.insert(주문_엔티티);
    }
}
