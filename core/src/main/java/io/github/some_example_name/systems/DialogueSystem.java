package io.github.some_example_name.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import io.github.some_example_name.core.ChoiceBox;
import io.github.some_example_name.core.ChoiceBox.OnChoiceSelectedListener;
import io.github.some_example_name.core.DialogueBox;
import io.github.some_example_name.data.DialogueData;
import io.github.some_example_name.data.DialogueData.ChoiceOption;
import io.github.some_example_name.data.DialogueData.CharacterDef;
import io.github.some_example_name.data.DialogueData.DialogueNode;

/**
 * 数据驱动的对话序列播放器。
 * <p>
 * 从 JSON 文件加载对话数据，按节点序列播放，支持：
 * - 多角色对话（通过 characters 角色表自动切换名字和立绘）
 * - 分支选择（ChoiceNode 弹出 ChoiceBox 让玩家选择）
 * - 空格推进 / 跳过打字机
 * <p>
 * NPC 只需调用 {@link #startSequence(String, Runnable)} 传入 JSON 路径和结束回调。
 */
public class DialogueSystem implements OnChoiceSelectedListener {
    private Stage uiStage;
    private DialogueBox dialogueBox;
    private ChoiceBox choiceBox;

    private DialogueData data;
    private DialogueNode currentNode;
    private Runnable onComplete;

    /** id → node 快速查找表 */
    private ObjectMap<String, DialogueNode> nodeMap = new ObjectMap<>();

    private static final Json json = new Json();

    public DialogueSystem(Stage uiStage, DialogueBox dialogueBox, ChoiceBox choiceBox) {
        this.uiStage = uiStage;
        this.dialogueBox = dialogueBox;
        this.choiceBox = choiceBox;
        this.choiceBox.setListener(this);
        this.uiStage.addActor(dialogueBox);
        this.uiStage.addActor(choiceBox);
    }

    /**
     * 开始播放一段对话序列。
     *
     * @param jsonPath   对话 JSON 文件的 assets 内部路径，如 "dialogues/redHood_default.json"
     * @param onComplete 整段对话结束后的回调（可为 null）
     */
    public void startSequence(String jsonPath, Runnable onComplete) {
        String jsonText = Gdx.files.internal(jsonPath).readString();
        this.data = json.fromJson(DialogueData.class, jsonText);
        this.onComplete = onComplete;

        // 构建 id → node 查找表
        nodeMap.clear();
        for (DialogueNode node : data.nodes) {
            nodeMap.put(node.id, node);
        }

        // 播放起始节点
        playNode(data.startNodeId);
    }

    /** 播放指定 id 的节点 */
    private void playNode(String nodeId) {
        currentNode = nodeMap.get(nodeId);
        if (currentNode == null) {
            Gdx.app.error("DialogueSystem", "节点不存在: " + nodeId);
            finishSequence();
            return;
        }

        // 查角色表，设置立绘和名字
        CharacterDef ch = data.characters.get(currentNode.speaker);
        String displayName = (ch != null) ? ch.name : currentNode.speaker;
        String portraitPath = (ch != null) ? ch.portrait : "";

        dialogueBox.setAvatar(portraitPath);
        dialogueBox.setText(displayName, currentNode.text);

        // 如果是选择节点，显示选项 UI（等打字完成后才显示，但 ChoiceBox 会等待）
        if (currentNode.choices != null && currentNode.choices.size > 0) {
            // 延迟显示选择框：等文本打完后再弹出
            // 这里先不显示，在 advance 时判断
        }
    }

    /** 在 WorldScreen 的 render 中每帧调用 */
    public void update(float delta) {
        if (!dialogueBox.isVisible()) return;

        // 1. 更新打字机动画
        dialogueBox.updateTyping(delta);

        // 2. 选择模式：由 ChoiceBox 接管输入
        if (choiceBox.isActive()) {
            choiceBox.update(delta);
            return;
        }

        // 3. 检测空格键推进
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (dialogueBox.isTyping()) {
                // 正在打字，瞬间跳过
                dialogueBox.skipTyping();
            } else if (dialogueBox.isFinished()) {
                advance();
            }
        }
    }

    /** 推进到下一节点或结束本段对话 */
    private void advance() {
        // 如果当前节点有选择项，弹出 ChoiceBox 等待玩家选择
        if (currentNode.choices != null && currentNode.choices.size > 0) {
            choiceBox.show(currentNode.choices);
            return;
        }

        // 普通节点：跳到 next 或结束
        if (currentNode.next != null) {
            playNode(currentNode.next);
        } else {
            finishSequence();
        }
    }

    /** 结束本段对话 */
    private void finishSequence() {
        dialogueBox.setVisible(false);
        if (onComplete != null) {
            Runnable cb = onComplete;
            onComplete = null;
            cb.run();
        }
    }

    /** ChoiceBox 选中某选项后的回调 */
    @Override
    public void onChoiceSelected(ChoiceOption option) {
        choiceBox.hide();
        if (option.next != null) {
            playNode(option.next);
        } else {
            finishSequence();
        }
    }

    public boolean isTalking() {
        return dialogueBox.isVisible();
    }
}
