package org.fisco.bcos.sdk.v3.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Extended JUnit 5 test class for ByteUtils
 * Tests uncovered methods with normal flows, exceptions, and boundary values
 */
class ByteUtilsExtendedTest {

    private byte[] testArray;
    private byte[] emptyArray;

    @BeforeEach
    void setUp() {
        testArray = new byte[]{1, 2, 3, 4, 5};
        emptyArray = new byte[0];
    }

    // ============ byteArrayToInt Tests ============

    @Test
    void testByteArrayToInt_NormalCase() {
        byte[] bytes = new byte[]{0, 0, 1, 0}; // 256
        int result = ByteUtils.byteArrayToInt(bytes);
        assertEquals(256, result);
    }

    @Test
    void testByteArrayToInt_NullInput() {
        int result = ByteUtils.byteArrayToInt(null);
        assertEquals(0, result);
    }

    @Test
    void testByteArrayToInt_EmptyArray() {
        int result = ByteUtils.byteArrayToInt(emptyArray);
        assertEquals(0, result);
    }

    @Test
    void testByteArrayToInt_SingleByte() {
        byte[] bytes = new byte[]{5};
        int result = ByteUtils.byteArrayToInt(bytes);
        assertEquals(5, result);
    }

    @Test
    void testByteArrayToInt_LargeValue() {
        byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        int result = ByteUtils.byteArrayToInt(bytes);
        // BigInteger interprets as unsigned, so max unsigned 32-bit value should be > Integer.MAX_VALUE
        // But since it returns an int, it will be the int value of that BigInteger
        assertTrue(result != 0); // Should be non-zero
    }

    // ============ byteArrayToLong Tests ============

    @Test
    void testByteArrayToLong_NormalCase() {
        byte[] bytes = new byte[]{0, 0, 0, 0, 0, 0, 1, 0}; // 256
        long result = ByteUtils.byteArrayToLong(bytes);
        assertEquals(256L, result);
    }

    @Test
    void testByteArrayToLong_NullInput() {
        long result = ByteUtils.byteArrayToLong(null);
        assertEquals(0L, result);
    }

    @Test
    void testByteArrayToLong_EmptyArray() {
        long result = ByteUtils.byteArrayToLong(emptyArray);
        assertEquals(0L, result);
    }

    // ============ nibblesToPrettyString Tests ============

    @Test
    void testNibblesToPrettyString_NormalCase() {
        byte[] nibbles = new byte[]{1, 2, 3};
        String result = ByteUtils.nibblesToPrettyString(nibbles);
        assertNotNull(result);
        assertTrue(result.contains("\\x"));
        assertEquals("\\x01\\x02\\x03", result);
    }

    @Test
    void testNibblesToPrettyString_EmptyArray() {
        String result = ByteUtils.nibblesToPrettyString(emptyArray);
        assertEquals("", result);
    }

    @Test
    void testNibblesToPrettyString_SingleNibble() {
        byte[] nibbles = new byte[]{15};
        String result = ByteUtils.nibblesToPrettyString(nibbles);
        assertEquals("\\x0f", result);
    }

    // ============ oneByteToHexString Tests ============

    @Test
    void testOneByteToHexString_Zero() {
        String result = ByteUtils.oneByteToHexString((byte) 0);
        assertEquals("00", result);
    }

    @Test
    void testOneByteToHexString_SmallValue() {
        String result = ByteUtils.oneByteToHexString((byte) 5);
        assertEquals("05", result);
    }

    @Test
    void testOneByteToHexString_LargeValue() {
        String result = ByteUtils.oneByteToHexString((byte) 255);
        assertEquals("ff", result);
    }

    @Test
    void testOneByteToHexString_NegativeValue() {
        String result = ByteUtils.oneByteToHexString((byte) -1);
        assertNotNull(result);
        assertEquals(2, result.length());
    }

    // ============ numBytes Tests ============

    @Test
    void testNumBytes_Zero() {
        int result = ByteUtils.numBytes("0");
        assertEquals(1, result);
    }

    @Test
    void testNumBytes_SmallNumber() {
        int result = ByteUtils.numBytes("255");
        assertEquals(1, result);
    }

    @Test
    void testNumBytes_LargeNumber() {
        int result = ByteUtils.numBytes("65536");
        assertEquals(3, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "10", "100", "1000", "10000"})
    void testNumBytes_VariousValues(String value) {
        int result = ByteUtils.numBytes(value);
        assertTrue(result > 0);
    }

