package org.example.GameEngine;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class GameEngine {
	//this game need to have at least 3 players
	//TODO: change this value
	public static final int MIN_PLAYER = 0;
	public static final int MAX_PLAYER = 9;
	public static final int TOTAL_ROUND = 2;
	//the user that hosts the game
	private User host;
	//the current state of the game
	private GameStatus gameStatus;
	//total number of player in the game
	private int people_in_game;
	//current round number
	private int round = 1;
	private Message gameLobby;
	//the controller that manager players in the game
	private PlayerManagementEngine userManagement;

	//the manager that manage the voting part
	private VotingManager votingManager;


	public GameEngine(User host) {
		this(GameStatus.WAITING, 0, host);
	}

	private GameEngine(GameStatus initStatus, int people_in_game, User host) {
		this.gameStatus = initStatus;
		this.people_in_game = people_in_game;
		this.host = host;
		this.userManagement = new PlayerManagementEngine();
		this.votingManager = new VotingManager();
	}

	public void increasePlayerNumber(User user) {
		userManagement.addPlayer(user);
		this.people_in_game++;
	}

	public void decreasePlayerNumber(User user) {
		userManagement.deletePlayer(user);
		this.people_in_game--;
	}

	public boolean isPlayerExist(User user) {
		return userManagement.isPlayerExist(user);
	}

	private void populatePlayerAssignment() {

		if (this.gameStatus == GameStatus.IN_PRORESS) {
			userManagement.populatePlayerRoleMap();
		}
	}

	public HashMap<User, PlayerStat> getPlayerAssignment() {
		return userManagement.getPlayerRoleMap();
	}


	public int getPeople_in_game() {
		return people_in_game;
	}

	public User getHost() {
		return host;
	}

	public GameStatus getGameStatus() {
		return gameStatus;
	}

	public void setGameStatus(GameStatus gameStatus) {
		this.gameStatus = gameStatus;
	}

	public void changeGameStatusToInProgress() {

		setGameStatus(GameStatus.IN_PRORESS);
		populatePlayerIndexMap();
		populatePlayerAssignment();
		votingManager.initializeVotingBoard(userManagement.getPlayerIndexMap());
		deleteLobby();
	}

	public List<Field> populateFields() {

		List<Field> result = new ArrayList<>();

		userManagement.getPlayerIndexMap().forEach((index, user) -> {
			Field newField = new Field(user.getName(), index, false);
			result.add(newField);
		});
		return result;
	}

	//only called when the game state changed from WAITING to IN PROGRESS
	private void populatePlayerIndexMap() {
		userManagement.populatePlayerIndexMap();
	}

	public void voteFor(User user, String index) {
		votingManager.voteFor(user, index);
	}

	public VotingManager getVotingManager() {
		return votingManager;
	}

	public int getVotedPeopleNum() {
		return votingManager.getVotedPlayer().size();
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public boolean is_Voting_Finished() {
		return votingManager.isVoting_finished();
	}

	public void setVotingStatus(boolean status) {
		votingManager.setVoting_finished(status);
	}

	public List<User> populateResult() {
		return votingManager.finalizeVote(userManagement.getPlayerIndexMap());
	}

	public HashMap<String, User> getPlayerIndexMap() {
		return userManagement.getPlayerIndexMap();
	}

	public void resetVotingBoard() {
		userManagement.populatePlayerIndexMap();
		votingManager.initializeVotingBoard(userManagement.getPlayerIndexMap());
		votingManager.resetVotedPlayer();
	}

	public PlayerManagementEngine getUserManagement() {
		return userManagement;
	}

	public PlayerStat getPlayerStat(User user) {
		return userManagement.getPlayerRoleMap().get(user);
	}

	public void setGameLobby(Message lobby) {
		this.gameLobby = lobby;
	}

	public void enterLobby(User user) {
		String content = gameLobby.getContentRaw().concat("   "+user.getName());
		gameLobby.editMessage(content).queue(msg -> setGameLobby(msg));
	}

	private void deleteLobby(){
		gameLobby.delete().queue();
	}

}
