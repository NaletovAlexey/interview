package com.program.training.ya.tasks.contest2026.queue;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Быстрое возведение в степень
 * <a href="https://new.contest.yandex.ru/contests/97108/problems?id=175943%2F2016_11_22%2FcXYEjvzveD">...</a>
 *
 * Пример:
 * push 3       ok
 * push 14      ok
 * size         2
 * clear        ok
 * push 1       ok
 * front        1
 * push 2       ok
 * front        1
 * pop          1
 * size         1
 * pop          2
 * size         2
 * exit         bye
 *
 * @author naletov
 */
public class FaultTolerantQueue
{
    private static final String PUSH = "push";
    private static final String POP = "pop";
    private static final String FRONT = "front";
    private static final String SIZE = "size";
    private static final String CLEAR = "clear";
    private static final String EXIT = "exit";

    private static final String OK = "ok";
    private static final String ERROR = "error";
    private static final String BYE = "bye";

    // record for a parsed command: command name + optional argument
    private record Command(String name, String arg) {}

    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        Queue<Integer> queue = new ArrayDeque<>();
        String line;
        while ((line = reader.readLine()) != null)
        {
            line = line.trim();
            if (line.isEmpty())
            {
                continue;
            }

            String[] parts = line.split("\\s+");
            Command command = new Command(parts[0], parts.length > 1 ? parts[1] : null);

            String result = switch (command.name())
            {
                case PUSH ->
                {
                    int n = Integer.parseInt(command.arg());
                    queue.add(n);
                    yield OK;
                }
                case POP -> queue.isEmpty() ? ERROR : String.valueOf(queue.poll());
                case FRONT  -> queue.isEmpty() ? ERROR : String.valueOf(queue.peek());
                case SIZE -> String.valueOf(queue.size());
                case CLEAR ->
                {
                    queue.clear();
                    yield OK;
                }
                case EXIT -> BYE;
                default -> null;
            };

            if (result == null)
            {
                continue;
            }

            writer.write(result);
            writer.newLine();

            if (command.name().equals(EXIT))
            {
                break;
            }
        }

        reader.close();
        writer.close();
    }
}
