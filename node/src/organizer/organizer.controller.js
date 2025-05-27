import { recordEventOnChain, recondSessionOnChain, openPublicSaleOnChain } from "./organizer.service.js";
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

    console.log('Event creation requested');

    // 서비스 호출
    const event = await recordEventOnChain({eventId, organizerAddress, title, maxWinners, priceKrw, photoCardURIs});

    // 성공 응답 (201 Created)
    res.send(response(status.SUCCESS, event));
}

export const createSession = async (req, res, next) => {
    const {
        eventId,
        sessionId,
        applications
    } = req.body;

    if (
        typeof eventId !== 'number' ||
        typeof sessionId !== 'number' ||
        !Array.isArray(applications) ||
        !applications.every((apply) => typeof apply == 'string')
    ) {
        throw new BaseError(status.PARAMETER_IS_WRONG);
    }

    console.log('Session creation requested');

    await recondSessionOnChain({eventId, sessionId, applications});

    res.send(response(status.SUCCESS));
}

export const openPublicSale = async (req, res, next) => {
    const { eventId } = req.body;

    if (typeof eventId !== 'number')
        throw new BaseError(status.PARAMETER_IS_WRONG);

    console.log('Open public sale');

    await openPublicSaleOnChain({eventId});

    res.send(response(status.SUCCESS));
}