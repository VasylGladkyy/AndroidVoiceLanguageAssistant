package com.example.androidvoicelanguageassistant;

public class ChatMessage {
    private boolean mLeft;
    private boolean mTranslate;
    private String mMessage;
    private String mLanguageCode;
    ChatMessage(boolean left,boolean translate,String message,String code){
        mLeft=left;
        mTranslate=translate;
        mMessage=message;
        mLanguageCode=code;
    }
    public String getmMessage() {
        return mMessage;
    }
    public boolean getmLeft(){
        return mLeft;
    }
    public String getmLanguageCode() { return mLanguageCode; }
    public boolean getmTranslate() { return mTranslate; }
}
