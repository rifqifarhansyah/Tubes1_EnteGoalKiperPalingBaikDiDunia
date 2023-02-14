package Services;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import Enums.ObjectTypes;
import Enums.PlayerActions;
import Models.GameObject;
import Models.PlayerAction;
import Models.World;

public class GreedyCommand extends BotService {
  public static PlayerAction run(GameObject enemy, PlayerAction p, UUID objectTracker, GameObject bot) {

    System.out.println("speed: " + bot.getSpeed() + "  size:  " + bot.getSize() + "  status: "
        + bot.getAfterburnerStatus() + " effect:  " + bot.getEffects() + "  distance: "
        + (BotService.getDistanceBetween(enemy, bot) - enemy.getSize() - bot.getSize()));
    activateAfterburner(p, bot, enemy, false);

    if (objectTracker == null || !(objectTracker == null ? UUID.randomUUID() : objectTracker).equals(enemy.getId())) {
      p.heading = (enemy.currentHeading + 90) % 360;
    }
    return p;
  }

  public static void activateAfterburner(PlayerAction p, GameObject bot, GameObject enemy, boolean attack) {
    if (attack) {

    } else {
      // System.out.println(bot.getAfterburnerStatus());
      if (!bot.getAfterburnerStatus()) {
        if (bot.getSpeed() == 0) {
          // System.out.println("speed 0");
          p.action = PlayerActions.FORWARD;
        } else if (bot.getSize() > 30) {
          if (enemy.getGameObjectType() == ObjectTypes.PLAYER) {
            if (BotService.getDistanceBetween(bot, enemy) - bot.getSize() - enemy.getSize() < 30) {
              p.action = PlayerActions.STARTAFTERBURNER;
            } else {
              p.action = PlayerActions.FORWARD;
            }
          } else {
            p.action = PlayerActions.STARTAFTERBURNER;
          }

        } else if (bot.getSize() < 20) {
          // System.out.println("kurang dari 20");
          p.action = PlayerActions.FORWARD;
        }
      } else {
        if (bot.getSize() < 20) {
          p.action = PlayerActions.STOPAFTERBURNER;
        } else {
          p.action = PlayerActions.FORWARD;
        }
      }
    }
  }

  public static void checkBorder(GameObject bot, World w, PlayerAction p) {
    var condition = Math.sqrt(Math.pow(bot.getPosition().getX(), 2) + Math.pow(bot.getPosition().getY(), 2))
        + bot.getSize() + 40 > w.getRadius();
    if (condition) {
      System.out.println("Evade border   size: " + bot.getSize());
      p.heading = BotService.getHeadingBetween(bot, 0, 0);
      p.action = PlayerActions.FORWARD;
    }
  }

  public static void checkHarmfulObject(GameObject bot, List<GameObject> gameObj, PlayerAction p,
      ObjectTypes objectType, int treshold, String text) {
    var ObjList = gameObj.stream()
        .filter(
            item -> item.getGameObjectType() == objectType)
        .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)))
        .collect(Collectors.toList());
    if (ObjList.size() > 0) {
      var condition = BotService.getDistanceBetween(bot, ObjList.get(0)) - bot.getSize()
          - ObjList.get(0).getSize() < treshold;
      if (condition) {
        System.out.println(text + "  size: " + bot.getSize());
        p.heading = (BotService.getHeadingBetween(bot, ObjList.get(0)) + 180) % 360;
        p.action = PlayerActions.FORWARD;
      }
    }
  }

  public static void evadeObject(GameObject bot, List<GameObject> gameObj, World w, PlayerAction p) {
    checkHarmfulObject(bot, gameObj, p, ObjectTypes.ASTEROID_FIELD, 10, "evade asteroid field");
    checkHarmfulObject(bot, gameObj, p, ObjectTypes.GAS_CLOUD, 20, "evade gas cloud");
    checkHarmfulObject(bot, gameObj, p, ObjectTypes.WORMHOLE, 20, "evade wormhole");
    checkBorder(bot, w, p);
  }

  public static void catchFood(List<GameObject> allState, GameObject bot, PlayerAction p) {
    System.out.println("Cathing food     Size : " + bot.getSize());
    var foodList = allState.stream()
        .filter(
            item -> item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPER_FOOD)
        .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)))
        .collect(Collectors.toList());
    System.out.println("food distance1 : " + BotService.getDistanceBetween(foodList.get(0), bot) + "   distance2 : "
        + BotService.getDistanceBetween(foodList.get(1), bot));
    p.heading = getHeadingBetween(bot, foodList.get(0));
    p.action = PlayerActions.FORWARD;
  }

}
