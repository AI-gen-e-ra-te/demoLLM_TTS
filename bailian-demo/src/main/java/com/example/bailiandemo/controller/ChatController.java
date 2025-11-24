package com.example.bailiandemo.controller;

import com.example.bailiandemo.dto.ChatTextRequest;
import com.example.bailiandemo.dto.ChatResponse;
import com.example.bailiandemo.service.QwenClient;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/chat")
public class ChatController {

    private final QwenClient qwenClient = new QwenClient();

    @PostMapping("/text")
    public ChatResponse chatByText(@RequestBody ChatTextRequest request) throws Exception {

        // 1. 调用 LLM
        String reply = qwenClient.chatWithQwen(request.getText());

        // 2. 调用 TTS（返回 URL）
        String audioUrl = qwenClient.ttsToAudioUrl(reply);

        // 3. 组装响应
        ChatResponse resp = new ChatResponse();
        resp.setReplyText(reply);
        resp.setAudioUrl(audioUrl);
        return resp;
    }
}
