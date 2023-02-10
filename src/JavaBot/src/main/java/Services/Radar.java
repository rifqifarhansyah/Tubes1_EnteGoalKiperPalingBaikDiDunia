package Services;

import Enums.*;
import Models.*;
import java.util.*;
import java.util.stream.*;

public class Radar {

  public static final int ALL_CLEAR = 0;
  public static final int TELEPORTDEFENCE = 1;
  public static final int SUPERNOVADEFENCE = 2;
  public static final int TORPEDODEFENCE = 3;

  public static int radar(GameState gameState, GameState prevState, GameObject bot) {
      private List<GameObject> getCloseGameObjects(GameState gameState, ObjectTypes objectType, int distance) {
        return gameState.getGameObjects()
            .stream()
            .filter(item -> item.getGameObjectType() == objectType && (BotService.getDistanceBetween(bot, item) < distance))
            .collect(Collectors.toList());
      }

      private int checkObjects(List<GameObject> prevObjects, List<GameObject> currentObjects, GameObject bot, int returnValue) {
        for (int i = 0; i < prevObjects.size(); i++) {
          for (int j = 0; j < currentObjects.size(); j++) {
            if (prevObjects.get(i).getId() == currentObjects.get(j).getId()) {
              if ((prevObjects.get(i).getPosition().x - bot.getPosition().x) != 0) {
                var prevGradient = BotService.toDegrees(Math.atan2(prevObjects.get(i).getPosition().y - bot.getPosition().y,
                    prevObjects.get(i).getPosition().x - bot.getPosition().x));
                var curGradient = BotService.toDegrees(Math.atan2(currentObjects.get(j).getPosition().y - bot.getPosition().y,
                    currentObjects.get(j).getPosition().x - bot.getPosition().x));
                if ((prevGradient - curGradient) < 0.05 || (prevGradient - curGradient) > -0.05) {
                  return returnValue;
                }
              } else if ((prevObjects.get(i).getPosition().y - bot.getPosition().y) != 0) {
                var prevGradient = BotService.toDegrees(Math.atan2(prevObjects.get(i).getPosition().x - bot.getPosition().x,
                    prevObjects.get(i).getPosition().y - bot.getPosition().y));
                var curGradient = BotService.toDegrees(Math.atan2(currentObjects.get(j).getPosition().x - bot.getPosition().x,
                    currentObjects.get(j).getPosition().y - bot.getPosition().y));
                if ((prevGradient - curGradient) < 0.05 || (prevGradient - curGradient) > -0.05) {
                  return returnValue;
                }
              } else {
                return 0;
              }
            }
          }
        }
        return 0;
      }

      private int checkObjectsNextVersion(List<GameObject> prevObjects, List<GameObject> currentObjects, GameObject bot, int returnValue, int vs) {
        for (int i = 0; i < prevObjects.size(); i++) {
          for (int j = 0; j < currentObjects.size(); j++) {
            if (prevObjects.get(i).getId() == currentObjects.get(j).getId()) {
              double deltaX = prevObjects.get(i).getPosition().x - bot.getPosition().x;
              double deltaY = prevObjects.get(i).getPosition().y - bot.getPosition().y;
              double prevRadians = Math.atan2(deltaY, deltaX);
              double prevDegrees = Math.toDegrees(prevRadians);

              deltaX = currentObjects.get(j).getPosition().x - bot.getPosition().x;
              deltaY = currentObjects.get(j).getPosition().y - bot.getPosition().y;
              double currRadians = Math.atan2(deltaY, deltaX);
              double currDegrees = Math.toDegrees(currRadians);

              int prevDistance = BotService.getDistanceBetween(prevObjects.get(i), bot);
              int currDistance = BotService.getDistanceBetween(currentObjects.get(j), bot);
              int vm = (prevDistance - currDistance);
              if (vm > vs * Math.cos(prevRadians-currRadians)) {
                return returnValue;
              }
            }
          }
        }
        return 0;
      }
      
      if(prevState != null){
        var prevSupernova = getCloseGameObjects(prevState, ObjectTypes.SUPERNOVA_BOMB, 200);
        var currSupernova = getCloseGameObjects(gameState, ObjectTypes.SUPERNOVA_BOMB, 200);
        var prevTele = getCloseGameObjects(prevState, ObjectTypes.TELEPORTER, 120);
        var currTele = getCloseGameObjects(gameState, ObjectTypes.TELEPORTER, 120);
        var prevTorpedo = getCloseGameObjects(prevState, ObjectTypes.TORPEDO, 120);
        var currTorpedo = getCloseGameObjects(gameState, ObjectTypes.TORPEDO, 120);
        var prevPlayer = getCloseGameObjects(prevState, ObjectTypes.PLAYER, 120);
        var currPlayer = getCloseGameObjects(gameState, ObjectTypes.PLAYER, 120);

        checkObjects(prevSupernova, currSupernova, bot, SUPERNOVADEFENCE);
        checkObjects(prevTele, currTele, bot, TELEPORTDEFENCE);
        checkObjects(prevTorpedo, currTorpedo, bot, TORPEDODEFENCE);
        checkObjectsNextVersion(prevSupernova, currSupernova, bot, SUPERNOVADEFENCE, 40);
        checkObjectsNextVersion(prevTele, currTele, bot, TELEPORTDEFENCE, 20);
        checkObjectsNextVersion(prevTorpedo, currTorpedo, bot, TORPEDODEFENCE, 20);
      }

    // if (prevState != null) {
    //   var prevSupernova = prevState.getGameObjects()
    //       .stream()
    //       .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVA_BOMB
    //           && (BotService.getDistanceBetween(bot, item) < 200))
    //       .collect(Collectors.toList());
    //   var currSupernova = gameState.getGameObjects()
    //       .stream()
    //       .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVA_BOMB
    //           && (BotService.getDistanceBetween(bot, item) < 200))
    //       .collect(Collectors.toList());
      // for (int i = 0; i < prevSupernova.size(); i++) {
      //   for (int j = 0; j < currSupernova.size(); j++) {
      //     if (prevSupernova.get(i).getId() == currSupernova.get(j).getId()) {
      //       if ((prevSupernova.get(i).getPosition().x - bot.getPosition().x) != 0) {
      //         var prevGradien = BotService
      //             .toDegrees(Math.atan2(prevSupernova.get(i).getPosition().y - bot.getPosition().y,
      //                 prevSupernova.get(i).getPosition().x - bot.getPosition().x));
      //         var curGradien = BotService
      //             .toDegrees(Math.atan2(currSupernova.get(j).getPosition().y - bot.getPosition().y,
      //                 currSupernova.get(j).getPosition().x - bot.getPosition().x));
      //         if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05) {
      //           return 2;
      //         }
      //       } else if ((prevSupernova.get(i).getPosition().y - bot.getPosition().y) != 0) {
      //         var prevGradien = BotService
      //             .toDegrees(Math.atan2(prevSupernova.get(i).getPosition().x - bot.getPosition().x,
      //                 prevSupernova.get(i).getPosition().y - bot.getPosition().y));
      //         var curGradien = BotService
      //             .toDegrees(Math.atan2(currSupernova.get(j).getPosition().x - bot.getPosition().x,
      //                 currSupernova.get(j).getPosition().y - bot.getPosition().y));
      //         if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05) {
      //           return 2;
      //         }
      //       } else {
      //         return 0;
      //       }
      //     }
      //   }
      // }

    //   var prevTele = prevState.getGameObjects()
    //       .stream()
    //       .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER
    //           && (BotService.getDistanceBetween(bot, item) < 120))
    //       .collect(Collectors.toList());
    //   var currentTele = gameState.getGameObjects()
    //       .stream()
    //       .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER
    //           && (BotService.getDistanceBetween(bot, item) < 120))
    //       .collect(Collectors.toList());
      // for (int i = 0; i < prevTele.size(); i++) {
      //   for (int j = 0; j < currentTele.size(); j++) {
      //     if (prevTele.get(i).getId() == currentTele.get(j).getId()) {
      //       if ((prevTele.get(i).getPosition().x - bot.getPosition().x) != 0) {
      //         var prevGradien = BotService.toDegrees(Math.atan2(prevTele.get(i).getPosition().y - bot.getPosition().y,
      //             prevTele.get(i).getPosition().x - bot.getPosition().x));
      //         var curGradien = BotService.toDegrees(Math.atan2(currentTele.get(j).getPosition().y - bot.getPosition().y,
      //             currentTele.get(j).getPosition().x - bot.getPosition().x));
      //         if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05) {
      //           return 1;
      //         }
      //       } else if ((prevTele.get(i).getPosition().y - bot.getPosition().y) != 0) {
      //         var prevGradien = BotService.toDegrees(Math.atan2(prevTele.get(i).getPosition().x - bot.getPosition().x,
      //             prevTele.get(i).getPosition().y - bot.getPosition().y));
      //         var curGradien = BotService.toDegrees(Math.atan2(currentTele.get(j).getPosition().x - bot.getPosition().x,
      //             currentTele.get(j).getPosition().y - bot.getPosition().y));
      //         if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05) {
      //           return 1;
      //         }
      //       } else {
      //         return 0;
      //       }
      //     } else {
      //       return 0;
      //     }
      //   }
      // }

      // for(int i=0; i<prevTele.size(); i++){
      //   for(int j=0; j<currentTele.size(); j++){
      //     if(prev)
      //   }
      // }

    //   var prevTorpedo = prevState.getGameObjects()
    //       .stream()
    //       .filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO
    //           && (BotService.getDistanceBetween(bot, item) < 120))
    //       .collect(Collectors.toList());
    //   var currTorpedo = gameState.getGameObjects()
    //       .stream()
    //       .filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO
    //           && (BotService.getDistanceBetween(bot, item) < 120))
    //       .collect(Collectors.toList());
    //   for (int i = 0; i < prevTorpedo.size(); i++) {
    //     for (int j = 0; j < currTorpedo.size(); j++) {
    //       if (prevTorpedo.get(i).getId() == currTorpedo.get(j).getId()) {
    //         if ((prevTorpedo.get(i).getPosition().x - bot.getPosition().x) != 0) {
    //           var prevGradien = BotService
    //               .toDegrees(Math.atan2(prevTorpedo.get(i).getPosition().y - bot.getPosition().y,
    //                   prevTorpedo.get(i).getPosition().x - bot.getPosition().x));
    //           var curGradien = BotService.toDegrees(Math.atan2(currTorpedo.get(j).getPosition().y - bot.getPosition().y,
    //               currTorpedo.get(j).getPosition().x - bot.getPosition().x));
    //           if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05) {
    //             return 3;
    //           }
    //         } else if ((prevTorpedo.get(i).getPosition().y - bot.getPosition().y) != 0) {
    //           var prevGradien = BotService
    //               .toDegrees(Math.atan2(prevTorpedo.get(i).getPosition().x - bot.getPosition().x,
    //                   prevTorpedo.get(i).getPosition().y - bot.getPosition().y));
    //           var curGradien = BotService.toDegrees(Math.atan2(currTorpedo.get(j).getPosition().x - bot.getPosition().x,
    //               currTorpedo.get(j).getPosition().y - bot.getPosition().y));
    //           if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05) {
    //             return 3;
    //           }
    //         } else {
    //           return 0;
    //         }
    //       }
    //     }
    //   }
    // } 
    return 0;
  }
}
