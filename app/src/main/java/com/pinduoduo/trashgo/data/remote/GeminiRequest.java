package com.pinduoduo.trashgo.data.remote;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GeminiRequest {

    private List<Content> contents;

    public GeminiRequest(String textPrompt, String base64Image) {
        this.contents = new ArrayList<>();
        List<Part> parts = new ArrayList<>();
        
        parts.add(new Part(textPrompt));
        parts.add(new Part(new InlineData("image/jpeg", base64Image)));
        
        this.contents.add(new Content(parts));
    }

    public static class Content {
        private List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }
    }

    public static class Part {
        private String text;
        @SerializedName("inline_data")
        private InlineData inlineData;

        public Part(String text) {
            this.text = text;
        }

        public Part(InlineData inlineData) {
            this.inlineData = inlineData;
        }
    }

    public static class InlineData {
        @SerializedName("mime_type")
        private String mimeType;
        private String data;

        public InlineData(String mimeType, String data) {
            this.mimeType = mimeType;
            this.data = data;
        }
    }
}
