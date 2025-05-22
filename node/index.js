import express from 'express';
import http from 'http'; 
import dotenv from 'dotenv';
import cors from 'cors';

import { response } from './config/response.js';
import { BaseError } from './config/error.js';
import { status } from './config/response.status.js';

import {organizerRouter} from './src/organizer/organizer.route.js';
import {buyerRouter} from './src/buyer/buyer.route.js';


dotenv.config();

const app = express();
const server = http.createServer(app);  // HTTP 서버 생성

// 서버 설정
app.use(cors());
app.use(express.static('public'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));

// 라우터 설정
app.use('/organizer', organizerRouter);
app.use('/buyer', buyerRouter);

// 404 처리
app.use((req, res, next) => {
    console.log(`Requested URL: ${req.originalUrl}`);
    const err = new BaseError(status.NOT_FOUND);
    next(err);
});

// 에러 핸들링
app.use((err, req, res, next) => {
    console.log(err);

    res.locals.message = err.message;
    res.locals.error = process.env.NODE_ENV !== 'production' ? err : {};

    if (err instanceof BaseError) {
        return res.status(err.data.status).send(response(err.data));
    } else {
        return res.send(response(status.INTERNAL_SERVER_ERROR));
    }
});
