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
    public UUID preyTracker = null;
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

    public int teleportSerang(PlayerAction playerAction, GameObject enemy) {
        // Punya return value yang menandakan heading teleporter dan bahwa telah
        // menembakkan teleporter
        playerAction.action = PlayerActions.FIRETELEPORT;
        // Kalau belum pakai radar
        playerAction.heading = getHeadingBetween(bot, enemy);
        System.out.println("Attack with launch teleport " + "  bot size : " + bot.getSize()
                + "    enemy size: " + enemy.getSize() + "  distance : "
                + getDistanceBetween(bot, enemy));

        return playerAction.heading;
    }

    public int checkTeleportSerang(PlayerAction playerAction, int headingTele, UUID preyTracker,
            List<GameObject> enemyList) {

        var teleporterList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER &&
                        item.getCurrentHeading() == headingTele) // Untuk mengetahui apakah teleporter punya kita atau
                                                                 // bukan, karena heading kemungkinan besar unik
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        var enemy = enemyList
                .stream().filter(item -> item.getId() == preyTracker)
                .collect(Collectors.toList());

        if (!teleporterList.isEmpty() && !enemy.isEmpty()) { // Jika teleporter masih ada dalam permainan
            var teleporterDistance = getDistanceBetween(teleporterList.get(0), enemy.get(0));
            if (teleporterDistance - bot.getSize() - enemy.get(0).getSize() <= 10) { // Jika radius tambah 10 kurang
                                                                                     // dari jarak teleporter ke
                // musuh terdekat
                System.out.println("Attack with detonate teleport " + "  bot size : " + bot.getSize()
                        + "    enemy size: " + enemy.get(0).getSize() + "  distance : "
                        + getDistanceBetween(bot, enemy.get(0)));
                playerAction.action = PlayerActions.TELEPORT;
                this.preyTracker = null;
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
        var playerState = gameState.getGameObjects();

        List<GameObject> preyList = gameState.getPlayerGameObjects().stream()
                .filter(item -> item.getSize() < bot.getSize())
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
        ;
        GameObject closestEnemy = null;
        if (preyList.size() > 0) {
            closestEnemy = preyList.get(0);
        }

        var objectRadar = new Radar();

        if (!objectState.isEmpty() && !playerState.isEmpty()) {
            if (gameState.getWorld().getCurrentTick() != tick) {
                System.out.println("tick: " + gameState.getWorld().getCurrentTick());
                var warning = objectRadar.checkRadar(gameState, bot);
                // System.out.println(warning);
                if (warning != null) {
                    if (warning.gameObjectType == ObjectTypes.SUPERNOVA_BOMB || objectRadar.supernovaDefend) {
                        // Jika muncul bahaya supernova bomb
                        playerAction = GreedyCommand.run(warning, playerAction, objectTracker, bot);
                        objectTracker = warning.getId();

                    } else if (warning.gameObjectType == ObjectTypes.TELEPORTER || objectRadar.teleportDefend) {
                        // Jika muncul bahaya teleporter
                        playerAction = GreedyCommand.run(warning, playerAction, objectTracker, bot);
                        objectTracker = warning.getId();

                    } else if (warning.gameObjectType == ObjectTypes.TORPEDO_SALVO || objectRadar.torpedoDefend) {
                        // Jika muncul bahaya torpedo
                        playerAction = GreedyCommand.run(warning, playerAction, objectTracker, bot);
                        objectTracker = warning.getId();

                    } else if (warning.gameObjectType == ObjectTypes.PLAYER) {
                        if (launchTorpedo(bot, warning, 150, playerAction, false)) {
                        } else {
                            playerAction = GreedyCommand.run(warning, playerAction, objectTracker, bot);
                            objectTracker = warning.getId();
                        }
                        // System.out.println(
                        // "1. " + warning.getGameObjectType() + " heading: " +
                        // warning.getCurrentHeading());
                        // Jika muncul bahaya player
                        // System.out.println(bot.getEffects());

                        // System.out.println(warning.getCurrentHeading() + " "
                        // +bot.getCurrentHeading());

                    }

                } else if (launchTorpedo(bot, closestEnemy,
                        closestEnemy != null ? closestEnemy.getSize() > 70 ? 300 : 200 : 200, playerAction, true)) {

                }

                // if (bot.getSize() > 35 && bot.getTorpedoCount() > 0 && closestEnemy != null
                // ? getDistanceBetween(bot, closestEnemy) - bot.getSize() -
                // closestEnemy.getSize() > 200
                // : false) {
                // GreedyCommand.launchTorpedo(bot, closestEnemy, closestEnemy != null ?
                // closestEnemy.getSize() > 70 ? 300 : 200 : 200, playerAction, true)
                // System.out.println("attack");
                // System.out.println("Attack with torpedo" + " bot size : " + bot.getSize()
                // + " enemy size: "
                // + closestEnemy.getSize() + " distance : "
                // + (getDistanceBetween(bot, closestEnemy) - bot.getSize() -
                // closestEnemy.getSize())
                // + " heading: "
                // + getHeadingBetween(bot, closestEnemy));
                // playerAction.action = PlayerActions.FIRETORPEDOES;
                // playerAction.heading = getHeadingBetween(bot, closestEnemy);
                // }
                // {
                // } else if (bot.getTeleCount() > 0 && bot.getSize() > 50 && closestEnemy !=
                // null
                // ? bot.getSize() - closestEnemy.getSize() > 40
                // : false) {
                // preyTracker = closestEnemy.getId();
                // headingTele = teleportSerang(playerAction, closestEnemy);

                // // if ()

                // }

                // playerAction.action = PlayerActions.STOP;
                else {
                    System.out.println("aman");

                    objectTracker = null;
                    if (bot.getAfterburnerStatus()) {
                        playerAction.action = PlayerActions.STOPAFTERBURNER;
                    } else {
                        GreedyCommand.catchFood(gameState.getGameObjects(), bot, playerAction);
                    }
                }
                objectTracker = GreedyCommand.evadeObject(bot, objectState, gameState.getWorld(), playerAction,
                        objectTracker);
                // if (headingTele != -1) {
                // headingTele = checkTeleportSerang(playerAction, headingTele, preyTracker,
                // playerState);
                // }

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

    public boolean launchTorpedo(GameObject bot, GameObject enemy, int distanceTreshold, PlayerAction p,
            boolean attack) {
        boolean condition1 = bot.getSize() > 35;
        boolean condition2 = false;

        if (attack) {
            condition2 = enemy != null
                    ? getDistanceBetween(bot, enemy) - bot.getSize() - enemy.getSize() < distanceTreshold
                    : false;
        } else {
            condition2 = getDistanceBetween(bot, enemy) - bot.getSize() - enemy.getSize() > distanceTreshold;
        }

        boolean condition3 = bot.getTorpedoCount() > 0;
        boolean condition = condition1 && condition2 && condition3;

        if (condition) {
            System.out
                    .println((attack ? "Attack with torpedo" : "Defend with torpedo") + "   bot size : " + bot.getSize()
                            + "    enemy size: "
                            + enemy.getSize() + "  distance : "
                            + (getDistanceBetween(bot, enemy) - bot.getSize() - enemy.getSize()) + "   heading:  "
                            + getHeadingBetween(bot, enemy));
            p.action = PlayerActions.FIRETORPEDOES;
            p.heading = getHeadingBetween(bot, enemy);
        }
        return condition;
    }
}
