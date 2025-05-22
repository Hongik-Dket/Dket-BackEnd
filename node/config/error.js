import { status } from "./response.status.js";

export class BaseError extends Error {
    constructor(data){
        if(!data) {
            data = status.INTERNAL_SERVER_ERROR;
        };
        
        super(data.message);
        this.data = data;
    }
}