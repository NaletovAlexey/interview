package com.program.training.ya.tasks.contest2026.expressions;

import java.io.*;
import java.util.*;

/**
 * Значение арифметического выражения
 * <a href="https://new.contest.yandex.ru/contests/97105/problems?id=30404%2F2023_01_18%2FHgDt2DvsLq">...</a>
 * Примеры:
 *          1+(2*2 - 3) = 2
 *          1+a+1       = WRONG
 *          1 1 + 2     = WRONG
 * @author naletov
 */
public class ArithmeticExpressionValue {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        String expression = reader.readLine();
        if (expression != null)
        {
            Optional<Long> result = parseExpression(expression);
            writer.write(result.isPresent() ?
                    String.valueOf(result.get()) : "WRONG");
            writer.newLine();
        }
        reader.close();
        writer.close();
    }

    private static int operationPriority(char operation)
    {
        return switch (operation)
        {
            case '(' -> 0;
            case '+', '-' -> 1;
            case '*' -> 2;
            default -> -1;
        };
    }

    private static boolean calculate(Deque<Long> numbers, Deque<Character> operations)
    {
        if (numbers.size() < 2) // incorrect expression
        {
            return true;
        }
        // order is important
        long rightValue = numbers.pop();
        long leftValue = numbers.pop();
        char operation = operations.pop();

        Long result = switch (operation)
        {
            case '+' -> leftValue + rightValue;
            case '-' -> leftValue - rightValue;
            case '*' -> leftValue * rightValue;
            default -> null;
        };

        if (result == null)
        {
            return true;
        }
        numbers.push(result);
        return false;
    }

    public static Optional<Long> parseExpression(String s)
    {
        Deque<Long> numbers = new ArrayDeque<>();
        Deque<Character> operations = new ArrayDeque<>();
        int n = s.length();
        int i = 0;
        boolean error = false;
        boolean isOperand = true;

        while (i < n)
        {
            char ch = s.charAt(i);

            if (Character.isWhitespace(ch))  // skip all whitespaces
            {
                i++;

            }
            else if (Character.isDigit(ch))
            {
                if (!isOperand)
                {
                    error = true;
                    break;
                }

                long value = 0;
                while (i < n && Character.isDigit(s.charAt(i)))
                {
                    value = (value * 10) + (s.charAt(i) - '0'); // - '0' is required to convert to digit
                    i++;
                }

                numbers.push(value);
                isOperand = false;

            }
            else if (ch == '(')
            {
                if (!isOperand)
                {
                    error = true;
                    break;
                }

                operations.push('(');
                i++;

            }
            else if (ch == ')') // (), (*, )-  are impossible
            {
                if (isOperand)
                {
                    error = true;
                    break;
                }

                while (!operations.isEmpty() && operations.peek() != '(')
                {
                    if (calculate(numbers, operations))
                    {
                        error = true;
                        break;
                    }
                }

                // did not find a brackets matching -> ( for )
                if (error || operations.isEmpty())
                {
                    error = true;
                    break;
                }

                operations.pop();
                i++;

            }
            else if (ch == '-' && isOperand) // -D = 0 - D
            {
                numbers.push(0L);
                operations.push('-');
                i++;

            }
            else if (ch == '+' || ch == '-' || ch == '*')
            {
                if (isOperand)
                {
                    error = true;
                    break;
                }

                while (!operations.isEmpty() && operationPriority(operations.peek()) >= operationPriority(ch))
                {
                    if (calculate(numbers, operations))
                    {
                        error = true;
                        break;
                    }
                }
                if (error)
                {
                    break;
                }

                operations.push(ch);
                isOperand = true;
                i++;

            }
            else
            {
                error = true; // incorrect symbol
                break;
            }
        }

        if (!error)
        {
            if (isOperand)
            {
                error = true; // operator expectation
            }

            while (!error && !operations.isEmpty())
            {
                if (operations.peek() == '(')
                {
                    error = true; // brackets issue
                    break;
                }
                if (calculate(numbers, operations))
                {
                    error = true;
                    break;
                }
            }

            if (!error && numbers.size() != 1)
            {
                error = true; // not a one digit
            }
        }
        return error ? Optional.empty() : Optional.of(numbers.peek());
    }
}
