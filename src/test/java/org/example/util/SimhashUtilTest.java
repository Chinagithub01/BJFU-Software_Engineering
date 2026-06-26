package org.example.util;

import org.junit.Assert;
import org.junit.Test;

public class SimhashUtilTest {

    @Test
    public void identicalTextSameHash() {
        String text = "软件工程课程设计 PeerReview 互评平台 simhash 查重测试";
        long a = SimhashUtil.compute(text);
        long b = SimhashUtil.compute(text);
        Assert.assertEquals(a, b);
        Assert.assertEquals(0, SimhashUtil.hammingDistance(a, b));
    }

    @Test
    public void similarTextLowHamming() {
        String a = "public class HelloWorld { public static void main(String[] args) { System.out.println(\"hi\"); } }";
        String b = "public class HelloWorld { public static void main(String[] args) { System.out.println(\"hello\"); } }";
        long ha = SimhashUtil.compute(a);
        long hb = SimhashUtil.compute(b);
        Assert.assertTrue(SimhashUtil.hammingDistance(ha, hb) <= 10);
    }

    @Test
    public void hexRoundTrip() {
        long hash = SimhashUtil.compute("round trip");
        String hex = SimhashUtil.toHex(hash);
        Assert.assertEquals(hash, SimhashUtil.fromHex(hex));
    }
}
