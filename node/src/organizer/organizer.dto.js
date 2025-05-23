export const createEventResponseDTO = (txHash, priceWei) => {
    return {
        "txHash": txHash,
        "priceWei": priceWei
    }
}