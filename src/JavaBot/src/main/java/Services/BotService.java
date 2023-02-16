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
    public GameObject launchedTeleport = null;
    public boolean justLaunchedTeleportStatus = false;

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

    public void teleportSerang(PlayerAction playerAction, GameObject enemy) {
        // Punya return value yang menandakan heading teleporter dan bahwa telah
        // menembakkan teleporter
        playerAction.action = PlayerActions.FIRETELEPORT;
        // Kalau belum pakai radar
        playerAction.heading = getHeadingBetween(bot, enemy);
        System.out.println("Attack with launch teleport " + "  bot size : " + bot.getSize()
                + "    enemy size: " + enemy.getSize() + "  distance : "
                + getDistanceBetween(bot, enemy));
        // return GameObject;
    }

    public GameObject checkTeleportSerang(PlayerAction p, GameObject bot, GameObject launchedTeleport,
            List<GameObject> enemyList, List<GameObject> teleportList) {

        boolean teleportStatus = false;
        if (launchedTeleport != null) {
            System.out.println("test 1");
            for (int i = 0; i < teleportList.size(); i++) {
                System.out.println(teleportList.get(i).getId() + "   " + launchedTeleport.getId());
                if (teleportList.get(i).getId().equals(launchedTeleport.getId())) {
                    teleportStatus = true;
                    launchedTeleport = teleportList.get(i);
                }
            }
            GameObject updateLaunceTeleport = launchedTeleport;
            System.out.println(updateLaunceTeleport);
            // System.out.println(updateLaunceTeleport);
            if (teleportStatus) {
                System.out.println("test 2");
                List<GameObject> closeEnemy = enemyList.stream()
                        .filter(item -> getDistanceBetween(item, updateLaunceTeleport) - bot.getSize() - item.getSize() < 20
                                && item.getId() != bot.getId())
                        .sorted(Comparator.comparing(
                                item -> getDistanceBetween(item, updateLaunceTeleport) - bot.getSize() - item.getSize()))
                        .collect(Collectors.toList());

                if (closeEnemy.size() > 0) {
                    System.out.println("detonate attack teleport  size " + bot.getSize() + "   enemy size :"
                            + closeEnemy.get(0).getSize());
                    p.action = PlayerActions.TELEPORT;
                    p.heading = getHeadingBetween(bot, closeEnemy.get(0));
                    return null;
                } else {
                    System.out.println("can't detonate, too far away");

                    return updateLaunceTeleport;
                }
            }
        }
        return null;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        var objectState = gameState.getGameObjects();
        var playerState = gameState.getGameObjects();
        var teleportState = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());

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
                System.out.println(launchedTeleport);
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
                            objectTracker = null;
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

                    } else if (closestEnemy != null
                            ? getDistanceBetween(closestEnemy, bot) - bot.getSize() - closestEnemy.getSize() < 20
                            : false) {
                        GreedyCommand.activateAfterburner(playerAction, bot, closestEnemy, true);
                    }

                } else if (!justLaunchedTeleportStatus && bot.getTeleCount() > 0 && bot.getSize() > 60
                        && (closestEnemy != null
                                ? bot.getSize() - closestEnemy.getSize() > 30
                                : false)
                        && launchedTeleport == null) {
                    teleportSerang(playerAction, closestEnemy);
                    justLaunchedTeleportStatus = true;

                } else if (launchTorpedo(bot, closestEnemy,
                        closestEnemy != null ? (closestEnemy.getSize() > 60 ? 400 : 200) : 200, playerAction, true)) {

                }

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
                if (justLaunchedTeleportStatus && teleportState.size() > 0
                        && playerAction.action != PlayerActions.FIRETELEPORT) {
                    launchedTeleport = teleportState.get(0);
                    justLaunchedTeleportStatus = false;
                }
                launchedTeleport = checkTeleportSerang(playerAction, bot, launchedTeleport, preyList, teleportState);
                System.out.println(launchedTeleport);
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
        boolean condition1 = bot.getSize() > (attack ? 35 : 20);
        boolean condition2 = false;

        if (attack) {
            condition2 = enemy != null
                    ? getDistanceBetween(bot, enemy) - bot.getSize() - enemy.getSize() < distanceTreshold
                            && enemy.getSize() > 20
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

        // if (attack && condition) {
        // p.action = PlayerActions.FORWARD;
        // p.heading = getHeadingBetween(bot, enemy);
        // }

        return condition;
    }
}
