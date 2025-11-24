const API_BASE = "http://localhost:8080";

const textInput = document.getElementById("textInput");
const sendTextBtn = document.getElementById("sendTextBtn");
const subtitleEl = document.getElementById("subtitle");
const audioPlayer = document.getElementById("audioPlayer");

sendTextBtn.addEventListener("click", async () => {
    const text = textInput.value.trim();
    if (!text) {
        alert("先输入一点内容吧~");
        return;
    }

    sendTextBtn.disabled = true;
    sendTextBtn.textContent = "发送中...";

    try {
        const resp = await fetch(`${API_BASE}/api/chat/text`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ text })
        });

        if (!resp.ok) {
            const errText = await resp.text();
            throw new Error("后端错误：" + errText);
        }

        const data = await resp.json();

        subtitleEl.textContent = data.replyText || "(无文本)";

        if (data.audioUrl) {
            audioPlayer.src = data.audioUrl;
            audioPlayer.play().catch(e => console.log("播放失败", e));
        }


    } catch (e) {
        console.error(e);
        alert("调用接口失败：" + e.message);
    } finally {
        sendTextBtn.disabled = false;
        sendTextBtn.textContent = "发送文字";
    }
});
