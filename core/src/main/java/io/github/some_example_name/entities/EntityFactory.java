package io.github.some_example_name.entities;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;

public class EntityFactory {

    public static BaseNPC createNPC(MapObject object) {
        if (!(object instanceof RectangleMapObject)) return null;

        Rectangle rect = ((RectangleMapObject) object).getRectangle();
        float x = rect.x;
        float y = rect.y;

        // 获取实体的类型
        String type = object.getProperties().get("type", String.class);
        if (type == null) return null; // 没写类型的跳过

        // 根据类型生成对应实体
        if ("RedHood".equals(type)) {
            // 1. 安全读取 actNum (Int)
            Integer actObj = object.getProperties().get("actNum", Integer.class);
            int actNum = (actObj != null) ? actObj : 1;

            // 2. 安全读取巡逻范围 (Float)
            Float leftObj = object.getProperties().get("patrolLeft", Float.class);
            float patrolLeft = (leftObj != null) ? leftObj : x - 100; // 没填就默认左边100像素

            Float rightObj = object.getProperties().get("patrolRight", Float.class);
            float patrolRight = (rightObj != null) ? rightObj : x + 100; // 没填就默认右边100像素

            // 3. 创建并返回
            RedHoodNPC npc = new RedHoodNPC(x, y, actNum);
            npc.startPatrol(patrolLeft, patrolRight);
            return npc;

        } else if ("Wolf".equals(type)) {
            // 以后扩展：return new WolfNPC(x, y);
        }

        return null;
    }
}
