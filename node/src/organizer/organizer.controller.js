import { recordEventOnChain } from "./organizer.service.js";
import { status } from "../../config/response.status.js";
import { response } from "../../config/response.js";
import { BaseError } from "../../config/error.js";

export const createEvent = async (req, res, next) => {
    const {
        eventId,
        organizerAddress,
        title,
        maxWinners,
        priceKrw,
        photoCardURIs,
    } = req.body;
  
      // 유효성 검사
    if (
        typeof eventId !== 'number' ||
        typeof organizerAddress !== 'string' ||
        organizerAddress.trim() === '' ||
        typeof title !== 'string' ||
        title.trim() === '' ||
        typeof maxWinners !== 'number' ||
        typeof priceKrw !== 'number' ||
        !Array.isArray(photoCardURIs) ||
        !photoCardURIs.every((uri) => typeof uri === 'string')
    ) {
        throw new BaseError(status.PARAMETER_IS_WRONG);
    }

    // 서비스 호출
    const event = await recordEventOnChain({eventId, organizerAddress, title, maxWinners, priceKrw, photoCardURIs});

    // 성공 응답 (201 Created)
    res.send(response(status.CREATED, event));

}