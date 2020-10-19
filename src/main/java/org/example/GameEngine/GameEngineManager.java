package org.example.GameEngine;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.HashMap;

public class GameEngineManager {

	private HashMap<MessageChannel, GameEngine> gameEngineMap = new HashMap<>();


	public boolean containsChannel(MessageChannel channel) {
		return gameEngineMap.containsKey(channel);
	}

	public GameEngine getGameEngineInstance(MessageChannel channel){
		return gameEngineMap.get(channel);
	}

	public void addGameInstance(MessageChannel channel, GameEngine instance){
		gameEngineMap.put(channel, instance);
	}

	public void removeGameInstance(MessageChannel channel){
		gameEngineMap.remove(channel);
	}

	public MessageEmbed populateNewGameMessage(User user){
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Welcome to Imposter Game!");
		builder.setDescription("To see game manual, enter \"~how\"");
		builder.addField("To Host: ", "once all players have joined, click 🆗 to start the game", false);
		builder.addField("To Players: ", "click ▶️ once to join the game",  false);
		builder.setFooter("This game is hosted by "+user.getName(), user.getAvatarUrl());
		builder.setColor(Color.RED);
		return builder.build();
	}
}
