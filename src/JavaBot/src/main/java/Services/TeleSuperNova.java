package Services;

import Enums.*;
import Models.*;
import java.lang.Math;

import java.util.*;
import java.util.stream.*;

public class TeleSuperNova {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    private int headingTele;
    private int headingSupernova;

    public TeleSuperNova() {
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

    public void tembakTorpedo(PlayerAction playerAction) {
      playerAction.action = PlayerActions.FIRETORPEDOES;
      
      if (!gameState.getGameObjects().isEmpty()) {
        var playerList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        playerAction.heading = getHeadingBetween(playerList.get(1));
      }
    }

    public int teleportSerang(PlayerAction playerAction, int firedTeleport) {
      // Punya return value yang menandakan heading teleporter dan bahwa telah menembakkan teleporter
      playerAction.action = PlayerActions.FIRETELEPORT;
      
      if (!gameState.getGameObjects().isEmpty()) {
        // Kalau belum pakai radar
        var playerList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        playerAction.heading = getHeadingBetween(playerList.get(1));
      }

      return playerAction.heading;
    }

    public int checkTeleportSerang (PlayerAction playerAction, int headingTele) {
      var teleporterList = gameState.getGameObjects()
        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER && 
          item.getCurrentHeading() == headingTele) // Untuk mengetahui apakah teleporter punya kita atau bukan, karena heading kemungkinan besar unik
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
        if (teleporterDistance <= bot.getSize() + 10) { // Jika radius tambah 10 kurang dari jarak teleporter ke musuh terdekat
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

    public int teleportBertahan(PlayerAction playerAction) {
      // Punya return value yang menandakan heading teleporter dan bahwa telah menembakkan teleporter
      playerAction.action = PlayerActions.FIRETELEPORT;
      
      if (!gameState.getGameObjects().isEmpty()) {
        // Kalau belum pakai radar
        var playerList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        // Pakai index 1 untuk cari pemain lain lain terdekat
        playerAction.heading = (getHeadingBetween(playerList.get(1)) + 90) % 360; // Sementara
      }

      return playerAction.heading+2160;
    }
    
    public int checkTeleportBertahan (PlayerAction playerAction, int headingTele) {
      var teleporterList = gameState.getGameObjects()
        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER && 
          item.getCurrentHeading() == headingTele % 360) // Untuk mengetahui apakah teleporter punya kita atau bukan, karena heading kemungkinan besar unik
        .sorted(Comparator
                .comparing(item -> getDistanceBetween(bot, item)))
        .collect(Collectors.toList());

      if (!teleporterList.isEmpty()) { // Check apakah teleporter masih ada di lapangan
        if (headingTele > 720) {
          var nextX = teleporterList.get(0).getPosition().getX() + 20*Math.cos(headingTele % 360); // Koor X pada tick selanjutnya
          var nextY = teleporterList.get(0).getPosition().getY() + 20*Math.sin(headingTele % 360); // Koor Y pada tick selanjutnya
          var nextPosition = Math.sqrt(Math.pow(nextX,2) + Math.pow(nextY,2)); // posisi teleporter pada tick selanjutnya   
          
          if (nextPosition > gameState.getWorld().getRadius()) { // Jika pada tick selanjutnya teleporter keluar border, teleport sekarang saja
            playerAction.action = PlayerActions.TELEPORT;
            return -1;
          }

          return headingTele - 360;  
        } else {
          playerAction.action = PlayerActions.TELEPORT;
          return -1;
        }
      } else {
        return -1;
      }
    }

    public int tembakSuperNova(PlayerAction playerAction) {
      // Punya return value yang menandakan bahwa telah menembakkan supernova
      playerAction.action = PlayerActions.FIRETELEPORT;
      
      if (!gameState.getGameObjects().isEmpty()) {
        // Kalau belum pakai radar
        var playerList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        playerAction.heading = getHeadingBetween(playerList.get(1));
      }

      return playerAction.heading;
    }

    public int checkDetonateSuperNova (PlayerAction playerAction, int headingSupernova) {
      var supernovaList = gameState.getGameObjects()
        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVA_BOMB && 
          item.getCurrentHeading() == headingSupernova)        // Untuk mengetahui apakah supernova punya kita atau bukan, karena heading kemungkinan besar unik        
          .sorted(Comparator
            .comparing(item -> getDistanceBetween(bot, item))) 
        .collect(Collectors.toList());

      var enemyList = gameState.getGameObjects()
        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER)
        .sorted(Comparator
              .comparing(item -> getDistanceBetween(supernovaList.get(0), item)))
        .collect(Collectors.toList());


      if (!supernovaList.isEmpty()) { // Jika supernova masih ada dalam permainan
        var supernovaDistance = getDistanceBetween(supernovaList.get(0), enemyList.get(1));       
        if (supernovaDistance <= bot.getSize() + 50) { // Jika radius tambah 50 kurang dari jarak supernova ke musuh terdekat
          playerAction.action = PlayerActions.DETONATESUPERNOVA;
          return -1;
        } 

        // Return heading awal, tunggu tick selanjutnya
        // jika pada tick2 selanjutnya supernova melebihi boundary maka otomatis hilang
        return headingSupernova; 
      } else {
        return -1;
      }
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        // Kalau mau pakai teleport dan supernova nanti pakai kondisional di luar fungsi 
        // buat tracking apakah pernah nembak teleporter atau supernova
        // if (headingTele != -1) {
              // if (headingTele > 360) {
              //    headingTele = checkTeleportBertahan();
              // } else {
                    // headingTele = checkTeleportSerang();
              // }  
        // } 

        // if (headingSupernova != -1) {
              // headingSupernova = checkDetonateSuperNova();
        // } 

        // else baru cek di bawah
        if (!gameState.getGameObjects().isEmpty()) {
            var foodList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            playerAction.heading = getHeadingBetween(foodList.get(0));
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
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

}
