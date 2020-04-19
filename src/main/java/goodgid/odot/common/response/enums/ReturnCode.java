package goodgid.odot.common.response.enums;

import lombok.Getter;

@Getter
public enum ReturnCode implements CodeEnum, TextEnum, ReturnEnum {
    SUCCESS("0000", "Success"),
    SERVICE_UNAVAILABLE("9000","Service Unavailable")
    ;

    private String code;
    private String text;

    ReturnCode(String code, String text) {
        this.code = code;
        this.text = text;
    }


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getReturnCode() {
        return getCode();
    }

    @Override
    public String getReturnMessage() {
        return getText();
    }

}
