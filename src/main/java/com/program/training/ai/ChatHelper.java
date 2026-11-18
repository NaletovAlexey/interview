package com.program.training.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.helpers.MessageAccumulator;
import com.anthropic.models.messages.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Инкапсулирует работу с историей диалога: хранит список сообщений
 * и умеет добавлять реплики пользователя/ассистента, а также
 * отправлять накопленный контекст в C
 * @author naletov
 */
public class ChatHelper {

    private static final long DEFAULT_MAX_TOKENS = 1000L;

    private final AnthropicClient client;
    private final Model model;
    private final long maxTokens;
    private final List<MessageParam> messages = new ArrayList<>();

    public ChatHelper(AnthropicClient client, Model model)
    {
        this(client, model, DEFAULT_MAX_TOKENS);
    }

    public ChatHelper(AnthropicClient client, Model model, long maxTokens)
    {
        this.client = client;
        this.model = model;
        this.maxTokens = maxTokens;
    }

    /**
     * Добавляет сообщение пользователя в историю диалога.
     */
    public void addUserMessage(String text)
    {
        messages.add(
                MessageParam.builder()
                        .role(MessageParam.Role.USER)
                        .content(text)
                        .build()
        );
    }

    /**
     * Добавляет сообщение ассистента в историю диалога.
     */
    public void addAssistantMessage(String text)
    {
        messages.add(
                MessageParam.builder()
                        .role(MessageParam.Role.ASSISTANT)
                        .content(text)
                        .build()
        );
    }

    /**
     * Отправляет всю накопленную историю в Claude и возвращает
     * текст ответа. Ответ в историю НЕ добавляется автоматически —
     * это нужно сделать явно через addAssistantMessage, если он
     * успешно получен.
     *
     * Без system-промпта — Claude отвечает в обычном, "дефолтном" режиме.
     */
    public String chat()
    {
        return chat(null);
    }

    /**
     * То же самое, но с system-промптом — инструкцией, задающей роль
     * и правила поведения Claude на весь разговор. В отличие от Python,
     * здесь не нужно вручную решать, добавлять ли параметр: если
     * system == null или пустая строка, .system(...) на builder'е
     * просто не вызывается.
     */
    public String chat(String system)
    {
        MessageCreateParams.Builder paramsBuilder = MessageCreateParams.builder()
                .model(model)
                .maxTokens(maxTokens)
                .messages(messages);

        if (system != null && !system.isBlank())
        {
            paramsBuilder.system(system);
        }

        Message message = client.messages().create(paramsBuilder.build());

        return message.content().getFirst()
                .text()
                .orElseThrow()
                .text();
    }

    /**
     * Убирает последнее сообщение из истории (например, после неудачного запроса).
     */
    public void removeLastMessage()
    {
        if (!messages.isEmpty())
        {
            messages.removeLast();
        }
    }

    /**
     * Текущий размер истории (количество сообщений).
     */
    public int historySize()
    {
        return messages.size();
    }

    /**
     * Потоковая версия chat(): по мере генерации текста вызывает
     * onTextChunk для каждого кусочка (например, чтобы печатать
     * в консоль без ожидания полного ответа), а по завершении
     * возвращает собранное целиком сообщение — как get_final_message()
     * в Python-версии.
     *
     * Ответ в историю НЕ добавляется автоматически — как и в chat().
     */
    public Message chatStreaming(String system, Consumer<String> onTextChunk)
    {
        MessageCreateParams.Builder paramsBuilder = MessageCreateParams.builder()
                .model(model)
                .maxTokens(maxTokens)
                .messages(messages);

        if (system != null && !system.isBlank())
        {
            paramsBuilder.system(system);
        }

        MessageAccumulator accumulator = MessageAccumulator.create();

        try (StreamResponse<RawMessageStreamEvent> stream =
                     client.messages().createStreaming(paramsBuilder.build()))
        {

            stream.stream()
                    .peek(accumulator::accumulate)
                    .flatMap(event -> event.contentBlockDelta().stream())
                    .flatMap(deltaEvent -> deltaEvent.delta().text().stream())
                    .forEach(textDelta -> onTextChunk.accept(textDelta.text()));
        }

        return accumulator.message();
    }

    /**
     * Перегрузка без system-промпта.
    */
    public Message chatStreaming(Consumer<String> onTextChunk)
    {
        return chatStreaming(null, onTextChunk);
    }
}
