package com.blooming.inpeak.answer.dto.response;

import com.blooming.inpeak.answer.dto.command.Message;
import java.util.List;

public record GPTResponse (
    List<Choice> choices
){
    public record Choice (
        int index,
        Message message
    ){ }
}
