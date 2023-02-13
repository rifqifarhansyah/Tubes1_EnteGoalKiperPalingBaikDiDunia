package Services;

import Enums.*;
import Models.*;
import java.util.*;
import java.util.stream.*;

public class GreedyCommand extends BotService {
  public static PlayerAction run(GameObject enemy, PlayerAction p, UUID objectTracker, GameObject bot) {

    System.out.println("speed: " + bot.getSpeed() + "  size:  " + bot.getSize() + "  status: "
        + bot.getAfterburnerStatus() + " effect:  " + bot.getEffects());
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
        System.out.println(bot.getSpeed());
        if (bot.getSpeed() == 0) {
          // System.out.println("speed 0");
          p.action = PlayerActions.FORWARD;
        } else if (bot.getSize() > 30) {
          if (enemy.getGameObjectType() == ObjectTypes.PLAYER) {
            if (BotService.getDistanceBetween(bot, enemy) - bot.getSize() + enemy.getSize() < 30) {
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
      System.out.println("Evade border");
      p.heading = BotService.getHeadingBetween(bot, 0, 0);
      p.action = PlayerActions.FORWARD;
    }
  }

  public static void catchFood(List<GameObject> allState, GameObject bot, PlayerAction p) {
    System.out.println("Cathing food");
    var foodList = allState.stream()
        .filter(
            item -> item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPER_FOOD)
        .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)))
        .collect(Collectors.toList());
    p.heading = getHeadingBetween(bot, foodList.get(0));
    p.action = PlayerActions.FORWARD;
  }

}
