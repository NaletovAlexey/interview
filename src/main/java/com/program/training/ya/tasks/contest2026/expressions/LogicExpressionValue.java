package com.program.training.ya.tasks.contest2026.expressions;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Значение логического выражения
 * Пример: 1|(0&0^1)
 * <a href="https://new.contest.yandex.ru/contests/97105/problems?id=30404%2F2023_01_18%2FV1acL50PPm">...</a>
 *
 * Операции:
 * ! — отрицание
 * & — логическое И
 * | — логическое ИЛИ
 * ^ — исключающее ИЛИ
 *
 * @author naletov
 */
public class LogicExpressionValue {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        String expression = reader.readLine();

        if (expression != null)
        {
            Optional<Integer> result = parseExpression(expression);

            writer.write(result.isPresent()
                    ? String.valueOf(result.get())
                    : "WRONG");
        }

        reader.close();
        writer.close();
    }

    private static int operationPriority(char operation)
    {
        return switch (operation)
        {
            case '(' -> 0;
            case '|', '^' -> 1;
            case '&' -> 2;
            case '!' -> 3;
            default -> -1;
        };
    }

    private static boolean calculate(Deque<Integer> numbers, Deque<Character> operations)
    {
        if (operations.isEmpty())
        {
            return true;
        }

        char operation = operations.pop();

        if (operation == '!')
        {
            if (numbers.isEmpty())
            {
                return true;
            }

            int value = numbers.pop();
            numbers.push(value == 0 ? 1 : 0);
            return false;
        }

        if (numbers.size() < 2)
        {
            return true;
        }

        int rightValue = numbers.pop();
        int leftValue = numbers.pop();

        int result;

        switch (operation)
        {
            case '&':
                result = leftValue & rightValue;
                break;

            case '|':
                result = leftValue | rightValue;
                break;

            case '^':
                result = leftValue ^ rightValue;
                break;

            default:
                return true;
        }

        numbers.push(result);
        return false;
    }

    public static Optional<Integer> parseExpression(String expression)
    {
        Deque<Integer> numbers = new ArrayDeque<>();
        Deque<Character> operations = new ArrayDeque<>();

        int i = 0;
        int n = expression.length();

        boolean isOperand = true;
        boolean error = false;

        while (i < n)
        {
            char ch = expression.charAt(i);

            if (Character.isWhitespace(ch))
            {
                i++;
            }
            else if (ch == '0' || ch == '1')
            {
                if (!isOperand)
                {
                    error = true;
                    break;
                }

                numbers.push(ch - '0');
                isOperand = false;
                i++;
            }
            else if (ch == '!')
            {
                if (!isOperand)
                {
                    error = true;
                    break;
                }

                operations.push(ch);
                i++;
            }
            else if (ch == '(')
            {
                if (!isOperand)
                {
                    error = true;
                    break;
                }

                operations.push(ch);
                i++;
            }
            else if (ch == ')')
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

                if (error || operations.isEmpty())
                {
                    error = true;
                    break;
                }

                operations.pop(); // drop (
                i++;
            }
            else if (ch == '&' || ch == '|' || ch == '^')
            {
                if (isOperand)
                {
                    error = true;
                    break;
                }

                while (!operations.isEmpty()
                        && operations.peek() != '('
                        && operationPriority(operations.peek())
                        >= operationPriority(ch))
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
                error = true;
                break;
            }
        }

        if (!error)
        {
            if (isOperand)
            {
                error = true;
            }
        }

        while (!error && !operations.isEmpty())
        {
            if (operations.peek() == '(')
            {
                error = true;
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
            error = true;
        }

        return error ? Optional.empty() : Optional.of(numbers.peek());
    }
}
