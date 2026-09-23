package com.program.training.ya.tasks.contest2026.brackets;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

/**
 * Правильная скобочная последовательность
 *
 * <a href="https://new.contest.yandex.ru/contests/97104/problems?id=175943%2F2016_11_22%2FeSUHFTjBwZ">...</a>
 *
 * test:
 * 1. ()[]  -> yes
 * 2. ([)]  -> no
 * 3. (     -> no
 *
 * @author naletov
 */
public class CorrectBracketSequence {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        String sequence = reader.readLine();
        if  (sequence == null)
        {
            sequence = "";
        }

        // Dictionary: closing bracket -> corresponding opening bracket
        HashMap<Character, Character> brackets = new HashMap<>();
        brackets.put(')', '(');
        brackets.put(']', '[');
        brackets.put('}', '{');

        Deque<Character> stack = new ArrayDeque<>();
        boolean isOK = true;

        for (int i = 0; i < sequence.length(); i++) {
            char bracket = sequence.charAt(i);
            if (bracket == '(' || bracket == '[' || bracket == '{')
            {
                stack.push(bracket);
            }
            else  if (brackets.containsKey(bracket))
            {
                if (stack.isEmpty() || stack.pop() != brackets.get(bracket))
                {
                    isOK = false;
                    break;
                }
            }
        }
        if (!stack.isEmpty()) {
            isOK = false;
        }
        writer.write(isOK ? "yes" : "no");

        reader.close();
        writer.close();
    }
}
