package io.github.some_example_name.data;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * 对话数据模型：一段完整对话序列，从 JSON 文件加载。
 * <p>
 * JSON 结构示例：
 * <pre>
 * {
 *   "characters": {
 *     "redHood": { "name": "小红帽", "portrait": "lihui/RedHood.png" },
 *     "wolf":    { "name": "大灰狼", "portrait": "lihui/wolf.png" }
 *   },
 *   "startNodeId": "n1",
 *   "nodes": [
 *     { "id": "n1", "speaker": "redHood", "text": "...", "next": "n2" },
 *     { "id": "n2", "speaker": "redHood", "text": "...", "choices": [
 *       { "text": "选项A", "next": "n3" },
 *       { "text": "选项B", "next": "n4" }
 *     ]},
 *     { "id": "n3", "speaker": "redHood", "text": "...", "next": null },
 *     { "id": "n4", "speaker": "wolf",    "text": "...", "next": null }
 *   ]
 * }
 * </pre>
 */
public class DialogueData {
    public ObjectMap<String, CharacterDef> characters = new ObjectMap<>();
    public Array<DialogueNode> nodes = new Array<>();
    public String startNodeId;

    /** 角色定义 */
    public static class CharacterDef {
        public String name;
        public String portrait;
    }

    /** 单个对话节点 */
    public static class DialogueNode {
        public String id;
        public String speaker;
        public String text;
        public String next;
        public Array<ChoiceOption> choices;
    }

    /** 选择选项 */
    public static class ChoiceOption {
        public String text;
        public String next;
    }
}
