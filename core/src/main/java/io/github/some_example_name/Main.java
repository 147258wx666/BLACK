package io.github.some_example_name;

import com.badlogic.gdx.Game;
import io.github.some_example_name.core.TitleScreen;
import io.github.some_example_name.core.WorldScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    @Override
    public void create() {
//        setScreen(new WorldScreen("maps/forest1/map.tmx",40,500 ));
        setScreen(new TitleScreen());
    }
}
