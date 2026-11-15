package io.github.some_example_name.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

/**
 * 统一的屏幕切换入口。
 * libGDX 的 Game.setScreen 不会释放旧 Screen，直接调用会泄漏整套 Texture/Stage。
 * 通过本类切换时，旧 Screen 会在当前帧渲染结束后被 dispose。
 * <p>
 * 注意：dispose 必须延迟到下一帧（postRunnable），因为切换通常发生在
 * 旧 Screen 的 render() 内部，若立即 dispose，后续代码会继续使用已销毁的
 * batch/texture，导致黑屏甚至崩溃。
 */
public class ScreenManager {
    public static void switchTo(Screen next) {
        com.badlogic.gdx.Game game = (com.badlogic.gdx.Game) Gdx.app.getApplicationListener();
        Screen old = game.getScreen();
        game.setScreen(next);
        if (old != null) {
            Gdx.app.postRunnable(old::dispose);
        }
    }
}
