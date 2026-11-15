package io.github.some_example_name.systems;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class MapManager {
    public Array<Rectangle> walls = new Array<>();
    public Array<MapObject> portals = new Array<>();

    // 它现在只接收加载好的地图，专职提取碰撞和传送门！
    public void extractData(TiledMap map) {
        walls.clear();
        portals.clear();

        // 1. 提取碰撞墙
        MapLayer collisionLayer = map.getLayers().get("Collision");
        if (collisionLayer != null) {
            for (MapObject obj : collisionLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    walls.add(((RectangleMapObject) obj).getRectangle());
                }
            }
        }

        // 2. 提取传送门
        MapLayer portalLayer = map.getLayers().get("Portals");
        if (portalLayer != null) {
            for (MapObject obj : portalLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    portals.add(obj);
                }
            }
        }
    }
}
