package io.github.some_example_name.entities;

import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.systems.DialogueSystem;

public interface Interactable {
    // 当玩家靠近并按下交互键（或自动触发）时调用
    void onInteract(Player player, DialogueSystem dialogueSystem);

    // 判断玩家是否在交互范围内
    boolean isPlayerInRange(Player player);
}
