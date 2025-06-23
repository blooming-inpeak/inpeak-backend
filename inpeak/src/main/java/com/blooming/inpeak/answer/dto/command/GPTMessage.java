package com.blooming.inpeak.answer.dto.command;

public record GPTMessage(
    String role,
    Object content
){ }
