import { application } from "express"

export const createEventResponseDTO = (txHash, priceWei) => {
    return {
        "txHash" : txHash,
        "priceWei" : priceWei
    }
}

export const createSessionResponseDTO = (sessionId, txHash, winners) => {
    return {
        "sessionId" : sessionId,
        "txHash" : txHash,
        "winnerWallets" : winners
    }
}