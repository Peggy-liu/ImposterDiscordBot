package org.example.GameEngine;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class PlayerManagementEngine {
    private List<User> players = new ArrayList<>();
    private HashMap<User, PlayerStat> playerRoleMap = new HashMap<>();
    private HashMap<String, User> playerIndexMap = new HashMap<>();



    public List<User> getPlayers() {
        return players;
    }


    public void addPlayer(User user) {
        players.add(user);
    }

    public void deletePlayer(User user) {
        if (players.contains(user)) {
            players.remove(user);
        }
    }

    public boolean isPlayerExist(User user) {
        return players.contains(user);
    }


    public void populatePlayerRoleMap() {
        Random random = new Random();
        //TODO  1 is the number of imposter in the game. Might set to other number by user later
        int streamSize = 1;
        int min = 0;
        int max = players.size() ;

        IntStream intStream = random.ints(streamSize, min, max);

        //assign the first index of this random IntStream to imposter, and the rest is normal player
        int[] arr = intStream.toArray();

        int imposter_index = arr[0];
        User imposter = players.get(imposter_index);
        playerRoleMap.put(imposter, PlayerStat.ANOMALY);

        //the rest of the players in the player list are NORMAL player
        for (User user: players) {
            if (!playerRoleMap.containsKey(user)){
                playerRoleMap.put(user, PlayerStat.NORMAL);
            }
        }

    }

    public HashMap<User, PlayerStat> getPlayerRoleMap() {
        HashMap<User, PlayerStat> list = (HashMap<User, PlayerStat>) playerRoleMap.clone();
        playerRoleMap.forEach(list::put);
        return list;
    }

    public void populatePlayerIndexMap(){
        //clean the old record and prepare for new round of input
        playerIndexMap.clear();

        int number = 1;
        for (User user:this.players) {
            playerIndexMap.put(Integer.toString(number), user);
            number++;
        }
    }

    public HashMap<String, User> getPlayerIndexMap() {
        HashMap<String, User> result = new HashMap<>();
        this.playerIndexMap.forEach(result::put);
        return result;
    }
}
