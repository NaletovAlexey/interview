package com.program.training.ya.tasks.contest2026.brackets;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Поврежденный XML
 *
 * <a href="https://new.contest.yandex.ru/contests/97104/problems?id=30404%2F2021_10_27%2F4skidbF6ac">...</a>
 * Ровно один символ в строке заменился на некоторый другой символ.
 *
 * @author naletov
 */
public class CorruptedXML {

    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        String s = reader.readLine();
        writer.write(Objects.requireNonNull(fixXml(s.toCharArray())));

        writer.close();
        reader.close();
    }

    private static String fixXml(char[] arr)
    {
        int n = arr.length;
        // all the possible symbols
        int idx = 0;
        char[] candidates = new char[29];

        candidates[idx++] = '<';
        candidates[idx++] = '>';
        candidates[idx++] = '/';
        for (char c = 'a'; c <= 'z'; c++)
            candidates[idx++] = c;

        int[] stackStart = new int[n];
        int[] stackEnd = new int[n];

        for (int i = 0; i < n; i++)
        {
            char original = arr[i];
            for (char c : candidates)
            {
                if (c == original)  // skip replacing a character with itself
                {
                    continue;
                }
                arr[i] = c;
                if (isCorrectXML(arr, n, stackStart, stackEnd)) {
                    return new String(arr);
                }
            }
            arr[i] = original;
        }

        return null;    // won't come here.
    }

    static boolean isCorrectXML(char[] arr, int n, int[] stackStart, int[] stackEnd)
    {
        Deque<String> stack = new ArrayDeque<>();
        int i = 0;  // current position

        while (i < n)
        {
            if (arr[i] != '<')
                return false;
            i++;

            boolean closing = false;
            if (i < n && arr[i] == '/')
            {
                closing = true;
                i++;
            }

            int start = i;
            // read tags name
            while (i < n && arr[i] >= 'a' && arr[i] <= 'z')
                i++;
            // tags name can be empty
            if (i == start)
                return false;

            int end = i;
            // after name must be >
            if (i >= n || arr[i] != '>')
                return false;
            i++;

            String tagName = new String(arr, start, end - start);

            if (closing)
            {
                // Tag can be closed in case of empty stack
                if (stack.isEmpty())
                {
                    return false;
                }

                // The closing tag must match the last opened tag.
                if (!stack.peek().equals(tagName))
                {
                    return false;
                }

                // Remove the last open tag.
                stack.pop();
            }
            else
            {
                // Add the opening tag to the stack.
                stack.push(tagName);
            }
        }
        // all the open tags must be closed
        return stack.isEmpty();
    }
}
