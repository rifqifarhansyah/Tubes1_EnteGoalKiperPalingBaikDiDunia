package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    public PlayerAction playerAction;
    private GameState gameState;
    // private GameState prevState = null;
    public Integer tick = null;
    public Integer subtick = null;
    public UUID objectTracker = null;
    private int headingTele = -1;

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

    public int teleportSerang(PlayerAction playerAction, int firedTeleport) {
        // Punya return value yang menandakan heading teleporter dan bahwa telah
        // menembakkan teleporter
        playerAction.action = PlayerActions.FIRETELEPORT;

        if (!gameState.getGameObjects().isEmpty()) {
            // Kalau belum pakai radar
            var playerList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            playerAction.heading = getHeadingBetween(bot, playerList.get(1));
        }

        return playerAction.heading;
    }

    public int checkTeleportSerang(PlayerAction playerAction, int headingTele) {
        var teleporterList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER &&
                        item.getCurrentHeading() == headingTele) // Untuk mengetahui apakah teleporter punya kita atau
                                                                 // bukan, karena heading kemungkinan besar unik
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        var enemyList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(teleporterList.get(0), item)))
                .collect(Collectors.toList());

        if (!teleporterList.isEmpty()) { // Jika teleporter masih ada dalam permainan
            var teleporterDistance = getDistanceBetween(teleporterList.get(0), enemyList.get(1));
            if (teleporterDistance <= bot.getSize() + 10) { // Jika radius tambah 10 kurang dari jarak teleporter ke
                                                            // musuh terdekat
                playerAction.action = PlayerActions.TELEPORT;
                return -1;
            }

            // Return heading awal, tunggu tick selanjutnya
            // jika pada tick2 selanjutnya teleporter melebihi boundary maka otomatis hilang
            return headingTele;
        } else {
            return -1;
        }
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        var objectState = gameState.getGameObjects();
        var playerState = gameState.getPlayerGameObjects();

        var objectRadar = new Radar();

        if (!objectState.isEmpty() && !playerState.isEmpty()) {
            if (gameState.getWorld().getCurrentTick() != tick) {
                System.out.println("tick: " + gameState.getWorld().getCurrentTick());
                var warning = objectRadar.checkRadar(gameState, bot);
                // System.out.println(warning);
                if (warning != null) {

                    if (warning.gameObjectType == ObjectTypes.SUPERNOVA_BOMB || objectRadar.supernovaDefend) {
                        // Jika muncul bahaya supernova bomb

                    } else if (warning.gameObjectType == ObjectTypes.TELEPORTER || objectRadar.teleportDefend) {
                        // Jika muncul bahaya teleporter

                    } else if (warning.gameObjectType == ObjectTypes.TORPEDO_SALVO || objectRadar.torpedoDefend) {
                        // Jika muncul bahaya torpedo

                    } else if (warning.gameObjectType == ObjectTypes.PLAYER) {
                        // System.out.println(
                        // "1. " + warning.getGameObjectType() + " heading: " +
                        // warning.getCurrentHeading());
                        // Jika muncul bahaya player
                        // System.out.println(bot.getEffects());
                        playerAction = GreedyCommand.run(warning, playerAction, objectTracker, bot);
                        objectTracker = warning.getId();
                        // System.out.println(warning.getCurrentHeading() + " "
                        // +bot.getCurrentHeading());

                    }

                } else {
                    // if (headingTele != -1) {
                    //     headingTele = checkTeleportSerang(playerAction, headingTele);
                    // } else {
                    //     objectTracker = null;
                    //     playerAction.action = PlayerActions.STOP;
                    //     var enemyList = gameState.getPlayerGameObjects()
                    //             .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER)
                    //             .sorted(Comparator
                    //                     .comparing(item -> getDistanceBetween(bot, item)))
                    //             .collect(Collectors.toList());

                    //     if (bot.getTeleCount() != 0) {
                    //         if (bot.getSize() > enemyList.get(1).getSize() + 20) {
                    //             headingTele = teleportSerang(playerAction, headingTele);
                    //         }
                    //     }
                    // }

                    System.out.println("aman");

                    objectTracker = null;
                    if (bot.getAfterburnerStatus()) {
                        playerAction.action = PlayerActions.STOPAFTERBURNER;
                    } else {
                        GreedyCommand.catchFood(gameState.getGameObjects(), bot, playerAction);
                    }
                    // playerAction.action = PlayerActions.STOP;
                }
                GreedyCommand.evadeObject(bot, objectState, gameState.getWorld(), playerAction);

            }

            if (gameState.getWorld().getCurrentTick() != tick) {
                // prevState = gameState;
                tick = gameState.getWorld().getCurrentTick();
                System.out.println();
            }
            this.playerAction = playerAction;
        }
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

    public static int getHeadingBetween(GameObject bot, int x, int y) {
        var direction = toDegrees(Math.atan2(y - bot.getPosition().y, x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    public static int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
}
