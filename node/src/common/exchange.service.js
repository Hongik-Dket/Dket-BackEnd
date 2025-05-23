import axios from 'axios';
import { BaseError } from '../../config/error.js';
import { status } from "../../config/response.status.js";

async function getEthPriceInKRW() {
    try {
        const response = await axios.get('https://api.coingecko.com/api/v3/simple/price?ids=ethereum&vs_currencies=krw');
        const price = response.data?.ethereum?.krw;

        if (typeof price !== 'number' || isNaN(price))
            throw new BaseError(status.FAIL_GET_ETH_PRICE)
        
        return price;

    } catch (error) {
        console.error('ETH 가격 조회 실패:', error.message);
        throw error;
    }
}

export async function convertKRWToETH(krwAmount) {
    if (typeof krwAmount !== 'number' || isNaN(krwAmount))
      throw new BaseError(status.INVALID_PRICE);

    const ethPriceKRW = await getEthPriceInKRW();
    const ethAmount = krwAmount / ethPriceKRW;

    return ethAmount;
}
  