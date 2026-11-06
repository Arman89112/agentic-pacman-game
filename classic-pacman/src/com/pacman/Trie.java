package com.pacman;

/**
 * Phase 5 data structure: a Trie (prefix tree) holding the cheat-code
 * vocabulary of the game.
 *
 * <p>Each node has up to 26 children (one per lower-case letter).  Inserting
 * "speedup" walks/creates the chain s -> p -> e -> e -> d -> u -> p and marks
 * the last node with a non-null {@code word}.  Matching is therefore O(L) for
 * an L-character command and never needs to re-scan earlier keystrokes - the
 * cursor position inside the tree *is* the memory of what has been typed so
 * far (the "tokenization" step of the command engine).
 */
public final class Trie {

    /** One vertex of the tree. */
    public static final class Node {
        public final Node[] next = new Node[26];
        /** Non-null when a complete command ends on this vertex. */
        public String word;
    }

    private final Node root = new Node();

    public Node root() {
        return root;
    }

    /** Inserts a lower-case word and marks its last node as a command. */
    public void insert(String word) {
        Node at = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            int k = c - 'a';
            if (k < 0 || k >= 26) {
                throw new IllegalArgumentException("Trie only stores a-z, got '" + c + "'");
            }
            if (at.next[k] == null) {
                at.next[k] = new Node();
            }
            at = at.next[k];
        }
        at.word = word;
    }

    /** Follows one character; returns the child node or {@code null}. */
    public static Node step(Node from, char c) {
        if (from == null) {
            return null;
        }
        int k = c - 'a';
        if (k < 0 || k >= 26) {
            return null;
        }
        return from.next[k];
    }
}
