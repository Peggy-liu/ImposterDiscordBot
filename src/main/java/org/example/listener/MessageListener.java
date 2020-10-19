package org.example.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.App;
import org.example.GameEngine.GameEngine;
import org.example.GameEngine.GameEngineManager;
import org.example.GameEngine.GameStatus;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class MessageListener extends ListenerAdapter {

	private static final String GAME_PREFIX = "~";
	private static final String NEWGAME_PREFIX = "newgame";
	private static final String MANUAL_PREFIX = "how";
	private static final String VOTE_PREFIX = "vote";
	private GameEngineManager gameEngineManager = App.GAME_ENGINE_MANAGER;


	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		JDA jda = event.getJDA();
		MessageChannel channel = event.getChannel();
		//ignore the message sent by bots
		if (event.getAuthor().isBot()) {
			return;
		}

		//when a user start a new game in a particular channel
		if (event.getMessage().getContentRaw().equalsIgnoreCase(GAME_PREFIX + NEWGAME_PREFIX)) {
			//check if a game exists in this channel, if no, create new game
			if (gameEngineManager.containsChannel(channel)) {
				channel.sendMessage(String.format("There is a game exist in this channel. " +
						"The host is %s. Please try again when this game finishes...", gameEngineManager.getGameEngineInstance(channel).getHost().getName())).queue();
			}
			else {
				User host = event.getAuthor();
				GameEngine gameEngine = new GameEngine(host);
				gameEngine.increasePlayerNumber(host);
				gameEngineManager.addGameInstance(channel, gameEngine);
				MessageEmbed welcomeMsg = gameEngineManager.populateNewGameMessage(host);
				channel.sendMessage(welcomeMsg).queue(msg -> msg.addReaction("▶️").queue());  //U+25b6U+fe0f
				//create game lobby
				channel.sendMessage("Players in Waiting Lobby: ")
						.queue(msg -> {
							msg.addReaction("🆗").queue();
							gameEngine.setGameLobby(msg);
							gameEngine.enterLobby(host);
						});

			}
			return;
		}

		//Identify whether there is a game instance in the channel first
		if (gameEngineManager.containsChannel(channel)) {
			GameEngine gameInstance = gameEngineManager.getGameEngineInstance(channel);


			//~vote
			if (event.getMessage().getContentRaw().equalsIgnoreCase(GAME_PREFIX + VOTE_PREFIX) &&
					gameInstance.isPlayerExist(event.getAuthor())) {
				if (gameInstance.getGameStatus().equals(GameStatus.IN_PRORESS)) {
					int curr_round = gameInstance.getRound();
					if (curr_round <= GameEngine.TOTAL_ROUND) {
						EmbedBuilder builder = new EmbedBuilder();
						builder.setTitle(String.format("ROUND %d Voting: WHO IS THE ANOMALY 👻", curr_round));
						builder.setDescription("Choose the player that you think is the anomaly. Select the number:");
						builder.setColor(Color.magenta);
						List<Field> fields = gameInstance.populateFields();
						fields.stream().forEach(builder::addField);
						MessageEmbed embed = builder.build();
						channel.sendMessage(embed).queue(msg -> {
							String emoji = "U+3%sU+20E3";
							fields.forEach(field -> {
								String num = field.getValue();
								msg.addReaction(String.format(emoji, num)).queue();
							});
						});
						EmbedBuilder builder1 = new EmbedBuilder();
						builder1.setTitle("Reminder");
						builder1.addField("To Host: ", "Please click ✅ when all players finished voting.", false);
						builder1.addField("To Player: ", "Players could skip this round by not clicking any emojis. " +
								"But remember, there are only 3 rounds.", false);
						builder1.setColor(Color.green);
						MessageEmbed embed1 = builder1.build();
						channel.sendMessage(embed1)
								.queueAfter(5, TimeUnit.SECONDS, success -> success.addReaction("✅").queue());

					}

				}
			}

		}

		else {
			if (event.getMessage().getContentRaw().startsWith(GAME_PREFIX)) {
				if (event.getMessage().getContentRaw().equals(GAME_PREFIX + MANUAL_PREFIX)) {
					EmbedBuilder builder = new EmbedBuilder();
					builder.setTitle("Imposter Game Manual");
					builder.setDescription("Look for the anomaly among you!");

					builder.addField("Instruction",
							"Each player will be given a piece of information, and they are almost the same! " +
									"However, there is one player who will be getting a slightly different piece...and that person don't even know!" +
									"Have a chat and describe what you have! " +
									"Vote for the person who you think is the anomaly!" +
									"PS: DO NOT to communicate with others about what you have during the game, describe only the information you have!",
							false);
					builder.addField("TIP FOR ANOMALY: ",
							"Once you found out you might be the anomaly, " +
									"try to deceive other players by describing what you think they might have!",
							false);
					builder.setFooter("created by " + jda.getSelfUser().getName(), jda.getSelfUser().getAvatarUrl());
					builder.setColor(Color.BLUE);
					MessageEmbed embed = builder.build();
					channel.sendMessage(embed).queue();
				}
				else {
					channel.sendMessage("there is no Imposter game instance alive in this Channel! Please check with the host " +
							"or use \"~newgame\" to start a new game")
							.queue();
				}

			}
		}
	}
}

