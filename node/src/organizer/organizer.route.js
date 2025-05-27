import express from 'express';
import asyncHandler from 'express-async-handler';
import { createEvent, createSession, openPublicSale } from './organizer.controller.js';

export const organizerRouter = express.Router();

// Event 등록
organizerRouter.post('/event', asyncHandler(createEvent));

// Session 등록
organizerRouter.post('/session', asyncHandler(createSession));

// 선착순 판매 전환
organizerRouter.post('/open-public', asyncHandler(openPublicSale));