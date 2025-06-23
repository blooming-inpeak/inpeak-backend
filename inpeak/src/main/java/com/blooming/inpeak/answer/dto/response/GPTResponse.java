package com.blooming.inpeak.answer.dto.response;

import com.blooming.inpeak.answer.dto.command.GPTMessage;
import java.util.List;

public record GPTResponse (
    List<Choice> choices
){
    public record Choice (
        int index,
        GPTMessage message
    ){ }
}