    // ============ firstNonZeroByte Tests ============

    @Test
    void testFirstNonZeroByte_NoZeroes() {
        byte[] data = new byte[]{1, 2, 3};
        int result = ByteUtils.firstNonZeroByte(data);
        assertEquals(0, result);
    }

    @Test
    void testFirstNonZeroByte_LeadingZeroes() {
        byte[] data = new byte[]{0, 0, 1, 2};
        int result = ByteUtils.firstNonZeroByte(data);
        assertEquals(2, result);
    }

    @Test
    void testFirstNonZeroByte_AllZeroes() {
        byte[] data = new byte[]{0, 0, 0, 0};
        int result = ByteUtils.firstNonZeroByte(data);
        assertEquals(-1, result);
    }

    // ============ stripLeadingZeroes Tests ============

    @Test
    void testStripLeadingZeroes_NoLeadingZeroes() {
        byte[] data = new byte[]{1, 2, 3};
        byte[] result = ByteUtils.stripLeadingZeroes(data);
        assertArrayEquals(data, result);
    }

    @Test
    void testStripLeadingZeroes_WithLeadingZeroes() {
        byte[] data = new byte[]{0, 0, 1, 2, 3};
        byte[] result = ByteUtils.stripLeadingZeroes(data);
        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    @Test
    void testStripLeadingZeroes_AllZeroes() {
        byte[] data = new byte[]{0, 0, 0};
        byte[] result = ByteUtils.stripLeadingZeroes(data);
        assertArrayEquals(ByteUtils.ZERO_BYTE_ARRAY, result);
    }

    @Test
    void testStripLeadingZeroes_NullInput() {
        byte[] result = ByteUtils.stripLeadingZeroes(null);
        assertNull(result);
    }

    // ============ increment Tests ============

    @Test
    void testIncrement_NormalCase() {
        byte[] bytes = new byte[]{0, 0, 5};
        boolean result = ByteUtils.increment(bytes);
        assertTrue(result);
        assertEquals(6, bytes[2]);
    }

    @Test
    void testIncrement_Overflow() {
        byte[] bytes = new byte[]{0, (byte) 255};
        boolean result = ByteUtils.increment(bytes);
        assertTrue(result);
        assertEquals(1, bytes[0]);
        assertEquals(0, bytes[1]);
    }

    @Test
    void testIncrement_AllMaxValues() {
        byte[] bytes = new byte[]{(byte) 255, (byte) 255};
        boolean result = ByteUtils.increment(bytes);
        assertFalse(result); // All bytes wrapped around to 0
    }

    // ============ copyToArray Tests ============

    @Test
    void testCopyToArray_SmallValue() {
        BigInteger value = BigInteger.valueOf(256);
        byte[] result = ByteUtils.copyToArray(value);
        assertEquals(32, result.length);
        assertEquals(1, result[30]);
        assertEquals(0, result[31]);
    }

    @Test
    void testCopyToArray_Zero() {
        BigInteger value = BigInteger.ZERO;
        byte[] result = ByteUtils.copyToArray(value);
        assertEquals(32, result.length);
        for (byte b : result) {
            assertEquals(0, b);
        }
    }

    @Test
    void testCopyToArray_LargeValue() {
        BigInteger value = new BigInteger("123456789012345678901234567890");
        byte[] result = ByteUtils.copyToArray(value);
        assertEquals(32, result.length);
    }

    // ============ setBit and getBit Tests ============

    @Test
    void testSetBit_SetToOne() {
        byte[] data = new byte[]{0, 0};
        byte[] result = ByteUtils.setBit(data, 0, 1);
        assertNotNull(result);
        assertEquals(1, ByteUtils.getBit(result, 0));
    }

    @Test
    void testSetBit_SetToZero() {
        byte[] data = new byte[]{(byte) 0xFF, (byte) 0xFF};
        byte[] result = ByteUtils.setBit(data, 0, 0);
        assertNotNull(result);
        assertEquals(0, ByteUtils.getBit(result, 0));
    }

    @Test
    void testSetBit_InvalidPosition() {
        byte[] data = new byte[]{0};
        assertThrows(Error.class, () -> {
            ByteUtils.setBit(data, 100, 1);
        });
    }

    @Test
    void testGetBit_FirstBit() {
        byte[] data = new byte[]{1};
        int result = ByteUtils.getBit(data, 0);
        assertEquals(1, result);
    }

    @Test
    void testGetBit_InvalidPosition() {
        byte[] data = new byte[]{0};
        assertThrows(Error.class, () -> {
            ByteUtils.getBit(data, 100);
        });
    }

    // ============ and, or, xor Tests ============

    @Test
    void testAnd_NormalCase() {
        byte[] b1 = new byte[]{(byte) 0xFF, 0x0F};
        byte[] b2 = new byte[]{0x0F, (byte) 0xFF};
        byte[] result = ByteUtils.and(b1, b2);
        assertEquals(0x0F, result[0]);
        assertEquals(0x0F, result[1]);
    }

    @Test
    void testAnd_DifferentSizes() {
        byte[] b1 = new byte[]{1, 2};
        byte[] b2 = new byte[]{1};
        assertThrows(RuntimeException.class, () -> {
            ByteUtils.and(b1, b2);
        });
    }

    @Test
    void testOr_NormalCase() {
        byte[] b1 = new byte[]{0x01, 0x02};
        byte[] b2 = new byte[]{0x04, 0x08};
        byte[] result = ByteUtils.or(b1, b2);
        assertEquals(0x05, result[0]);
        assertEquals(0x0A, result[1]);
    }

    @Test
    void testOr_DifferentSizes() {
        byte[] b1 = new byte[]{1, 2};
        byte[] b2 = new byte[]{1};
        assertThrows(RuntimeException.class, () -> {
            ByteUtils.or(b1, b2);
        });
    }

    @Test
    void testXor_NormalCase() {
        byte[] b1 = new byte[]{0x0F, 0x0F};
        byte[] b2 = new byte[]{0x0F, 0x00};
        byte[] result = ByteUtils.xor(b1, b2);
        assertEquals(0, result[0]);
        assertEquals(0x0F, result[1]);
    }

    @Test
    void testXor_DifferentSizes() {
        byte[] b1 = new byte[]{1, 2};
        byte[] b2 = new byte[]{1};
        assertThrows(RuntimeException.class, () -> {
            ByteUtils.xor(b1, b2);
        });
    }

    @Test
    void testXorAlignRight_B1Longer() {
        byte[] b1 = new byte[]{1, 2, 3};
        byte[] b2 = new byte[]{3};
        byte[] result = ByteUtils.xorAlignRight(b1, b2);
        assertEquals(3, result.length);
        assertEquals(1, result[0]);
        assertEquals(2, result[1]);
        assertEquals(0, result[2]); // 3 XOR 3 = 0
    }

    @Test
    void testXorAlignRight_B2Longer() {
        byte[] b1 = new byte[]{3};
        byte[] b2 = new byte[]{1, 2, 3};
        byte[] result = ByteUtils.xorAlignRight(b1, b2);
        assertEquals(3, result.length);
    }

    @Test
    void testXorAlignRight_SameLength() {
        byte[] b1 = new byte[]{1, 2};
        byte[] b2 = new byte[]{2, 1};
        byte[] result = ByteUtils.xorAlignRight(b1, b2);
        assertEquals(2, result.length);
    }

    // ============ merge Tests ============

    @Test
    void testMerge_TwoArrays() {
        byte[] arr1 = new byte[]{1, 2};
        byte[] arr2 = new byte[]{3, 4};
        byte[] result = ByteUtils.merge(arr1, arr2);
        assertArrayEquals(new byte[]{1, 2, 3, 4}, result);
    }

    @Test
    void testMerge_ThreeArrays() {
        byte[] arr1 = new byte[]{1};
        byte[] arr2 = new byte[]{2};
        byte[] arr3 = new byte[]{3};
        byte[] result = ByteUtils.merge(arr1, arr2, arr3);
        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    @Test
    void testMerge_WithEmptyArray() {
        byte[] arr1 = new byte[]{1, 2};
        byte[] arr2 = new byte[0];
        byte[] result = ByteUtils.merge(arr1, arr2);
        assertArrayEquals(arr1, result);
    }

    @Test
    void testMerge_NoArrays() {
        byte[] result = ByteUtils.merge();
        assertEquals(0, result.length);
    }

    // ============ isNullOrZeroArray and isSingleZero Tests ============

    @Test
    void testIsNullOrZeroArray_Null() {
        assertTrue(ByteUtils.isNullOrZeroArray(null));
    }

    @Test
    void testIsNullOrZeroArray_EmptyArray() {
        assertTrue(ByteUtils.isNullOrZeroArray(emptyArray));
    }

    @Test
    void testIsNullOrZeroArray_NonEmptyArray() {
        assertFalse(ByteUtils.isNullOrZeroArray(testArray));
    }

    @Test
    void testIsSingleZero_True() {
        assertTrue(ByteUtils.isSingleZero(new byte[]{0}));
    }

    @Test
    void testIsSingleZero_False_MultipleZeros() {
        assertFalse(ByteUtils.isSingleZero(new byte[]{0, 0}));
    }

    @Test
    void testIsSingleZero_False_NonZero() {
        assertFalse(ByteUtils.isSingleZero(new byte[]{1}));
    }

    @Test
    void testIsSingleZero_False_Empty() {
        assertFalse(ByteUtils.isSingleZero(emptyArray));
    }

    // ============ difference Tests ============

    @Test
    void testDifference_NormalCase() {
        Set<byte[]> setA = new HashSet<>();
        setA.add(new byte[]{1, 2});
        setA.add(new byte[]{3, 4});
        setA.add(new byte[]{5, 6});

        Set<byte[]> setB = new HashSet<>();
        setB.add(new byte[]{3, 4});

        Set<byte[]> result = ByteUtils.difference(setA, setB);
        assertEquals(2, result.size());
    }

    @Test
    void testDifference_EmptySets() {
        Set<byte[]> setA = new HashSet<>();
        Set<byte[]> setB = new HashSet<>();
        Set<byte[]> result = ByteUtils.difference(setA, setB);
        assertTrue(result.isEmpty());
    }

    @Test
    void testDifference_NoCommonElements() {
        Set<byte[]> setA = new HashSet<>();
        setA.add(new byte[]{1, 2});

        Set<byte[]> setB = new HashSet<>();
        setB.add(new byte[]{3, 4});

        Set<byte[]> result = ByteUtils.difference(setA, setB);
        assertEquals(1, result.size());
    }

    // ============ length Tests ============

    @Test
    void testLength_SingleArray() {
        int result = ByteUtils.length(testArray);
        assertEquals(5, result);
    }

    @Test
    void testLength_MultipleArrays() {
        int result = ByteUtils.length(testArray, new byte[]{6, 7, 8});
        assertEquals(8, result);
    }

    @Test
    void testLength_WithNullArray() {
        int result = ByteUtils.length(testArray, null, new byte[]{6});
        assertEquals(6, result);
    }

    @Test
    void testLength_EmptyArrays() {
        int result = ByteUtils.length(emptyArray, emptyArray);
        assertEquals(0, result);
    }

    // ============ bytes/ints conversion Tests ============

    @Test
    void testBytesToInts_BigEndian() {
        byte[] bytes = new byte[]{0, 0, 0, 1, 0, 0, 0, 2};
        int[] result = ByteUtils.bytesToInts(bytes, true);
        assertEquals(2, result.length);
        assertEquals(1, result[0]);
        assertEquals(2, result[1]);
    }

    @Test
    void testBytesToInts_LittleEndian() {
        byte[] bytes = new byte[]{1, 0, 0, 0, 2, 0, 0, 0};
        int[] result = ByteUtils.bytesToInts(bytes, false);
        assertEquals(2, result.length);
        assertEquals(1, result[0]);
        assertEquals(2, result[1]);
    }

    @Test
    void testIntsToBytes_BigEndian() {
        int[] ints = new int[]{1, 2};
        byte[] result = ByteUtils.intsToBytes(ints, true);
        assertEquals(8, result.length);
        assertEquals(0, result[0]);
        assertEquals(0, result[1]);
        assertEquals(0, result[2]);
        assertEquals(1, result[3]);
    }

    @Test
    void testIntsToBytes_LittleEndian() {
        int[] ints = new int[]{1, 2};
        byte[] result = ByteUtils.intsToBytes(ints, false);
        assertEquals(8, result.length);
        assertEquals(1, result[0]);
    }

    // ============ bigEndianToShort Tests ============

    @Test
    void testBigEndianToShort_NormalCase() {
        byte[] bytes = new byte[]{0, 1};
        short result = ByteUtils.bigEndianToShort(bytes);
        assertEquals(1, result);
    }

    @Test
    void testBigEndianToShort_WithOffset() {
        byte[] bytes = new byte[]{5, 0, 1};
        short result = ByteUtils.bigEndianToShort(bytes, 1);
        assertEquals(1, result);
    }

    @Test
    void testBigEndianToShort_MaxValue() {
        byte[] bytes = new byte[]{(byte) 0x7F, (byte) 0xFF};
        short result = ByteUtils.bigEndianToShort(bytes);
        assertEquals(Short.MAX_VALUE, result);
    }

    // ============ shortToBytes Tests ============

    @Test
    void testShortToBytes_PositiveValue() {
        short value = 256;
        byte[] result = ByteUtils.shortToBytes(value);
        assertEquals(2, result.length);
        assertEquals(1, result[0]);
        assertEquals(0, result[1]);
    }

    @Test
    void testShortToBytes_Zero() {
        short value = 0;
        byte[] result = ByteUtils.shortToBytes(value);
        assertEquals(2, result.length);
        assertEquals(0, result[0]);
        assertEquals(0, result[1]);
    }

    // ============ hexStringToBytes Tests ============

    @Test
    void testHexStringToBytes_WithPrefix() {
        String hex = "0x0102";
        byte[] result = ByteUtils.hexStringToBytes(hex);
        assertArrayEquals(new byte[]{1, 2}, result);
    }

    @Test
    void testHexStringToBytes_WithoutPrefix() {
        String hex = "0102";
        byte[] result = ByteUtils.hexStringToBytes(hex);
        assertArrayEquals(new byte[]{1, 2}, result);
    }

    @Test
    void testHexStringToBytes_OddLength() {
        String hex = "123";
        byte[] result = ByteUtils.hexStringToBytes(hex);
        assertEquals(2, result.length);
    }

    @Test
    void testHexStringToBytes_Null() {
        byte[] result = ByteUtils.hexStringToBytes(null);
        assertArrayEquals(ByteUtils.EMPTY_BYTE_ARRAY, result);
    }

    // ============ hostToBytes Tests ============

    @Test
    void testHostToBytes_Localhost() {
        byte[] result = ByteUtils.hostToBytes("127.0.0.1");
        assertEquals(4, result.length);
        assertEquals(127, result[0]);
        assertEquals(0, result[1]);
        assertEquals(0, result[2]);
        assertEquals(1, result[3]);
    }

    @Test
    void testHostToBytes_InvalidHost() {
        byte[] result = ByteUtils.hostToBytes("invalid.host.name.that.does.not.exist");
        assertEquals(4, result.length);
        // Should return 0.0.0.0 as fallback
        assertArrayEquals(new byte[]{0, 0, 0, 0}, result);
    }

    // ============ bytesToIp Tests ============

    @Test
    void testBytesToIp_Localhost() {
        byte[] bytes = new byte[]{127, 0, 0, 1};
        String result = ByteUtils.bytesToIp(bytes);
        assertEquals("127.0.0.1", result);
    }

    @Test
    void testBytesToIp_ZeroAddress() {
        byte[] bytes = new byte[]{0, 0, 0, 0};
        String result = ByteUtils.bytesToIp(bytes);
        assertEquals("0.0.0.0", result);
    }

    @Test
    void testBytesToIp_MaxAddress() {
        byte[] bytes = new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255};
        String result = ByteUtils.bytesToIp(bytes);
        assertEquals("255.255.255.255", result);
    }

    // ============ numberOfLeadingZeros Tests ============

    @Test
    void testNumberOfLeadingZeros_NoZeros() {
        byte[] bytes = new byte[]{1, 2, 3};
        int result = ByteUtils.numberOfLeadingZeros(bytes);
        assertEquals(7, result); // First byte 0x01 has 7 leading zeros
    }

    @Test
    void testNumberOfLeadingZeros_WithLeadingZeros() {
        byte[] bytes = new byte[]{0, 0, 1};
        int result = ByteUtils.numberOfLeadingZeros(bytes);
        assertEquals(23, result); // 2 full bytes + 7 bits
    }

    @Test
    void testNumberOfLeadingZeros_AllZeros() {
        byte[] bytes = new byte[]{0, 0, 0};
        int result = ByteUtils.numberOfLeadingZeros(bytes);
        assertEquals(24, result);
    }

    // ============ parseBytes Tests ============

    @Test
    void testParseBytes_NormalCase() {
        byte[] input = new byte[]{1, 2, 3, 4, 5};
        byte[] result = ByteUtils.parseBytes(input, 1, 3);
        assertArrayEquals(new byte[]{2, 3, 4}, result);
    }

    @Test
    void testParseBytes_OffsetTooHigh() {
        byte[] input = new byte[]{1, 2, 3};
        byte[] result = ByteUtils.parseBytes(input, 10, 5);
        assertArrayEquals(ByteUtils.EMPTY_BYTE_ARRAY, result);
    }

    @Test
    void testParseBytes_LengthZero() {
        byte[] input = new byte[]{1, 2, 3};
        byte[] result = ByteUtils.parseBytes(input, 0, 0);
        assertArrayEquals(ByteUtils.EMPTY_BYTE_ARRAY, result);
    }

    @Test
    void testParseBytes_LengthExceedsInput() {
        byte[] input = new byte[]{1, 2, 3};
        byte[] result = ByteUtils.parseBytes(input, 1, 10);
        assertEquals(10, result.length);
        assertEquals(2, result[0]);
        assertEquals(3, result[1]);
        // Rest should be zeros (default value)
    }

    // ============ parseWord Tests ============

    @Test
    void testParseWord_FirstWord() {
        byte[] input = new byte[64]; // 2 words
        input[31] = 5; // Set last byte of first word
        byte[] result = ByteUtils.parseWord(input, 0);
        assertEquals(32, result.length);
        assertEquals(5, result[31]);
    }

    @Test
    void testParseWord_SecondWord() {
        byte[] input = new byte[64]; // 2 words
        input[63] = 10; // Set last byte of second word
        byte[] result = ByteUtils.parseWord(input, 1);
        assertEquals(32, result.length);
        assertEquals(10, result[31]);
    }

    @Test
    void testParseWord_WithOffset() {
        byte[] input = new byte[96]; // 3 words
        input[95] = 15;
        byte[] result = ByteUtils.parseWord(input, 32, 1);
        assertEquals(32, result.length);
        assertEquals(15, result[31]);
    }

    // ============ trimLeadingBytes Tests ============

    @Test
    void testTrimLeadingBytes_NormalCase() {
        byte[] bytes = new byte[]{0, 0, 1, 2, 3};
        byte[] result = ByteUtils.trimLeadingBytes(bytes, (byte) 0);
        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    @Test
    void testTrimLeadingBytes_NoLeadingBytes() {
        byte[] bytes = new byte[]{1, 2, 3};
        byte[] result = ByteUtils.trimLeadingBytes(bytes, (byte) 0);
        assertArrayEquals(bytes, result);
    }

    @Test
    void testTrimLeadingBytes_AllSameValue() {
        byte[] bytes = new byte[]{5, 5, 5, 5};
        byte[] result = ByteUtils.trimLeadingBytes(bytes, (byte) 5);
        assertEquals(1, result.length);
        assertEquals(5, result[0]);
    }

    @Test
    void testTrimLeadingZeroes_Convenience() {
        byte[] bytes = new byte[]{0, 0, 1, 2};
        byte[] result = ByteUtils.trimLeadingZeroes(bytes);
        assertArrayEquals(new byte[]{1, 2}, result);
    }

    // ============ encodeDataList Tests ============

    @Test
    void testEncodeDataList_SingleNumericValue() {
        byte[] result = ByteUtils.encodeDataList("255");
        assertEquals(32, result.length);
        assertEquals((byte) 0xFF, result[31]);
    }

    @Test
    void testEncodeDataList_MultipleValues() {
        byte[] result = ByteUtils.encodeDataList("1", "2");
        assertEquals(64, result.length); // 2 * 32 bytes
    }

    @Test
    void testEncodeDataList_HexValue() {
        byte[] result = ByteUtils.encodeDataList("0xFF");
        assertEquals(32, result.length);
        assertEquals((byte) 0xFF, result[31]);
    }

    @Test
    void testEncodeDataList_StringValue() {
        byte[] result = ByteUtils.encodeDataList("test");
        assertEquals(32, result.length);
    }

    @Test
    void testEncodeDataList_TooLargeValue() {
        // Create a string representing a value larger than 32 bytes
        StringBuilder largeValue = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            largeValue.append("9");
        }
        assertThrows(RuntimeException.class, () -> {
            ByteUtils.encodeDataList(largeValue.toString());
        });
    }
}
