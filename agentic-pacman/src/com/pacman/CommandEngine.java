package com.pacman;

/**
 * Phase 5: the hidden command engine sitting on top of the {@link Trie}.
 *
 * <p>Every letter the player types - including the letters of the movement keys
 * - is fed to {@link #feed(char)}.  The engine keeps one cursor inside the trie
 * that remembers the partial word typed so far, so a command is recognised in
 * O(L) time and without ever blocking the game loop.  As soon as the cursor
 * lands on a node that ends a command, the command name is returned and the
 * cursor is reset, ready for the next secret code.
 *
 * <p>Effects (speed boost, freeze, god mode) are applied by {@link Game},
 * which owns the actual game state.
 */
public final class CommandEngine {

    private final Trie trie = new Trie();
    private Trie.Node cursor;

    public CommandEngine() {
        trie.insert("speedup");
        trie.insert("freeze");
        trie.insert("godmode");
        reset();
    }

    /** Forgets any partially typed command. */
    public void reset() {
        cursor = trie.root();
    }

    /**
     * Feeds one keystroke.
     *
     * @return the completed command (e.g. "godmode"), or {@code null}
     */
    public String feed(char ch) {
        char c = Character.toLowerCase(ch);
        if (c < 'a' || c > 'z') {
            return null;
        }
        Trie.Node next = Trie.step(cursor, c);
        if (next == null) {
            // The partial word was garbage.  This character may still be the
            // first letter of a fresh command (e.g. "..xgodmode"), so restart
            // from the root instead of dropping it on the floor.
            reset();
            next = Trie.step(cursor, c);
            if (next == null) {
                return null;
            }
        }
        cursor = next;
        if (cursor.word != null) {
            String done = cursor.word;
            reset();
            return done;
        }
        return null;
    }

    /** Node currently matched - only used by the debug overlay / tests. */
    public Trie.Node cursor() {
        return cursor;
    }
}
