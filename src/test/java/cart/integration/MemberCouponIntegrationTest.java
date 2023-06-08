package cart.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import cart.application.dto.coupon.CouponRequest;
import cart.application.dto.member.MemberJoinRequest;
import cart.application.dto.member.MemberLoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class MemberCouponIntegrationTest extends IntegrationTest {
    @Test
    @DisplayName("사용자의 쿠폰 목록을 조회한다.")
    void getMyCoupons() {
        // given
        /** 쿠폰 저장 */
        final CouponRequest 신규_가입_쿠폰_등록_요청 = new CouponRequest("신규 가입 축하 쿠폰", 20, 365);
        쿠폰_저장(신규_가입_쿠폰_등록_요청);

        /** 사용자 저장 */
        final MemberJoinRequest 져니_저장_요청 = new MemberJoinRequest("journey", "jourzura1");
        사용자_저장(져니_저장_요청);

        final MemberLoginRequest 져니_로그인_요청 = new MemberLoginRequest(져니_저장_요청.getName(), 져니_저장_요청.getPassword());

        // expected
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
}
