package org.example.listener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.App;
import org.example.GameEngine.GameEngine;
import org.example.GameEngine.GameEngineManager;
import org.example.GameEngine.GameStatus;
import org.example.GameEngine.PlayerStat;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class ReactionListener extends ListenerAdapter {

	private GameEngineManager gameEngineManager = App.GAME_ENGINE_MANAGER;

	@Override
	public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
		MessageChannel receivedChannel = event.getChannel();
		String reactedEmoji = event.getReactionEmote().getAsCodepoints();
		//System.out.println(reactedEmoji);
		JDA jda = event.getJDA();
		//ignore emoji reaction other than voting number
		if (!reactedEmoji.startsWith("U+")) {
			return;
		}
		//ignore bot reaction
		if (event.getMember().getUser().isBot()) {
			return;
		}

		if (gameEngineManager.containsChannel(receivedChannel)) {
			GameEngine gameEngine = gameEngineManager.getGameEngineInstance(receivedChannel);
			//scenario: when user join the game
			if (reactedEmoji.equals("U+25b6U+fe0f")) {
				User user = event.getMember().getUser();
				//Player can only join the game at the WAITING STATE:
				if (gameEngine.getGameStatus().equals(GameStatus.WAITING)) {

					if (gameEngine.isPlayerExist(user)) {
						jda.openPrivateChannelById(user.getId())
								.queue(ch -> ch.sendMessage("You have joined the game! Please wait for the game start...").queue());
					}
					else {
						//can not be more than MAX_PLAYER number of people
						if (gameEngine.getPeople_in_game() <= GameEngine.MAX_PLAYER) {
							gameEngine.enterLobby(user);
							gameEngine.increasePlayerNumber(user);
						}
						else {
							jda.openPrivateChannelById(user.getId())
									.queue(ch -> ch.sendMessage("The game is in full, please wait for the next round!").queue());
						}
					}
				}
				else {
					//game state is IN PROGRESS, notify the player to wait for the next game.
				}
				return;
			}

			//scenario: when the host start the game 🆗
			if (reactedEmoji.equals("U+1f197")) {
				//game state is changed from WAITING to IN PROGRESS
				if (gameEngine.getGameStatus().equals(GameStatus.WAITING)) {
					//only the host can start the game
					if (event.getMember().getUser().getId().equals(gameEngine.getHost().getId())) {
						//if there are less than 3 player, game cannot be started
						if (gameEngine.getPeople_in_game() < GameEngine.MIN_PLAYER) {
							event.getChannel().sendMessage("A minimal of " + GameEngine.MIN_PLAYER + " players is required to start the game! Please ask more friends!").queue();
						}
						else {
							gameEngine.changeGameStatusToInProgress();
							receivedChannel.sendMessage(String.format("------------------------Round %d---------------------------", gameEngine.getRound()))
									.queue();
							HashMap<User, PlayerStat> playerList = gameEngine.getPlayerAssignment();

							playerList.forEach((player, stat) -> {
								jda.openPrivateChannelById(player.getId())
										.queue(dm -> dm.sendMessage("You are : " + stat.getValue()).queue());
							});
						}

					}
				}
				return;
			}
			//scenario: voting, make sure the user is in this game
			User voter = event.getMember().getUser();
			if (gameEngine.isPlayerExist(voter)) {
				String str = "U+3%sU+20E3";
				IntStream stream = IntStream.range(1, 10);

				Iterator<Integer> iterator = stream.sorted().iterator();
				while (iterator.hasNext()) {
					int num = iterator.next();
					String emoji_name = String.format(str, num);
					if (reactedEmoji.equalsIgnoreCase(emoji_name)) {
						if (!gameEngine.is_Voting_Finished()) {
							//prevent user vote for themselves
							if (voter.getName().equals(gameEngine.getPlayerIndexMap().get(Integer.toString(num)).getName())) {
								event.getReaction().removeReaction(voter).queue();
								jda.openPrivateChannelById(voter.getId())
										.queue(msg -> msg.sendMessage("You cannot vote for yourself!").queue());
								break;
							}
							gameEngine.voteFor(voter, Integer.toString(num));
							break;
						}
						else {
							receivedChannel.sendMessage("Voting session is already finished! Stop voting!").queue();
						}

					}
				}
			}
			//when the host click ✅ to finalize the voting
			if (gameEngine.is_Voting_Finished() == false && gameEngine.getGameStatus().equals(GameStatus.IN_PRORESS) &&
					gameEngine.getRound() <= GameEngine.TOTAL_ROUND) {
				if (voter.equals(gameEngine.getHost())) {
					if (reactedEmoji.equalsIgnoreCase("U+2705")) {
						receivedChannel.deleteMessageById(event.getMessageId()).queue();
						receivedChannel.sendMessage("------------------Voting Finished!--------------------").queue();
						gameEngine.setVotingStatus(true);
						receivedChannel.sendMessage("Populating result.....").queueAfter(3, TimeUnit.SECONDS);
						List<User> result = gameEngine.populateResult();
						int curr_round = gameEngine.getRound();

						if (result.size() == 1) {
							User suspect = result.get(0);
							PlayerStat stat = gameEngine.getPlayerAssignment().get(suspect);
							if (stat.equals(PlayerStat.ANOMALY)) {
								receivedChannel.sendMessage(suspect.getName() + " is eliminated.").queueAfter(5, TimeUnit.SECONDS);
								receivedChannel.sendMessage(suspect.getName() + " IS the ANOMALY. Normal player wins! Game Finish!").queueAfter(7, TimeUnit.SECONDS);
								//the game instance will be destroyed after the game finished
								gameEngineManager.removeGameInstance(receivedChannel);
								return;
							}
							else {
								receivedChannel.sendMessage(suspect.getName() + " is eliminated.").queueAfter(5, TimeUnit.SECONDS);
								//third round but anomaly hasn't benn found, normal player lose, game finishes
								if (curr_round == GameEngine.TOTAL_ROUND) {
									receivedChannel.sendMessage(suspect.getName() + " IS NOT the ANOMALY. Game Over! Anomaly wins!").queueAfter(7, TimeUnit.SECONDS);
									//the game instance will be destroyed after the game finished
									gameEngineManager.removeGameInstance(receivedChannel);
									return;
								}
								receivedChannel.sendMessage(suspect.getName() + " IS NOT the ANOMALY. ")
										.queueAfter(7, TimeUnit.SECONDS);
								gameEngine.decreasePlayerNumber(suspect);

								if (gameEngine.getPeople_in_game() == 1 &&
										gameEngine.getPlayerStat(gameEngine.getUserManagement().getPlayers().get(0)).equals(PlayerStat.ANOMALY)) {
									receivedChannel.sendMessage("Game Over! Anomaly wins!  ")
											.queueAfter(9, TimeUnit.SECONDS);
									return;
								}
							}

						}
						else {
							receivedChannel.sendMessage("This round is a tie. No one is eliminated....").queueAfter(5, TimeUnit.SECONDS);
							if (curr_round == GameEngine.TOTAL_ROUND) {
								receivedChannel.sendMessage("Game Over! Anomaly wins!").queueAfter(7, TimeUnit.SECONDS);
								gameEngineManager.removeGameInstance(receivedChannel);
								return;
							}

						}
						//Before final round
						if (curr_round < GameEngine.TOTAL_ROUND) {
							curr_round++;
							gameEngine.setRound(curr_round);
							receivedChannel.sendMessage("Game Continues...").queueAfter(9, TimeUnit.SECONDS);
							receivedChannel.sendMessage(String.format("------------------------Round %d---------------------------", gameEngine.getRound()))
									.queueAfter(11, TimeUnit.SECONDS);
							gameEngine.setVotingStatus(false);
							gameEngine.resetVotingBoard();

						}
					}
				}
			}
			else {
				//This round's voting has been finalized
			}
		}
	}
}
