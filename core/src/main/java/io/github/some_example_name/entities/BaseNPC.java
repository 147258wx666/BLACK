package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.some_example_name.systems.DialogueSystem;

public abstract class BaseNPC extends Actor {
    public enum Direction { DOWN, LEFT, UP, RIGHT }

    // 动画相关
    protected Animation<TextureRegion> animDown, animLeft, animUp, animRight;
    protected Texture texture;
    protected float stateTime = 0f;
    protected Direction currentDir = Direction.DOWN;

    // 移动相关
    protected float speed;
    protected Vector2 moveTarget = null;
    protected boolean isMoving = false;

    // 到达目的地后的回调函数
    private Runnable onArriveCallback = null;
    protected boolean blockPortal = false;

    public boolean isBlockingPortal() {
        return blockPortal;
    }

    public BaseNPC(float x, float y, float width, float height, String texturePath, float speed) {
        setBounds(x, y, width, height);
        this.speed = speed;

        // 1. 加载并切割贴图 (默认 4x4 布局：下、左、上、右)
        texture = new Texture(texturePath);
        TextureRegion[][] temp = TextureRegion.split(texture, texture.getWidth() / 4, texture.getHeight() / 4);

        animDown = createAnim(temp[0]);
        animLeft = createAnim(temp[1]);
        animUp = createAnim(temp[2]);
        animRight = createAnim(temp[3]);
    }

    public void dispose() {
        texture.dispose();
    }

    private Animation<TextureRegion> createAnim(TextureRegion[] frames) {
        Animation<TextureRegion> anim = new Animation<>(0.15f, frames);
        anim.setPlayMode(Animation.PlayMode.LOOP);
        return anim;
    }

    // ==========================================
    //  核心移动控制方法
    // ==========================================

    // 普通移动
    public void moveTo(float targetX, float targetY) {
        moveTo(targetX, targetY, null);
    }

    // 带有到达回调的移动
    public void moveTo(float targetX, float targetY, Runnable onArrive) {
        this.moveTarget = new Vector2(targetX, targetY);
        this.isMoving = true;
        this.onArriveCallback = onArrive;
    }

    public void stopMoving() {
        this.isMoving = false;
        this.moveTarget = null;
        this.onArriveCallback = null;
        this.stateTime = 0; // 停止时重置动画，防止停在奇怪的动作上
    }

    // ==========================================
    //  核心生命周期
    // ==========================================

    @Override
    public void act(float delta) {
        super.act(delta);

        if (isMoving && moveTarget != null) {
            Vector2 dir = new Vector2(moveTarget.x - getX(), moveTarget.y - getY());
            float distance = dir.len();

            // 判定是否到达目标
            if (distance < 5f) {
                setPosition(moveTarget.x, moveTarget.y);
                isMoving = false;
                moveTarget = null;

                // 到达目的地，执行回调
                if (onArriveCallback != null) {
                    onArriveCallback.run();
                    onArriveCallback = null;
                }
            } else {
                dir.nor();

                // 执行移动
                moveBy(dir.x * speed * delta, dir.y * speed * delta);

                // 自动计算动画朝向
                if (Math.abs(dir.x) > Math.abs(dir.y)) {
                    currentDir = dir.x > 0 ? Direction.RIGHT : Direction.LEFT;
                } else {
                    currentDir = dir.y > 0 ? Direction.UP : Direction.DOWN;
                }

                stateTime += delta;
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion currentFrame;

        if (!isMoving) {
            // 静止时，默认显示朝下（DOWN）的第一帧
            currentFrame = animDown.getKeyFrame(0);
        } else {
            switch (currentDir) {
                case LEFT:  currentFrame = animLeft.getKeyFrame(stateTime); break;
                case UP:    currentFrame = animUp.getKeyFrame(stateTime); break;
                case RIGHT: currentFrame = animRight.getKeyFrame(stateTime); break;
                case DOWN:
                default:    currentFrame = animDown.getKeyFrame(stateTime); break;
            }
        }

        batch.draw(currentFrame, getX(), getY(), getWidth(), getHeight());
    }
    // 动态修改移动速度（比如巡逻慢，逃跑快）
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    // 抽象方法：强迫所有子类实现自己的 AI 逻辑
    public abstract void updateLogic(float delta, Player player, DialogueSystem dialogueSystem);
}


