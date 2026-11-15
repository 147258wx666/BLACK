package io.github.some_example_name.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.some_example_name.data.GameState;
import io.github.some_example_name.systems.DialogueSystem;

public class RedHoodNPC extends BaseNPC {

    private enum State { PATROLLING, TALKING, RUNNING_AWAY, PICKING_FLOWERS }
    private State currentState = State.PATROLLING;

    private int actNumber;
    private float patrolX1, patrolX2, currentTarget;

    public RedHoodNPC(float x, float y, int actNumber) {
        super(x, y, 52, 52, "images/p2.png", 80f);
        this.actNumber = actNumber;
    }

    public void startPatrol(float x1, float x2) {
        this.patrolX1 = x1;
        this.patrolX2 = x2;
        this.currentTarget = x2;
        this.currentState = State.PATROLLING;
        this.blockPortal = true;
        super.moveTo(currentTarget, getY());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (currentState == State.PATROLLING && !isMoving) {
            currentTarget = (currentTarget == patrolX2) ? patrolX1 : patrolX2;
            super.moveTo(currentTarget, getY());
        }

        if (currentState == State.RUNNING_AWAY && getX() > 800f) {
            this.remove();
            GameState.unlockStory();
            this.blockPortal = false;
        }
    }


    @Override
    public void updateLogic(float delta, Player player, DialogueSystem dialogueSystem) {
        if (currentState == State.PATROLLING) {
            float dist = com.badlogic.gdx.math.Vector2.dst(
                player.getX() + player.getWidth()/2, player.getY() + player.getHeight()/2,
                getX() + getWidth()/2, getY() + getHeight()/2
            );
            if (dist < 60f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                triggerStory(player, dialogueSystem);
            }
        }
    }

    private void triggerStory(Player player, DialogueSystem dialogueSystem) {
        currentState = State.TALKING;
        super.stopMoving();
        GameState.lockStory();

        String scriptPath = (actNumber == 3)
            ? "dialogues/redHood_act3.json"
            : "dialogues/redHood_default.json";

        dialogueSystem.startSequence(scriptPath, () -> {
            if (actNumber == 3) {
                currentState = State.PICKING_FLOWERS;
                setSpeed(120f);
                super.moveTo(600f, getY() + 20, () -> {
                    GameState.unlockStory();
                    this.blockPortal = false;
                });
            } else {
                currentState = State.RUNNING_AWAY;
                setSpeed(350f);
                super.moveTo(1500f, getY());
            }
        });
    }
}
