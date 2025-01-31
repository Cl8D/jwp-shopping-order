package cart.integration;

import static cart.exception.ErrorCode.COUPON_ALREADY_USED;
import static cart.exception.ErrorCode.COUPON_EXPIRED;
import static cart.exception.ErrorCode.FORBIDDEN;
import static cart.exception.ErrorCode.ORDER_INVALID_PRODUCTS;
import static cart.exception.ErrorCode.ORDER_QUANTITY_EXCEED;
import static cart.exception.ErrorCode.PRODUCT_DELETED;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import cart.application.dto.cartitem.CartRequest;
import cart.application.dto.coupon.CouponRequest;
import cart.application.dto.member.MemberJoinRequest;
import cart.application.dto.member.MemberLoginRequest;
import cart.application.dto.order.OrderProductRequest;
import cart.application.dto.order.OrderRequest;
import cart.application.dto.product.ProductRequest;
import cart.exception.ErrorCode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

public class OrderIntegrationTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("쿠폰을 포함하지 않은 상품을 주문한다.")
    void orderProducts() {
        // given
        쿠폰을_저장한다();
        상품을_저장한다();
        사용자를_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니에_상품을_추가한다(져니_로그인_요청);

        /** 상품 주문 요청 */
        final List<OrderProductRequest> 주문할_상품들 = List.of(
            new OrderProductRequest(1L, 10),
            new OrderProductRequest(2L, 5));
        final OrderRequest 주문_요청 = new OrderRequest(null, 주문할_상품들);

        // when
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(주문_요청)
            .post("/orders")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .header(LOCATION, "/orders/" + 1);

        // then
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .get("/orders/{id}", 1)
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("orderId", equalTo(1))
            .body("totalPrice", equalTo(175_000))
            .body("discountedTotalPrice", equalTo(175_000))
            .body("couponDiscountPrice", equalTo(0))
            .body("deliveryPrice", equalTo(3_000))
            .body("coupon.id", equalTo(null))
            .body("coupon.name", equalTo(null))
            .body("coupon.discountRate", equalTo(null))
            .body("items[0].quantity", equalTo(10))
            .body("items[0].product.id", equalTo(1))
            .body("items[0].product.name", equalTo("치킨"))
            .body("items[0].product.price", equalTo(10_000))
            .body("items[0].product.imageUrl", equalTo("http://example.com/chicken.jpg"))
            .body("items[1].quantity", equalTo(5))
            .body("items[1].product.id", equalTo(2))
            .body("items[1].product.name", equalTo("피자"))
            .body("items[1].product.price", equalTo(15_000))
            .body("items[1].product.imageUrl", equalTo("http://example.com/pizza.jpg"))
            .body("isValid", equalTo(true));

        /** 첫 주문 감사 쿠폰이 발급되었는지 확인한다. */
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .get("/users/me/coupons")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("size", is(2))
            .body("[0].id", equalTo(1))
            .body("[0].name", equalTo("신규 가입 축하 쿠폰"))
            .body("[0].discountRate", equalTo(20))
            .body("[0].isUsed", equalTo(false))
            .body("[1].id", equalTo(2))
            .body("[1].name", equalTo("첫 주문 감사 쿠폰"))
            .body("[1].discountRate", equalTo(10))
            .body("[1].isUsed", equalTo(false));
    }

    @Test
    @DisplayName("쿠폰을 포함한 상품을 주문한다.")
    void orderProducts_with_coupons() {
        // given
        쿠폰을_저장한다();
        상품을_저장한다();
        사용자를_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니에_상품을_추가한다(져니_로그인_요청);

        /** 상품 주문 요청 */
        final List<OrderProductRequest> 주문할_상품들 = List.of(
            new OrderProductRequest(1L, 10),
            new OrderProductRequest(2L, 5));
        final OrderRequest 주문_요청 = new OrderRequest(1L, 주문할_상품들);

        // when
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(주문_요청)
            .post("/orders")
            .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value())
            .header(LOCATION, "/orders/" + 1);

        // then
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .get("/orders/{id}", 1)
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("orderId", equalTo(1))
            .body("totalPrice", equalTo(175_000))
            .body("discountedTotalPrice", equalTo(140_000))
            .body("couponDiscountPrice", equalTo(35_000))
            .body("deliveryPrice", equalTo(3_000))
            .body("coupon.id", equalTo(1))
            .body("coupon.name", equalTo("신규 가입 축하 쿠폰"))
            .body("coupon.discountRate", equalTo(20))
            .body("items[0].quantity", equalTo(10))
            .body("items[0].product.id", equalTo(1))
            .body("items[0].product.name", equalTo("치킨"))
            .body("items[0].product.price", equalTo(10_000))
            .body("items[0].product.imageUrl", equalTo("http://example.com/chicken.jpg"))
            .body("items[1].quantity", equalTo(5))
            .body("items[1].product.id", equalTo(2))
            .body("items[1].product.name", equalTo("피자"))
            .body("items[1].product.price", equalTo(15_000))
            .body("items[1].product.imageUrl", equalTo("http://example.com/pizza.jpg"))
            .body("isValid", equalTo(true));

        /** 첫 주문 감사 쿠폰이 발급되었는지 확인한다. */
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .get("/users/me/coupons")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("size", is(2))
            .body("[0].id", equalTo(1))
            .body("[0].name", equalTo("신규 가입 축하 쿠폰"))
            .body("[0].discountRate", equalTo(20))
            .body("[0].isUsed", equalTo(true))
            .body("[1].id", equalTo(2))
            .body("[1].name", equalTo("첫 주문 감사 쿠폰"))
            .body("[1].discountRate", equalTo(10))
            .body("[1].isUsed", equalTo(false));
    }

    @Test
    @DisplayName("쿠폰을 포함한 상품 주문 시, 만료된 쿠폰이라면 예외가 발생한다.")
    void orderProducts_with_expired_coupons() {
        // given
        쿠폰을_저장한다();
        상품을_저장한다();
        사용자를_저장한다();
        만료된_쿠폰을_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니에_상품을_추가한다(져니_로그인_요청);

        /** 상품 주문 요청 */
        final List<OrderProductRequest> 주문할_상품들 = List.of(
            new OrderProductRequest(1L, 10),
            new OrderProductRequest(2L, 5));
        final OrderRequest 주문_요청 = new OrderRequest(3L, 주문할_상품들);

        // when
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(주문_요청)
            .post("/orders")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errorCode", equalTo(COUPON_EXPIRED.name()))
            .body("errorMessage", equalTo("만료된 쿠폰입니다."));
    }

    @Test
    @DisplayName("쿠폰을 포함한 상품 주문 시, 이미 사용한 쿠폰이면 예외가 발생한다.")
    void orderProducts_with_already_used_coupon() {
        // given
        쿠폰을_저장한다();
        상품을_저장한다();
        사용자를_저장한다();
        이미_사용한_쿠폰을_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니에_상품을_추가한다(져니_로그인_요청);

        /** 상품 주문 요청 */
        final List<OrderProductRequest> 주문할_상품들 = List.of(
            new OrderProductRequest(1L, 10),
            new OrderProductRequest(2L, 5));
        final OrderRequest 주문_요청 = new OrderRequest(3L, 주문할_상품들);

        // when
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(주문_요청)
            .post("/orders")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errorCode", equalTo(COUPON_ALREADY_USED.name()))
            .body("errorMessage", equalTo("이미 사용한 쿠폰입니다."));
    }

    @Test
    @DisplayName("주문하는 상품의 총 개수가 100,000개 초과면 예외가 발생한다.")
    void orderProducts_exceed_max_quantity() {
        // given
        쿠폰을_저장한다();
        상품을_저장한다();
        사용자를_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니에_상품을_추가한다(져니_로그인_요청);

        /** 상품 주문 요청 */
        final List<OrderProductRequest> 주문할_상품들 = List.of(
            new OrderProductRequest(1L, 100_000),
            new OrderProductRequest(2L, 1));
        final OrderRequest 주문_요청 = new OrderRequest(1L, 주문할_상품들);

        // expected
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(주문_요청)
            .post("/orders")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errorCode", equalTo(ORDER_QUANTITY_EXCEED.name()))
            .body("errorMessage", equalTo("상품은 최대 100,000개까지 주문할 수 있습니다."));
    }

    @Test
    @DisplayName("장바구니에 담기지 않은 상품을 주문하려고 하면 예외가 발생한다.")
    void orderProducts_invalid_products() {
        // given
        쿠폰을_저장한다();
        사용자를_저장한다();
        상품을_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니에_상품을_추가한다(져니_로그인_요청);

        /** 상품 주문 요청 */
        final List<OrderProductRequest> 주문할_상품들 = List.of(
            new OrderProductRequest(2L, 10),
            new OrderProductRequest(3L, 5));
        final OrderRequest 주문_요청 = new OrderRequest(1L, 주문할_상품들);

        // expected
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(주문_요청)
            .post("/orders")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errorCode", equalTo(ORDER_INVALID_PRODUCTS.name()))
            .body("errorMessage", equalTo("장바구니에 담기지 않은 상품은 주문할 수 없습니다."));
    }

    @Test
    @DisplayName("제거된 상품을 주문하려고 하면 예외가 발생한다.")
    void orderProducts_deleted_product() {
        // given
        쿠폰을_저장한다();
        사용자를_저장한다();
        상품을_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니에_상품을_추가한다(져니_로그인_요청);
        상품을_제거한다();

        /** 상품 주문 요청 */
        final List<OrderProductRequest> 주문할_상품들 = List.of(
            new OrderProductRequest(1L, 10),
            new OrderProductRequest(2L, 5));
        final OrderRequest 주문_요청 = new OrderRequest(1L, 주문할_상품들);

        // expected
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(주문_요청)
            .post("/orders")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errorCode", equalTo(PRODUCT_DELETED.name()))
            .body("errorMessage", equalTo("현재 판매 중이지 않은 상품은 주문할 수 없습니다."));
    }

    @Test
    @DisplayName("특정 주문 정보를 조회한다.")
    void getOrderById() {
        // given
        쿠폰을_저장한다();
        상품을_저장한다();
        사용자를_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니에_상품을_추가한다(져니_로그인_요청);
        쿠폰과_함께_상품을_주문한다(져니_로그인_요청);

        // expected
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .get("/orders/{id}", 1)
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("orderId", equalTo(1))
            .body("totalPrice", equalTo(175_000))
            .body("discountedTotalPrice", equalTo(140_000))
            .body("couponDiscountPrice", equalTo(35_000))
            .body("deliveryPrice", equalTo(3_000))
            .body("isValid", equalTo(true))
            .body("coupon.id", equalTo(1))
            .body("coupon.name", equalTo("신규 가입 축하 쿠폰"))
            .body("coupon.discountRate", equalTo(20))
            .body("items[0].quantity", equalTo(10))
            .body("items[0].product.id", equalTo(1))
            .body("items[0].product.name", equalTo("치킨"))
            .body("items[0].product.price", equalTo(10_000))
            .body("items[0].product.imageUrl", equalTo("http://example.com/chicken.jpg"))
            .body("items[1].quantity", equalTo(5))
            .body("items[1].product.id", equalTo(2))
            .body("items[1].product.name", equalTo("피자"))
            .body("items[1].product.price", equalTo(15_000))
            .body("items[1].product.imageUrl", equalTo("http://example.com/pizza.jpg"));
    }

    @Test
    @DisplayName("특정 주문 정보가 요청받은 사용자의 소유가 아니라면 예외가 발생한다.")
    void getOrderById_forbidden() {
        // given
        쿠폰을_저장한다();
        상품을_저장한다();
        사용자를_저장한다();
        final MemberJoinRequest 라온_등록_요청 = new MemberJoinRequest("raon", "jourzura2");
        사용자_저장(라온_등록_요청);

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니에_상품을_추가한다(져니_로그인_요청);
        쿠폰과_함께_상품을_주문한다(져니_로그인_요청);
        final MemberLoginRequest 라온_로그인_요청 = new MemberLoginRequest(라온_등록_요청.getName(), 라온_등록_요청.getPassword());

        // expected
        given()
            .auth().preemptive().basic(라온_로그인_요청.getName(), 라온_로그인_요청.getPassword())
            .when()
            .get("/orders/{id}", 1)
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .body("errorCode", equalTo(FORBIDDEN.name()))
            .body("errorMessage", equalTo("권한이 없습니다."));
    }

    @Test
    @DisplayName("사용자의 전체 주문 정보를 조회한다.")
    void getOrders() {
        // given
        쿠폰을_저장한다();
        상품을_저장한다();
        사용자를_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니에_상품을_추가한다(져니_로그인_요청);
        쿠폰과_함께_상품을_주문한다(져니_로그인_요청);

        장바구니에_상품을_추가한다(져니_로그인_요청);
        쿠폰없이_상품을_주문한다(져니_로그인_요청);

        // expected
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .get("/orders")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            /** 첫 번째 주문 검증 */
            .body("[0].orderId", equalTo(1))
            .body("[0].totalPrice", equalTo(175_000))
            .body("[0].discountedTotalPrice", equalTo(140_000))
            .body("[0].couponDiscountPrice", equalTo(35_000))
            .body("[0].deliveryPrice", equalTo(3_000))
            .body("[0].isValid", equalTo(true))
            .body("[0].coupon.id", equalTo(1))
            .body("[0].coupon.name", equalTo("신규 가입 축하 쿠폰"))
            .body("[0].coupon.discountRate", equalTo(20))
            .body("[0].items[0].quantity", equalTo(10))
            .body("[0].items[0].product.id", equalTo(1))
            .body("[0].items[0].product.name", equalTo("치킨"))
            .body("[0].items[0].product.price", equalTo(10_000))
            .body("[0].items[0].product.imageUrl", equalTo("http://example.com/chicken.jpg"))
            .body("[0].items[1].quantity", equalTo(5))
            .body("[0].items[1].product.id", equalTo(2))
            .body("[0].items[1].product.name", equalTo("피자"))
            .body("[0].items[1].product.price", equalTo(15_000))
            .body("[0].items[1].product.imageUrl", equalTo("http://example.com/pizza.jpg"))
            /** 두 번째 주문 검증 */
            .body("[1].orderId", equalTo(2))
            .body("[1].totalPrice", equalTo(175_000))
            .body("[1].discountedTotalPrice", equalTo(175_000))
            .body("[1].couponDiscountPrice", equalTo(0))
            .body("[1].deliveryPrice", equalTo(3_000))
            .body("[1].isValid", equalTo(true))
            .body("[1].coupon.id", equalTo(null))
            .body("[1].coupon.name", equalTo(null))
            .body("[1].coupon.discountRate", equalTo(null))
            .body("[1].items[0].quantity", equalTo(10))
            .body("[1].items[0].product.id", equalTo(1))
            .body("[1].items[0].product.name", equalTo("치킨"))
            .body("[1].items[0].product.price", equalTo(10_000))
            .body("[1].items[0].product.imageUrl", equalTo("http://example.com/chicken.jpg"))
            .body("[1].items[1].quantity", equalTo(5))
            .body("[1].items[1].product.id", equalTo(2))
            .body("[1].items[1].product.name", equalTo("피자"))
            .body("[1].items[1].product.price", equalTo(15_000))
            .body("[1].items[1].product.imageUrl", equalTo("http://example.com/pizza.jpg"));
    }

    @Test
    @DisplayName("주문한 지 3일 이내에 주문을 취소하면 전액 환불된다.")
    void cancelOrder_before_3day() {
        // given
        쿠폰을_저장한다();
        상품을_저장한다();
        사용자를_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니에_상품을_추가한다(져니_로그인_요청);
        final LocalDateTime orderTime = LocalDateTime.now().minusDays(2);
        특정_시간에_쿠폰과_함께_상품을_주문한다(orderTime);

        // when
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .post("/orders/cancel/{id}", 1)
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("refundPrice", equalTo(143_000));

        // then
        /** 주문 상태가 취소인지 확인한다 */
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .get("/orders/{id}", 1)
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("orderId", equalTo(1))
            .body("totalPrice", equalTo(175_000))
            .body("discountedTotalPrice", equalTo(140_000))
            .body("couponDiscountPrice", equalTo(35_000))
            .body("deliveryPrice", equalTo(3_000))
            .body("isValid", equalTo(false))
            .body("coupon.id", equalTo(1))
            .body("coupon.name", equalTo("신규 가입 축하 쿠폰"))
            .body("coupon.discountRate", equalTo(20))
            .body("items[0].quantity", equalTo(10))
            .body("items[0].product.id", equalTo(1))
            .body("items[0].product.name", equalTo("치킨"))
            .body("items[0].product.price", equalTo(10_000))
            .body("items[0].product.imageUrl", equalTo("http://example.com/chicken.jpg"))
            .body("items[1].quantity", equalTo(5))
            .body("items[1].product.id", equalTo(2))
            .body("items[1].product.name", equalTo("피자"))
            .body("items[1].product.price", equalTo(15_000))
            .body("items[1].product.imageUrl", equalTo("http://example.com/pizza.jpg"));

        /** 쿠폰이 사용 가능한 상태로 변경되었는지 확인한다 */
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .get("/users/me/coupons")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("size", is(1))
            .body("[0].id", equalTo(1))
            .body("[0].name", equalTo("신규 가입 축하 쿠폰"))
            .body("[0].discountRate", equalTo(20))
            .body("[0].isUsed", equalTo(false));
    }

    @Test
    @DisplayName("주문한 지 7일 이내에 주문을 취소하면 반액 환불된다.")
    void cancelOrder_before_7day() {
        // given
        쿠폰을_저장한다();
        상품을_저장한다();
        사용자를_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니에_상품을_추가한다(져니_로그인_요청);
        final LocalDateTime orderTime = LocalDateTime.now().minusDays(6);
        특정_시간에_쿠폰과_함께_상품을_주문한다(orderTime);

        // when
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .post("/orders/cancel/{id}", 1)
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("refundPrice", equalTo(73_000));

        // then
        /** 주문 상태가 취소인지 확인한다 */
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .get("/orders/{id}", 1)
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("orderId", equalTo(1))
            .body("totalPrice", equalTo(175_000))
            .body("discountedTotalPrice", equalTo(140_000))
            .body("couponDiscountPrice", equalTo(35_000))
            .body("deliveryPrice", equalTo(3_000))
            .body("isValid", equalTo(false))
            .body("coupon.id", equalTo(1))
            .body("coupon.name", equalTo("신규 가입 축하 쿠폰"))
            .body("coupon.discountRate", equalTo(20))
            .body("items[0].quantity", equalTo(10))
            .body("items[0].product.id", equalTo(1))
            .body("items[0].product.name", equalTo("치킨"))
            .body("items[0].product.price", equalTo(10_000))
            .body("items[0].product.imageUrl", equalTo("http://example.com/chicken.jpg"))
            .body("items[1].quantity", equalTo(5))
            .body("items[1].product.id", equalTo(2))
            .body("items[1].product.name", equalTo("피자"))
            .body("items[1].product.price", equalTo(15_000))
            .body("items[1].product.imageUrl", equalTo("http://example.com/pizza.jpg"));

        /** 쿠폰이 사용 가능한 상태로 변경되었는지 확인한다 */
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .get("/users/me/coupons")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("size", is(1))
            .body("[0].id", equalTo(1))
            .body("[0].name", equalTo("신규 가입 축하 쿠폰"))
            .body("[0].discountRate", equalTo(20))
            .body("[0].isUsed", equalTo(false));
    }

    @Test
    @DisplayName("주문한 지 7일 이후라면 예외가 발생한다.")
    void cancelOrder_expired() {
        // given
        쿠폰을_저장한다();
        상품을_저장한다();
        사용자를_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니에_상품을_추가한다(져니_로그인_요청);
        final LocalDateTime orderTime = LocalDateTime.now().minusDays(8);
        특정_시간에_쿠폰과_함께_상품을_주문한다(orderTime);

        // when
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .post("/orders/cancel/{id}", 1)
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errorCode", equalTo(ErrorCode.ORDER_CANNOT_CANCEL.name()))
            .body("errorMessage", equalTo("주문 취소 기간이 지나 취소가 불가능합니다."));
    }

    private void 장바구니에_상품을_추가한다(final MemberLoginRequest 져니_로그인_요청) {
        final CartRequest 치킨_장바구니_저장_요청 = new CartRequest(1L);
        final CartRequest 피자_장바구니_저장_요청 = new CartRequest(2L);
        장바구니_상품_저장(져니_로그인_요청, 치킨_장바구니_저장_요청);
        장바구니_상품_저장(져니_로그인_요청, 피자_장바구니_저장_요청);
    }

    private void 사용자를_저장한다() {
        final MemberJoinRequest 져니_저장_요청 = new MemberJoinRequest("journey", "password");
        사용자_저장(져니_저장_요청);
    }

    private void 상품을_저장한다() {
        final ProductRequest 치킨_등록_요청 = new ProductRequest("치킨", 10_000, "http://example.com/chicken.jpg");
        final ProductRequest 피자_등록_요청 = new ProductRequest("피자", 15_000, "http://example.com/pizza.jpg");
        상품_저장(치킨_등록_요청);
        상품_저장(피자_등록_요청);
    }

    private void 상품을_제거한다() {
        given().log().all()
            .when()
            .delete("/products/{id}", 1)
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }

    private void 쿠폰을_저장한다() {
        final CouponRequest 신규_가입_쿠폰_등록_요청 = new CouponRequest("신규 가입 축하 쿠폰", 20, 365);
        final CouponRequest 첫_주문_쿠폰_등록_요청 = new CouponRequest("첫 주문 감사 쿠폰", 10, 10);
        쿠폰_저장(신규_가입_쿠폰_등록_요청);
        쿠폰_저장(첫_주문_쿠폰_등록_요청);
    }

    private void 만료된_쿠폰을_저장한다() {
        final CouponRequest 비밀의_쿠폰_요청 = new CouponRequest("비밀의 쿠폰", 20, 1);
        쿠폰_저장(비밀의_쿠폰_요청);

        final String sql = "INSERT INTO member_coupon(member_id, coupon_id, issued_at, expired_at, is_used) "
            + "VALUES (?, ?, ?, ?, ?)";
        final LocalDateTime issuedAt = LocalDateTime.of(2023, 6, 1, 13, 0, 0).minusDays(10);
        final LocalDateTime expiredAt = LocalDateTime.of(2023, 6, 1, 13, 0, 0).minusDays(3);
        jdbcTemplate.update(sql, 1L, 3L, Timestamp.valueOf(issuedAt), Timestamp.valueOf(expiredAt), 0);
    }

    private void 이미_사용한_쿠폰을_저장한다() {
        final CouponRequest 비밀의_쿠폰_요청 = new CouponRequest("비밀의 쿠폰", 20, 20);
        쿠폰_저장(비밀의_쿠폰_요청);

        final String sql = "INSERT INTO member_coupon(member_id, coupon_id, issued_at, expired_at, is_used) "
            + "VALUES (?, ?, ?, ?, ?)";
        final LocalDateTime issuedAt = LocalDateTime.of(2023, 6, 1, 13, 0, 0);
        final LocalDateTime expiredAt = LocalDateTime.of(2023, 6, 1, 13, 0, 0).plusDays(20);
        jdbcTemplate.update(sql, 1L, 3L, Timestamp.valueOf(issuedAt), Timestamp.valueOf(expiredAt), 1);
    }

    private void 쿠폰과_함께_상품을_주문한다(final MemberLoginRequest 져니_로그인_요청) {
        final List<OrderProductRequest> 주문할_상품들 = List.of(
            new OrderProductRequest(1L, 10),
            new OrderProductRequest(2L, 5));
        final OrderRequest 주문_요청 = new OrderRequest(1L, 주문할_상품들);
        주문_저장(져니_로그인_요청, 주문_요청);
    }

    private void 쿠폰없이_상품을_주문한다(final MemberLoginRequest 져니_로그인_요청) {
        final List<OrderProductRequest> 주문할_상품들 = List.of(
            new OrderProductRequest(1L, 10),
            new OrderProductRequest(2L, 5));
        final OrderRequest 주문_요청 = new OrderRequest(null, 주문할_상품들);
        주문_저장(져니_로그인_요청, 주문_요청);
    }

    private void 특정_시간에_쿠폰과_함께_상품을_주문한다(final LocalDateTime orderTime) {
        final String 주문_저장_SQL = "INSERT INTO `order`(member_id, total_price, discounted_total_price, "
            + "delivery_price, ordered_at, is_valid) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(주문_저장_SQL, 1, 170_000, 140_000, 3_000, orderTime, true);

        final String 주문_상품_저장_SQL = "INSERT INTO order_product(order_id, product_id, ordered_product_price, quantity) "
            + "VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(주문_상품_저장_SQL, 1, 1, 10000, 10);
        jdbcTemplate.update(주문_상품_저장_SQL, 1, 2, 15000, 5);

        final String 주문_쿠폰_저장_SQL = "INSERT INTO order_coupon(order_id, coupon_id) VALUES (?, ?)";
        jdbcTemplate.update(주문_쿠폰_저장_SQL, 1, 1);
    }
}
