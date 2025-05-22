import express from 'express';
import asyncHandler from 'express-async-handler';
import { addEvent, addSession } from './organizer.controller.js';

export const organizerRouter = express.Router();

// Event 등록
organizerRouter.post('/event', asyncHandler(addEvent));

// Session 등록
organizerRouter.post('/session', asyncHandler(addSession));