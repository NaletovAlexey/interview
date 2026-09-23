package com.program.training.ya.tasks.contest2026.brackets;

import java.io.*;

/**
 * Минимальная ПСП
 *
 * В первой строке записано число n (1 ≤ n ≤ 100000)
 * Во второй строке записана строка w, состоящая из 4 различных скобок
 * В третьей строке записана строка s, ее длина не превосходит n
 *
 * <a href="https://new.contest.yandex.ru/contests/97104/problems?id=30404%2F2024_11_02%2FmyVIGk5VNmwZ">...</a>
 *
 * test:
 * 1. 6 -> ()[] -> ([( -> answer: ([()])
 * 2. 6 -> ][)( -> ([ -> answer: ([][])
 * 3. 4 -> (][) -> ()[] -> answer: ()[]
 *
 * @author naletov
 */
public class FindBracketSequence {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        int n = Integer.parseInt(reader.readLine());
        String w = reader.readLine();
        String s = reader.readLine();

        char[] stack = new char[n]; // store opening brackets
        int top = 0;                // elements amount in stack

        StringBuilder answer = new StringBuilder(n);
        answer.append(s);   // it must start with a prefix

        // Handle prefix
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);

            if (isOpening(c))
            {
                stack[top++] = c;
            } else {
                top--;
            }
        }

        // complete up the answer
        while (answer.length() < n) {
            int remaining = n - answer.length() - 1;

            for (int i = 0; i < 4; i++) // 4 - w length
            {
                char c = w.charAt(i);
                int newTop;

                if (isOpening(c))
                {
                    newTop = top + 1;   // if there is opening bracket -> stack size + 1
                } else {
                    if (top == 0)       // If the candidate bracket is closing, but the stack is empty, such a bracket cannot be placed.
                    {
                        continue;
                    }

                    char expected;      // determine which closing bracket corresponds to the upper opening one

                    if (stack[top - 1] == '(')
                    {
                        expected = ')';
                    } else
                    {
                        expected = ']';
                    }

                    if (c != expected)
                    {
                        continue;
                    }

                    newTop = top - 1;   // the closing bracket matches -> the stack size is reduced by 1.
                }

                // check if sequence can be finished
                if (remaining < newTop) // After adding a candidate, there must be enough characters left to close all open brackets.
                {
                    continue;
                }
                // The difference between the number of remaining characters and the open brackets must be even.
                if ((remaining - newTop) % 2 != 0)
                {
                    continue;
                }

                // The symbol matches
                answer.append(c);

                if (isOpening(c))
                {
                    stack[top++] = c;
                } else {
                    top--;
                }
                break;
            }
        }
        writer.write(String.valueOf(answer));

        reader.close();
        writer.close();
    }

    // Checks if it's opening bracket
    private static boolean isOpening(char c)
    {
        return c == '(' || c == '[';
    }
}
