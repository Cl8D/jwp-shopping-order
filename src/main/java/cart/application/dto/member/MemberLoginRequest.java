package cart.application.dto.member;

import javax.validation.constraints.NotBlank;

public class MemberLoginRequest {

    @NotBlank(message = "이름에 빈 값을 입력할 수 없습니다.")
    private final String name;

    @NotBlank(message = "사용자 비밀번호는 비어있을 수 없습니다.")
    private final String password;

    public MemberLoginRequest() {
        this(null, null);
    }

    public MemberLoginRequest(final String name, final String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}
