package Services;

import Enums.*;
import Models.*;
import java.util.*;
import java.util.stream.*;

public class Radar {
  public boolean supernovaDefend;
  public boolean teleportDefend;
  public boolean torpedoDefend;
  public boolean playerDefend;

  public static final int ALL_CLEAR = 0;
  public static final int TELEPORTDEFENCE = 1;
  public static final int SUPERNOVADEFENCE = 2;
  public static final int TORPEDODEFENCE = 3;

  public Radar() {
    supernovaDefend = false;
    teleportDefend = false;
    torpedoDefend = false;
    playerDefend = false;
}
  
  public List<GameObject> getCloseGameObjects(GameState gameState, GameObject bot, ObjectTypes objectType, int distance) {
    // Mendapatkan semua gameObject dalam radius distance
    if (objectType == ObjectTypes.PLAYER) {
      return gameState.getPlayerGameObjects()
      .stream()
      .filter(item -> item.getGameObjectType() == objectType && (BotService.getDistanceBetween(bot, item) < (distance + item.getSize())))
      .collect(Collectors.toList());  
    } else {
      return gameState.getGameObjects()
      .stream()
      .filter(item -> item.getGameObjectType() == objectType && (BotService.getDistanceBetween(bot, item) < (distance + item.getSize())))
      .collect(Collectors.toList());
    }

  }

  // public static int checkObjects(List<GameObject> prevState, List<GameObject> currentState, GameObject bot, int returnValue) {
  //   for (int i = 0; i < prevState.size(); i++) {
  //     for (int j = 0; j < currentState.size(); j++) {
  //       if (prevState.get(i).getId() == currentState.get(j).getId()) {
  //         if ((prevState.get(i).getPosition().x - bot.getPosition().x) != 0) {
  //           var prevGradient = BotService.toDegrees(Math.atan2(prevState.get(i).getPosition().y - bot.getPosition().y,
  //               prevState.get(i).getPosition().x - bot.getPosition().x));
  //           var curGradient = BotService.toDegrees(Math.atan2(currentState.get(j).getPosition().y - bot.getPosition().y,
  //               currentState.get(j).getPosition().x - bot.getPosition().x));
  //           if ((prevGradient - curGradient) < 0.05 || (prevGradient - curGradient) > -0.05) {
  //             return returnValue;
  //           }
  //         } else if ((prevState.get(i).getPosition().y - bot.getPosition().y) != 0) {
  //           var prevGradient = BotService.toDegrees(Math.atan2(prevState.get(i).getPosition().x - bot.getPosition().x,
  //               prevState.get(i).getPosition().y - bot.getPosition().y));
  //           var curGradient = BotService.toDegrees(Math.atan2(currentState.get(j).getPosition().x - bot.getPosition().x,
  //               currentState.get(j).getPosition().y - bot.getPosition().y));
  //           if ((prevGradient - curGradient) < 0.05 || (prevGradient - curGradient) > -0.05) {
  //             return returnValue;
  //           }
  //         } else {
  //           return 0;
  //         }
  //       }
  //     }
  //   }
  //   return 0;
  // }

  public GameObject checkObjectsNextVersion(List<GameObject> prevObjects, List<GameObject> currentObjects, GameObject bot, int vs) {    
    List<GameObject> objectList = new ArrayList<>();

    for (int i = 0; i < prevObjects.size(); i++) {
      for (int j = 0; j < currentObjects.size(); j++) {
        if (prevObjects.get(i).getId().equals(currentObjects.get(j).getId())) {
          double prevDistance = BotService.getDistanceBetween(prevObjects.get(i), bot);
          double currDistance = BotService.getDistanceBetween(currentObjects.get(j), bot);
          double vm = (prevDistance - currDistance);
          double radian = Math.atan2(bot.getSize(),currDistance);
          
          if (currentObjects.get(j).gameObjectType == ObjectTypes.SUPERNOVA_BOMB) {
            radian = Math.atan2(480,currDistance);
          } 

          if (currentObjects.get(j).getGameObjectType() == ObjectTypes.PLAYER) {
            vs = 200/currentObjects.get(j).getSize();
          }

          // System.out.println("vm: " + vm + " |  vs:  " + vs*Math.cos(radian));
          // System.out.println();

          if (vm > vs * Math.cos(radian)) {
            // System.out.println("vm: " + vm + " >  vs:  " + vs*Math.cos(radian));
            objectList.add(currentObjects.get(j));
          }
        }
      }
    }

    // System.out.println("test");

    if (objectList.size() > 0) {
      var newObjList = objectList.stream()
      .sorted(Comparator.comparing(item -> BotService.getDistanceBetween(bot, item)))
      .collect(Collectors.toList()); 
      return newObjList.get(0);
    }
    else {
      return null;
    }
  }

  public GameObject checkRadar(GameState gameState, GameState prevState, GameObject bot) {
      if(prevState != null){
        var prevSupernova = getCloseGameObjects(prevState, bot, ObjectTypes.SUPERNOVA_BOMB, 800+bot.getSize());
        var currSupernova = getCloseGameObjects(gameState, bot, ObjectTypes.SUPERNOVA_BOMB, 800+bot.getSize());

        var prevTele = getCloseGameObjects(prevState, bot, ObjectTypes.TELEPORTER, 200+bot.getSize());
        var currTele = getCloseGameObjects(gameState, bot, ObjectTypes.TELEPORTER, 200+bot.getSize());

        var prevTorpedo = getCloseGameObjects(prevState, bot, ObjectTypes.TORPEDO_SALVO, 280+bot.getSize());
        var currTorpedo = getCloseGameObjects(gameState, bot, ObjectTypes.TORPEDO_SALVO, 280+bot.getSize());

        var prevPlayer = getCloseGameObjects(prevState, bot, ObjectTypes.PLAYER, 200+bot.getSize());
        var currPlayer = getCloseGameObjects(gameState, bot, ObjectTypes.PLAYER, 200+bot.getSize());

        
        var danger = checkObjectsNextVersion(prevSupernova, currSupernova, bot, 40);
        if (danger != null) {supernovaDefend=true;return danger;}
        danger = checkObjectsNextVersion(prevTele, currTele, bot, 20);
        if (danger != null) {teleportDefend=true;return danger;}
        danger = checkObjectsNextVersion(prevTorpedo, currTorpedo, bot, 20);
        if (danger != null) {torpedoDefend=true;return danger;}
        danger = checkObjectsNextVersion(prevPlayer, currPlayer, bot, 20);
        if (danger != null) {playerDefend=true;return danger;}
      }
      return null;
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

}
