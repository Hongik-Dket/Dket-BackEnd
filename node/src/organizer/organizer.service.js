import { ethers } from 'ethers';
import dotenv from 'dotenv';
dotenv.config();

import abis from '../../abis.json' with { type: 'json' };
import { contractAddress } from '../../config/smartcontract.js';
import { BaseError } from "../../config/error.js";
import { status } from "../../config/response.status.js";

import * as dto from "./organizer.dto.js";
import { convertKRWToETH } from "../common/exchange.service.js";

// 프로바이더 및 서명자 설정 (예: 환경변수 기반)
const provider = new ethers.JsonRpcProvider(process.env.RPC_URL);
const wallet = new ethers.Wallet(process.env.PRIVATE_KEY, provider);
const contract = new ethers.Contract(contractAddress, abis, wallet);

export const recordEventOnChain = async ({
    eventId,
    organizerAddress,
    title,
    maxWinners,
    priceKrw,
    photoCardURIs,
}) => {
    const priceEth = await convertKRWToETH(priceKrw);
    const priceWei = ethers.parseEther(priceEth.toString());

    try {
        const tx = await contract.createEvent(
        eventId,
        organizerAddress,
        title,
        maxWinners,
        priceWei,
        photoCardURIs
        );

        // 블록에 기록될 때까지 대기
        const receipt = await tx.wait();

        // 성공적으로 기록된 경우
        return dto.createEventResponseDTO(receipt.hash, priceWei.toString());

    } catch (err) {
        console.error('Smart contract call failed:', err);
        throw new BaseError(status.BLOCKCHAIN_TRANSACTION_FAILED);
    }
};
