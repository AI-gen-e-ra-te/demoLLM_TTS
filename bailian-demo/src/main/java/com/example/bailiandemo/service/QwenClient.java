package com.example.bailiandemo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class QwenClient {

    private static final String CHAT_URL =
            "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private static final String TTS_URL =
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";

    private static final String API_KEY = System.getenv("DASHSCOPE_API_KEY");

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private void checkApiKey() {
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IllegalStateException("环境变量 DASHSCOPE_API_KEY 未配置！");
        }
    }

    /**
     * 调用 LLM API
     */
    public String chatWithQwen(String userText) throws Exception {
        checkApiKey();

        String body = """
                {
                  "model": "qwen-plus",
                  "messages": [
                    { "role": "system", "content": "You are a helpful assistant." },
                    { "role": "user", "content": %s }
                  ]
                }
                """.formatted(mapper.writeValueAsString(userText));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CHAT_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(response.body());
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    /**
     * 调用 TTS API（返回音频 URL）
     */
    public String ttsToAudioUrl(String text) throws Exception {
        checkApiKey();

        String body = """
                {
                  "model": "qwen3-tts-flash",
                  "input": {
                    "text": %s,
                    "voice": "Cherry",
                    "language_type": "Chinese"
                  }
                }
                """.formatted(mapper.writeValueAsString(text));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TTS_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // 打印完整返回内容（调试用）
        System.out.println("TTS 返回内容 = " + response.body());

        JsonNode root = mapper.readTree(response.body());

        // 正确路径：output.audio.url
        JsonNode audioUrlNode = root.path("output").path("audio").path("url");

        if (audioUrlNode.isMissingNode() || audioUrlNode.asText().isEmpty()) {
            throw new RuntimeException("TTS 返回中没有 audio.url 字段！");
        }

        return audioUrlNode.asText();
    }


}
