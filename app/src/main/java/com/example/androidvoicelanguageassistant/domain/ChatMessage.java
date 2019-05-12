package com.example.androidvoicelanguageassistant.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Getter
    private boolean mLeft;
    @Getter
    private boolean mTranslate;
    @Getter
    private String mMessage;
    @Getter
    private String mLanguageCode;

}
