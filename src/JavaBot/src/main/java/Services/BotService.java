package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    public PlayerAction playerAction;
    private GameState gameState;
    private GameState prevState = null;
    public Integer tick = null;
    public UUID objectTracker = null;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }

    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {        

        var objectState = gameState.getGameObjects();
        var playerState = gameState.getPlayerGameObjects();

        var objectRadar = new Radar();

        if (!objectState.isEmpty() && !playerState.isEmpty() && prevState != null) {
            if (gameState.getWorld().getCurrentTick() != tick) {
                System.out.println(gameState.getWorld().getCurrentTick());
                var warning = objectRadar.checkRadar(gameState, prevState, bot);
                // System.out.println(warning);
                if (warning != null) {

                    if (warning.gameObjectType == ObjectTypes.SUPERNOVA_BOMB || objectRadar.supernovaDefend) {
                        // Jika muncul bahaya supernova bomb

                    } else if (warning.gameObjectType == ObjectTypes.TELEPORTER || objectRadar.teleportDefend) {
                        // Jika muncul bahaya teleporter

                    } else if (warning.gameObjectType == ObjectTypes.TORPEDO_SALVO || objectRadar.torpedoDefend) {
                        // Jika muncul bahaya torpedo

                    } else if (warning.gameObjectType == ObjectTypes.PLAYER) {
                        System.out.println(
                                "1. " + warning.getGameObjectType() + "   heading:  " + warning.getCurrentHeading());
                        // Jika muncul bahaya player
                        // System.out.println(bot.getEffects());
                        playerAction = GreedyCommand.run(warning, playerAction, objectTracker, bot);
                        objectTracker = warning.getId();
                        // System.out.println(warning.getCurrentHeading() + " "
                        // +bot.getCurrentHeading());

                    }

                } else {
                    System.out.println("aman");
                    objectTracker = null;
                    playerAction.action = PlayerActions.STOP;
                }
                
            }
        }

        if (gameState.getWorld().getCurrentTick() != tick) {
            prevState = gameState;
            tick = gameState.getWorld().getCurrentTick();
        }
        this.playerAction = playerAction;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream()
                .filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    public static double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    public static int getHeadingBetween(GameObject bot, GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    public static int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
}
