package cart.integration;

import static cart.exception.ErrorCode.CART_ALREADY_ADD;
import static cart.exception.ErrorCode.CART_NOT_FOUND;
import static cart.exception.ErrorCode.PRODUCT_NOT_FOUND;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import cart.application.dto.cartitem.CartItemQuantityUpdateRequest;
import cart.application.dto.cartitem.CartRequest;
import cart.application.dto.cartitem.CartResponse;
import cart.application.dto.member.MemberJoinRequest;
import cart.application.dto.member.MemberLoginRequest;
import cart.application.dto.product.ProductRequest;
import cart.exception.ErrorResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class CartIntegrationTest extends IntegrationTest {

    @Test
    @DisplayName("장바구니에 아이템을 추가한다.")
    void addCartItem() {
        // given
        사용자를_저장한다();
        상품을_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        final CartRequest 장바구니_저장_요청 = new CartRequest(1L);

        // expected
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .body(장바구니_저장_요청)
            .when()
            .post("/cart-items")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .header(LOCATION, "/cart-items/" + 1);
    }

    @Test
    @DisplayName("존재하지 않는 상품을 장바구니에 추가하려고 하면 예외가 발생한다.")
    void addCartItem_not_exists_product() {
        // given
        사용자를_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        final CartRequest 장바구니_저장_요청 = new CartRequest(1L);

        // expected
        final ErrorResponse 에러_응답 = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .body(장바구니_저장_요청)
            .when()
            .post("/cart-items")
            .then()
            .statusCode(NOT_FOUND.value())
            .extract()
            .as(ErrorResponse.class);

        assertThat(에러_응답)
            .extracting(ErrorResponse::getErrorCode, ErrorResponse::getErrorMessage)
            .containsExactly(PRODUCT_NOT_FOUND, "상품이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("이미 추가된 상품을 장바구니에 추가하려고 하면 예외가 발생한다.")
    void addCartItem_already_add() {
        // given
        사용자를_저장한다();
        상품을_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        final CartRequest 치킨_장바구니_저장_요청 = new CartRequest(1L);
        장바구니_상품_저장(져니_로그인_요청, 치킨_장바구니_저장_요청);

        // expected
        final CartRequest 이미_존재하는_상품_장바구니_저장_요청 = new CartRequest(1L);
        final ErrorResponse 에러_응답 = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .body(이미_존재하는_상품_장바구니_저장_요청)
            .when()
            .post("/cart-items")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract()
            .as(ErrorResponse.class);

        assertThat(에러_응답)
            .extracting(ErrorResponse::getErrorCode, ErrorResponse::getErrorMessage)
            .containsExactly(CART_ALREADY_ADD, "장바구니에 해당 상품이 이미 등록되어 있습니다.");
    }

    @Test
    @DisplayName("사용자가 담은 장바구니 아이템을 조회한다.")
    void getCartItems() {
        // given
        사용자를_저장한다();
        상품을_저장한다();
        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니에_상품을_추가한다(져니_로그인_요청);
        장바구니_상품의_수량을_변경한다(10, 져니_로그인_요청);

        // expected
        final List<CartResponse> 장바구니_응답 = given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .get("/cart-items")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getList(".", CartResponse.class);

        assertThat(장바구니_응답).hasSize(2);
        assertThat(장바구니_응답)
            .extracting(CartResponse::getId, CartResponse::getQuantity,
                cartResponse -> cartResponse.getProduct().getId(),
                cartResponse -> cartResponse.getProduct().getName(),
                cartResponse -> cartResponse.getProduct().getPrice(),
                cartResponse -> cartResponse.getProduct().getImageUrl())
            .containsExactly(
                tuple(1L, 10, 1L, "치킨", 10000, "http://example.com/chicken.jpg"),
                tuple(2L, 1, 2L, "피자", 15000, "http://example.com/pizza.jpg")
            );
    }

    @Test
    @DisplayName("장바구니에 담긴 아이템의 수량을 변경한다.")
    void increaseCartItemQuantity() {
        // given
        사용자를_저장한다();
        상품을_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니에_상품을_추가한다(져니_로그인_요청);
        장바구니_상품의_수량을_변경한다(10, 져니_로그인_요청);

        // expected
        final CartItemQuantityUpdateRequest 장바구니_수량_수정_요청 = new CartItemQuantityUpdateRequest(10);
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .body(장바구니_수량_수정_요청)
            .patch("/cart-items/{cartItemId}", 1)
            .then()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("장바구니에 담긴 아이템의 수량을 0으로 변경하면, 장바구니에서 아이템이 삭제된다.")
    void decreaseCartItemQuantityToZero() {
        // given
        /** 사용자 저장 */
        사용자를_저장한다();

        /** 상품 저장 */
        final ProductRequest 치킨_등록_요청 = new ProductRequest("치킨", 10_000, "http://example.com/chicken.jpg");
        상품_저장(치킨_등록_요청);

        /** 장바구니 상품 저장 */
        final CartRequest 치킨_장바구니_저장_요청 = new CartRequest(1L);
        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        장바구니_상품_저장(져니_로그인_요청, 치킨_장바구니_저장_요청);

        // when
        final CartItemQuantityUpdateRequest 장바구니_수량_수정_요청 = new CartItemQuantityUpdateRequest(0);
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .body(장바구니_수량_수정_요청)
            .patch("/cart-items/{cartItemId}", 1)
            .then()
            .statusCode(HttpStatus.OK.value());

        // then
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .get("/cart-items")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("size", is(0));
    }

    @Test
    @DisplayName("다른 사용자가 담은 장바구니 아이템의 수량을 변경하려 하면 실패한다.")
    void updateOtherMembersCartItem() {
        // given
        /** 사용자 저장 */
        final MemberJoinRequest 져니_등록_요청 = new MemberJoinRequest("journey", "password");
        final MemberJoinRequest 라온_등록_요청 = new MemberJoinRequest("raon", "jourzura!");
        사용자_저장(져니_등록_요청);
        사용자_저장(라온_등록_요청);

        /** 상품 저장 */
        final ProductRequest 치킨_등록_요청 = new ProductRequest("치킨", 10_000, "http://example.com/chicken.jpg");
        상품_저장(치킨_등록_요청);

        /** 장바구니 상품 저장 */
        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest(져니_등록_요청.getName(), 져니_등록_요청.getPassword());
        final CartRequest 치킨_장바구니_저장_요청 = new CartRequest(1L);
        장바구니_상품_저장(져니_로그인_요청, 치킨_장바구니_저장_요청);
        final MemberLoginRequest 라온_로그인_요청 = new MemberLoginRequest(라온_등록_요청.getName(), 라온_등록_요청.getPassword());

        // expected
        final CartItemQuantityUpdateRequest 장바구니_수량_수정_요청 = new CartItemQuantityUpdateRequest(10);
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().preemptive().basic(라온_로그인_요청.getName(), 라온_로그인_요청.getPassword())
            .when()
            .body(장바구니_수량_수정_요청)
            .patch("/cart-items/{cartItemId}", 1)
            .then()
            .statusCode(FORBIDDEN.value());
    }

    @Test
    @DisplayName("장바구니에 담긴 아이템을 삭제한다.")
    void removeCartItem() {
        // given
        사용자를_저장한다();
        상품을_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        final CartRequest 치킨_장바구니_저장_요청 = new CartRequest(1L);
        장바구니_상품_저장(져니_로그인_요청, 치킨_장바구니_저장_요청);

        // when
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .delete("/cart-items/{cartItemId}", 1)
            .then();

        // then
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .get("/cart-items")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("size", is(0));
    }

    @Test
    @DisplayName("다른 사용자가 담은 장바구니 아이템을 삭제하려고 하면 실패한다.")
    void removeCartItem_other_member_items() {
        /** 사용자 저장 */
        final MemberJoinRequest 져니_등록_요청 = new MemberJoinRequest("journey", "password");
        final MemberJoinRequest 라온_등록_요청 = new MemberJoinRequest("raon", "jourzura!");
        사용자_저장(져니_등록_요청);
        사용자_저장(라온_등록_요청);

        /** 상품 저장 */
        final ProductRequest 치킨_등록_요청 = new ProductRequest("치킨", 10_000, "http://example.com/chicken.jpg");
        상품_저장(치킨_등록_요청);

        /** 장바구니 상품 저장 */
        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest(져니_등록_요청.getName(), 져니_등록_요청.getPassword());
        final CartRequest 치킨_장바구니_저장_요청 = new CartRequest(1L);
        장바구니_상품_저장(져니_로그인_요청, 치킨_장바구니_저장_요청);
        final MemberLoginRequest 라온_로그인_요청 = new MemberLoginRequest(라온_등록_요청.getName(), 라온_등록_요청.getPassword());

        // expected
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().preemptive().basic(라온_로그인_요청.getName(), 라온_로그인_요청.getPassword())
            .when()
            .delete("/cart-items/{cartItemId}", 1)
            .then()
            .statusCode(FORBIDDEN.value());
    }

    @Test
    @DisplayName("장바구니에 담긴 아이템을 여러 개 삭제한다.")
    void removeCartItems() {
        // given
        사용자를_저장한다();
        상품을_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        final CartRequest 치킨_장바구니_저장_요청 = new CartRequest(1L);
        장바구니_상품_저장(져니_로그인_요청, 치킨_장바구니_저장_요청);
        final CartRequest 피자_장바구니_저장_요청 = new CartRequest(2L);
        장바구니_상품_저장(져니_로그인_요청, 피자_장바구니_저장_요청);

        // when
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .delete("/cart-items?ids=1,2")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then
        given()
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .get("/cart-items")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("size", is(0));
    }

    @Test
    @DisplayName("장바구니에 담긴 아이템을 여러 개 삭제할 때, 요청한 상품 개수와 실제 장바구니에 담긴 개수가 다르다면 예외가 발생한다.")
    void removeCartItems_forbidden() {
        // given
        사용자를_저장한다();
        상품을_저장한다();

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest("journey", "password");
        final CartRequest 치킨_장바구니_저장_요청 = new CartRequest(1L);
        장바구니_상품_저장(져니_로그인_요청, 치킨_장바구니_저장_요청);

        // when
        final ErrorResponse 에러_응답 = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().preemptive().basic(져니_로그인_요청.getName(), 져니_로그인_요청.getPassword())
            .when()
            .delete("/cart-items?ids=2,3")
            .then()
            .statusCode(NOT_FOUND.value())
            .extract()
            .as(ErrorResponse.class);

        assertThat(에러_응답)
            .extracting(ErrorResponse::getErrorCode, ErrorResponse::getErrorMessage)
            .containsExactly(CART_NOT_FOUND, "장바구니 정보를 찾을 수 없습니다.");
    }

    private void 사용자를_저장한다() {
        final MemberJoinRequest 져니_등록_요청 = new MemberJoinRequest("journey", "password");
        사용자_저장(져니_등록_요청);
    }

    private void 상품을_저장한다() {
        final ProductRequest 치킨_등록_요청 = new ProductRequest("치킨", 10_000, "http://example.com/chicken.jpg");
        final ProductRequest 피자_등록_요청 = new ProductRequest("피자", 15_000, "http://example.com/pizza.jpg");
        상품_저장(치킨_등록_요청);
        상품_저장(피자_등록_요청);
    }

    private void 장바구니에_상품을_추가한다(final MemberLoginRequest 져니_로그인_요청) {
        final CartRequest 치킨_장바구니_저장_요청 = new CartRequest(1L);
        final CartRequest 피자_장바구니_저장_요청 = new CartRequest(2L);
        장바구니_상품_저장(져니_로그인_요청, 치킨_장바구니_저장_요청);
        장바구니_상품_저장(져니_로그인_요청, 피자_장바구니_저장_요청);
    }

    private void 장바구니_상품의_수량을_변경한다(final int quantity, final MemberLoginRequest 져니_로그인_요청) {
        final CartItemQuantityUpdateRequest 장바구니_수량_수정_요청 = new CartItemQuantityUpdateRequest(quantity);
        장바구니_상품_수량_수정(져니_로그인_요청, 장바구니_수량_수정_요청, 1L);
    }
}
