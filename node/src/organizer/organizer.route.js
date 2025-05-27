import express from 'express';
import asyncHandler from 'express-async-handler';
import { createEvent, createSession } from './organizer.controller.js';

export const organizerRouter = express.Router();

// Event 등록
organizerRouter.post('/event', asyncHandler(createEvent));

// Session 등록
organizerRouter.post('/session', asyncHandler(createSession));