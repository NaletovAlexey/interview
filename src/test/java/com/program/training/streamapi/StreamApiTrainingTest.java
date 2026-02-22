package com.program.training.streamapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author naletov
 */
class StreamApiTrainingTest {
    StreamApiTraining test;
    @BeforeEach
    void setUp() {
        test = new StreamApiTraining();
    }

    @Test
    void testOddNumbers() {
        List<Integer> nums = new ArrayList<>();
        nums.add(1);
        nums.add(2);
        nums.add(3);
        nums.add(4);
        nums.add(5);

        List<Integer> even = test.getListOfEvenNums(nums);
        assertEquals(2, even.size());
    }

    @Test
    void testARows() {
        List<String> rows = new ArrayList<>();
        rows.add("Accc");
        rows.add("asdasd");
        rows.add("dgdfg");
        rows.add("sdfdsf");
        rows.add("asdad");
        rows.add("asdad");

        List<String> aRows = test.getARows(rows);
        assertEquals(3, aRows.size());
    }

    @Test
    void testMergeLists()
    {
        assertEquals(7, test.getMergeLists().size());
    }
}