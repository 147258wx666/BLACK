package io.github.some_example_name.data;

import com.badlogic.gdx.utils.Array;

public class SaveData {
    public String mapPath;
    public float playerX;
    public float playerY;
    public String playerGender;  // "MALE" 或 "FEMALE"
    public int corruptionLevel;
    public Array<String> inventory = new Array<>();
    public String saveTime;

    public SaveData() {}
}
