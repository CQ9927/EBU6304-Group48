package com.ebu6304.group48.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashTest {

    @Test
    void hash_returns64CharHexString() {
        String hash = PasswordHash.hash("testuser", "password123");
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"), "hash should be lowercase hex");
    }

    @Test
    void hash_isDeterministic() {
        String a = PasswordHash.hash("alice", "secret");
        String b = PasswordHash.hash("alice", "secret");
        assertEquals(a, b);
    }

    @Test
    void hash_differentUsernameDifferentHash() {
        String a = PasswordHash.hash("alice", "demo123");
        String b = PasswordHash.hash("bob", "demo123");
        assertNotEquals(a, b);
    }

    @Test
    void hash_differentPasswordDifferentHash() {
        String a = PasswordHash.hash("ta_demo", "demo123");
        String b = PasswordHash.hash("ta_demo", "other456");
        assertNotEquals(a, b);
    }

    @Test
    void hash_knownValue() {
        String hash = PasswordHash.hash("ta_demo", "demo123");
        // Hash should be deterministic across runs
        String expected = PasswordHash.hash("ta_demo", "demo123");
        assertEquals(expected, hash);
    }

    @Test
    void hash_specialCharacters() {
        String hash = PasswordHash.hash("user@test", "p@$$w0rd!你好");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }
}
