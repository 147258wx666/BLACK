package io.github.some_example_name.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ScreenFilterSystem {
    private SpriteBatch batch;
    private Texture whitePixel;
    private static int corruptionLevel = 0;

    public static int getCorruptionLevel() {
        return corruptionLevel;
    }
    public ScreenFilterSystem() {
        batch = new SpriteBatch();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();
    }

    // 增加崩坏等级（每次切森林图时调用）
    public static void addCorruption() {
        corruptionLevel++;
        if (corruptionLevel > 3) corruptionLevel = 3; // 最高限制3级，防止全红看不见
    }

    // 恢复崩坏等级（剧情需要恢复时调用）
    public static void resetCorruption() {
        corruptionLevel = 0;
    }

    // 手动设置特定等级（如果需要）
    public static void setCorruption(int level) {
        corruptionLevel = Math.max(0, Math.min(3, level));
    }

    // ==========================================

    public void render() {
        if (corruptionLevel <= 0) return;

        // 计算透明度 (1级0.2，2级0.4，3级0.6)
        float alpha = corruptionLevel * 0.2f;

        // 使用纯屏幕坐标，不受摄像机影响
        batch.setProjectionMatrix(batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        batch.begin();
        batch.setColor(0.6f, 0f, 0f, alpha); // 血红滤镜
        batch.draw(whitePixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(1, 1, 1, 1);
        batch.end();
    }

    public void dispose() {
        batch.dispose();
        whitePixel.dispose();
    }
}
