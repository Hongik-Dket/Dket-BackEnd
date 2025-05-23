import { StatusCodes } from "http-status-codes";

export const status = {
    // success
    SUCCESS: { status: StatusCodes.OK, "isSuccess": true, "code": "2000", "message": "SUCCESS!" },
    CREATED: { status: StatusCodes.CREATED, "isSuccess": true, "code": "2010", "message": "CREATED!" },
    JOINED: { status: StatusCodes.CREATED, "isSuccess": true, "code": "2020", "message": "JOINED!" },
    NO_CONTENT: { status: StatusCodes.NO_CONTENT, "isSuccess": true, "code": "2040", "message": "NO CONTENT!" },

    // error
    INTERNAL_SERVER_ERROR: { status: StatusCodes.INTERNAL_SERVER_ERROR, "isSuccess": false, "code": "COMMON000", "message": "서버 에러, 관리자에게 문의 바랍니다." },
    BAD_REQUEST: { status: StatusCodes.BAD_REQUEST, "isSuccess": false, "code": "COMMON001", "message": "잘못된 요청입니다." },
    UNAUTHORIZED: { status: StatusCodes.UNAUTHORIZED, "isSuccess": false, "code": "COMMON002", "message": "권한이 잘못되었습니다." },
    METHOD_NOT_ALLOWED: { status: StatusCodes.METHOD_NOT_ALLOWED, "isSuccess": false, "code": "COMMON003", "message": "지원하지 않는 Http Method 입니다." },
    FORBIDDEN: { status: StatusCodes.FORBIDDEN, "isSuccess": false, "code": "COMMON004", "message": "금지된 요청입니다." },
    NOT_FOUND: { status: StatusCodes.NOT_FOUND, "isSuccess": false, "code": "COMMON005", "message": "페이지를 찾을 수 없습니다." },
    PARAMETER_IS_WRONG: { status: StatusCodes.BAD_REQUEST, "isSuccess": false, "code": "COMMON006", "message": "잘못된 파라미터입니다." },

    BLOCKCHAIN_TRANSACTION_FAILED: {status: StatusCodes.INTERNAL_SERVER_ERROR, "isSuccess": false, "code": "BLOCKCHAIN001", "message": "블록체인 트랜잭션 발생에 실패했습니다." },
    FAIL_GET_ETH_PRICE: {status: StatusCodes.INTERNAL_SERVER_ERROR, "isSuccess": false, "code": "BLOCKCHAIN002", "message": "API에서 올바른 ETH 가격을 받지 못했습니다." },
    INVALID_PRICE: {status: StatusCodes.BAD_REQUEST, "isSuccess": false, "code": "BLOCKCHAIN003", "message": "krwAmount는 유효한 숫자여야 합니다." },
    

};