package com.example.demo.global.response.status;

import com.example.demo.global.response.code.BaseErrorCode;
import com.example.demo.global.response.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    COMMON_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 에러가 발생했습니다. 관리자에게 문의하세요."),
    COMMON_BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_4001", "잘못된 요청입니다."),
    COMMON_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_4002", "인증이 필요합니다."),
    COMMON_WRONG_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON_4003", "잘못된 파라미터 값 입니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_4004", "입력값이 유효하지 않습니다."),
    INVALID_WALLET_ADDRESS(HttpStatus.BAD_REQUEST, "COMMON_4005", "유효하지 않은 지갑 주소 형식입니다."),

    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_4001", "해당 유저를 찾을 수 없습니다."),
    USER_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "USER_4002", "이미 탈퇴한 유저입니다."),
    USER_ALREADY_LOGOUT(HttpStatus.BAD_REQUEST, "USER_4003", "이미 로그아웃한 유저입니다."),
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "USER_4004", "이미 존재하는 사용자입니다."),
    USER_INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "USER_4005", "로그인 경로가 규칙에 맞지 않습니다."),
    USER_INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "USER_4006", "비밀번호 설정 규칙에 맞지 않습니다."),
    USER_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER_4007", "비밀번호가 잘못되었습니다."),
    USER_NICKNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "USER_4008", "이미 존재하는 유저 닉네임입니다."),

    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "TOKEN_4001", "토큰이 누락되었습니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "TOKEN_4002", "해당 토큰을 찾을 수 없습니다."),
    TOKEN_INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_4003", "만료되거나 잘못된 엑세스 토큰입니다."),
    TOKEN_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_4004", "만료되거나 잘못된 리프레시 토큰입니다."),

    ADMIN_UNAUTHORIZED_ACCESS(HttpStatus.BAD_REQUEST, "ADMIN_4001", "관리자만 접근 가능한 경로입니다."),

    OAUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "OAUTH_4001", "OAuth 로그인에 실패했습니다."),
    OAUTH_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OAUTH_4002", "OAuth 로그인 처리에 실패했습니다."),

    TICKET_INVALID_SEAT(HttpStatus.BAD_REQUEST, "TICKET_4001", "좌석 코드는 0 이상 999999 이하의 숫자여야 합니다."),
    TICKET_INVALID_BUYER(HttpStatus.BAD_REQUEST, "TICKET_4002", "해당 티켓을 구매할 수 없는 사용자입니다."),
    TICKET_NOT_FOUND(HttpStatus.BAD_REQUEST, "TICKET_4003", "해당 티켓을 찾을 수 없습니다."),
    TICKET_WRONG_PARAMETER(HttpStatus.BAD_REQUEST, "TICKET_4004", "id 또는 number 중 하나만 입력해야 합니다."),
    TICKET_INVALID_USER(HttpStatus.BAD_REQUEST, "TICKET_4005", "해당 티켓에 대한 권한이 없는 사용자입니다."),
    TICKET_INVALID(HttpStatus.BAD_REQUEST, "TICKET_4006", "유효하지 않은 티켓입니다."),
    TICKET_ALREADY_PAID(HttpStatus.BAD_REQUEST, "TICKET_4007", "이미 티켓을 결제했습니다."),

    CONCERT_NOT_FOUND(HttpStatus.BAD_REQUEST, "CONCERT_4001", "해당 공연을 찾을 수 없습니다."),
    CONCERT_ORGANIZER_MISMATCH(HttpStatus.BAD_REQUEST, "CONCERT_4002", "해당 공연의 개최자가 아닙니다."),
    CONCERT_SESSION_MISMATCH(HttpStatus.BAD_REQUEST, "CONCERT_4003", "해당 공연의 회차가 아닙니다."),
    CONCERT_INVALID_SCHEDULE(HttpStatus.BAD_REQUEST, "CONCERT_4004", "잘못된 응모 기간 혹은 공연 기간입니다."),

    SESSION_NOT_FOUND(HttpStatus.BAD_REQUEST, "SESSION_4001", "해당 회차를 찾을 수 없습니다."),
    SESSION_ALREADY_DRAWN(HttpStatus.BAD_REQUEST, "SESSION_4002", "이미 추첨 완료된 세션입니다."),
    SESSION_DRAW_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SESSION_5001", "해당 세션 추첨에 실패했습니다."),
    SESSION_MINTING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SESSION_5002", "해당 세션 티켓 발행에 실패했습니다."),
    SESSION_CANNOT_BUY(HttpStatus.BAD_REQUEST, "SESSION_4003", "티켓을 구매할 수 없는 세션입니다."),

    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_5001", "이미지 업로드에 실패하였습니다."),
    IMAGE_GENERATE_QRCODE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_5002", "QR 코드 생성에 실패했습니다."),
    IMAGE_CONVERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_5003", "이미지 변환에 실패했습니다."),

    BLOCKCHAIN_TRANSACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "BLOCKCHAIN_5001", "블록체인 트랜잭션 발생에 실패하였습니다."),
    BLOCKCHAIN_GET_ETH_PRICE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "BLOCKCHAIN_5002", "이더리움 가격을 조회하는데 실패하였습니다."),
    BLOCKCHAIN_ESTIMATE_GAS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "BLOCKCHAIN_5004", "가스 추정에 실패하였습니다."),

    TOKEN_INVALID(HttpStatus.BAD_REQUEST, "TOKEN_4001", "유효하지 않은 토큰입니다."),

    JOB_EXECUTION_FAILED(HttpStatus.BAD_REQUEST, "JOB_4001", "Job 실행에 실패했습니다."),
    JOB_STORE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JOB_5001", "Job 저장에 실패했습니다."),
    JOB_UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR, "JOB_5002", "알 수 없는 Job 에러가 발생했습니다."),
    JOB_CANCEL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JOB_5003", "Job 취소에 실패했습니다."),
    INVALID_JOB_CLASS(HttpStatus.BAD_REQUEST, "JOB_4002", "유효한 Job Class가 아닙니다."),

    APPLY_INVALID_PERIOD(HttpStatus.BAD_REQUEST, "APPLY_4001", "응모 기간이 아닙니다."),
    APPLY_ALREADY_DONE(HttpStatus.BAD_REQUEST, "APPLY_4002", "이미 해당 회차에 응모한 상태입니다."),
    APPLY_NOT_FOUND(HttpStatus.BAD_REQUEST, "APPLY_4003", "해당 응모 내역을 찾을 수 없습니다."),
    APPLY_AGE_RESTRICTED(HttpStatus.BAD_REQUEST, "APPLY_4004", "응모 가능한 연령이 아닙니다."),
    APPLY_SELF_HOSTING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "APPLY_4005", "본인이 개최한 공연에는 응모할 수 없습니다."),

    IPFS_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IPFS_5001", "IPFS 업로드에 실패했습니다."),

    METADATA_NOT_FOUND(HttpStatus.BAD_REQUEST, "METADATA_4001", "해당 메타데이터를 찾을 수 없습니다."),
    METADATA_JSON_CONVERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "METADATA_5001", "해당 메타데이터를 Json으로 변환하는데 실패했습니다."),

    WALLET_INVALID_ADDRESS(HttpStatus.BAD_REQUEST, "WALLET_4001", "유효하지 않은 주소입니다."),
    WALLET_GET_BALANCE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "WALLET_5001", "잔액 조회에 실패했습니다."),

    PHOTOCARD_NOT_FOUND(HttpStatus.BAD_REQUEST, "PHOTOCARD_4001", "해당 포토카드를 찾을 수 없습니다."),

    RESALE_NOT_FOUND(HttpStatus.BAD_REQUEST, "RESALE_4001", "해당 리세일 정보를 찾을 수 없습니다."),
    RESALE_INVALID_PRICE(HttpStatus.BAD_REQUEST, "RESALE_4002", "리세일 판매가는 0이 아닌 양수이어야 합니다."),
    RESALE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "RESALE_4002", "리세일이 불가한 공연입니다."),
    RESALE_ALREADY_LISTED(HttpStatus.BAD_REQUEST, "RESALE_4003", "아직 판매 중인 리세일이 있는 티켓입니다."),
    RESALE_PRICE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "RESALE_4004", "리세일 가격 상한을 초과하였습니다."),
    RESALE_ALREADY_RESERVED(HttpStatus.BAD_REQUEST, "RESALE_4005", "이미 예약된 리세일입니다."),
    RESALE_RESERVATION_FORBIDDEN(HttpStatus.FORBIDDEN, "RESALE_4006", "본인이 예약하지 않은 리세일입니다."),
    RESALE_NOT_RESERVED_USER(HttpStatus.BAD_REQUEST, "RESALE_4007", "현재 사용자가 예약한 리세일이 아닙니다."),
    RESALE_CONFLICT(HttpStatus.INTERNAL_SERVER_ERROR, "RESALE_5001", "리세일 락 획득에 실패했습니다."),

    RESALE_AUTH_NO_TEMPLATE(HttpStatus.NOT_FOUND, "RESALE_AUTH_4001", "서명 템플릿을 찾을 수 없습니다."),
    RESALE_AUTH_INVALID_ADDRESS(HttpStatus.BAD_REQUEST, "RESALE_AUTH_4002", "잘못된 주소입니다."),
    RESALE_AUTH_WRONG_PARAMETER(HttpStatus.BAD_REQUEST, "RESALE_AUTH_4003", "잘못된 파라미터입니다."),
    RESALE_AUTH_SIGN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RESALE_AUTH_5001", "EIP-712 서명에 실패했습니다."),
    RESALE_AUTH_TYPEDDATA_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RESALE_AUTH_5002", "TypedDateJson 생성에 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .httpStatus(httpStatus)
                .code(code)
                .message(message)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return getReason();
    }
}