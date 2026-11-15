package io.github.some_example_name.data;

import com.badlogic.gdx.utils.Array;

public class GameState {
    public static boolean isStoryActive = false;

    public static Array<String> inventory = new Array<>();

    public static void lockStory() { isStoryActive = true; }
    public static void unlockStory() { isStoryActive = false; }


    public static void addItem(String itemId) {
        if (!inventory.contains(itemId, false)) {
            inventory.add(itemId);
        }
    }

    public static boolean hasItem(String itemId) {
        return inventory.contains(itemId, false);
    }
}
