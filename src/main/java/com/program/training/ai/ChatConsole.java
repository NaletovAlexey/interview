package com.program.training.ai;

import io.github.cdimascio.dotenv.Dotenv;
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Model;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 *  Простой консольный чат с Claude.
 *  Вся работа с историей диалога вынесена в ChatHelper — здесь только
 *  цикл чтения ввода и вывод ответов. Для завершения работы
 *  пользователь вводит "end".
 *
 * <a href="https://anthropic.skilljar.com/claude-with-the-anthropic-api/287727>...</a>
 *
 * @author naletov
 */
public class ChatConsole
{
    public static String ANTHROPIC_API_KEY = "ANTHROPIC_API_KEY";

    private static final String EXIT_COMMAND = "end";

    public static void main(String[] args)
    {
        // Принудительно переводим stdout/stdin на UTF-8 — не полагаемся
        // на кодировку консоли по умолчанию (на Windows это часто CP1251/CP866)
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        // Загружаем ANTHROPIC_API_KEY из .env, если он там есть
        // .directory("./") — ищем .env в рабочей директории процесса,
        // а не там, где случайно оказался рабочи
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();

        String apiKey = dotenv.get(ANTHROPIC_API_KEY);

        // Диагностика: убеждаемся, что ключ реально нашёлся, не печатая сам ключ
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("ANTHROPIC_API_KEY не найден через .env, пробуем переменную окружения ОС...");
        } else {
            System.out.println("Ключ найден в .env, длина: " + apiKey.length() + " символов.");
            System.setProperty("ANTHROPIC_API_KEY", apiKey);
        }

        AnthropicClient client = AnthropicOkHttpClient.fromEnv();
        ChatHelper chatHelper = new ChatHelper(client, Model.CLAUDE_SONNET_4_5);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Чат с Claude запущен. Введите \"" + EXIT_COMMAND + "\" для выхода.\n");

        while (true)
        {
            System.out.print("Вы: ");
            String userInput = scanner.nextLine().trim();

            if (userInput.equalsIgnoreCase(EXIT_COMMAND))
            {
                System.out.println("Разговор завершён.");
                break;
            }

            if (userInput.isEmpty())
            {
                continue;
            }

            chatHelper.addUserMessage(userInput);

            String answer;
            try
            {
                // Без system-промпта
                answer = chatHelper.chat();

                // С system-промптом
//                String system = """
//                        You are a patient math tutor.
//                        Do not directly answer a student's questions.
//                        Guide them to a solution step by step.
//                        """;
//                String answer = chatHelper.chat(system);
            } catch (Exception e)
            {
                System.out.println("Ошибка при обращении к API: " + e.getMessage());
                // не добавляем некорректный обмен в историю
                chatHelper.removeLastMessage();
                continue;
            }

            chatHelper.addAssistantMessage(answer);

            System.out.println("Claude: " + answer + "\n");
        }

        scanner.close();
    }
}