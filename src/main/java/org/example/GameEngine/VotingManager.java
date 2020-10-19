package org.example.GameEngine;

import net.dv8tion.jda.api.entities.User;

import java.util.*;

public class VotingManager {

	private HashMap<String, Integer> votingBoard;
	private HashMap<User, String> votedPlayer;
	private boolean Voting_finished;


	public VotingManager() {
		this.votingBoard = new HashMap<>();
		this.votedPlayer = new HashMap<>();
		this.Voting_finished = false;
	}

	public void initializeVotingBoard(HashMap<String, User> map) {

		map.keySet().forEach(index -> votingBoard.put(index, 0));
	}


	public HashMap<User, String> getVotedPlayer() {
		return votedPlayer;
	}


	//User voted for suspected player(int index)
	public void voteFor(User user, String index) {

		votedPlayer.put(user, index);
	}

	public List<User> finalizeVote(HashMap<String, User> playerIndexMap) {
		votedPlayer.forEach((user, index) -> {
			addVoteTo(index);
		});

		List<User> result = new ArrayList<>();

		int highest_vote = 0;
		List<String> most_suspect = new ArrayList<>();

		Iterator<Map.Entry<String, Integer>> iterator =  votingBoard.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<String, Integer> record = iterator.next();
			int vote_num = record.getValue();
			String suspect = record.getKey();
			if(vote_num >= highest_vote && vote_num !=0){
				if(vote_num == highest_vote){
					most_suspect.add(suspect);
				}
				highest_vote = vote_num;
				most_suspect.clear();
				most_suspect.add(suspect);
			}
		}
		most_suspect.stream().forEach(index -> {
			result.add(playerIndexMap.get(index));
		});
		return result;
	}

	public boolean isVoting_finished() {
		return Voting_finished;
	}

	public void setVoting_finished(boolean voting_finished) {
		Voting_finished = voting_finished;
	}

	private void addVoteTo(String index) {
		if (votingBoard.containsKey(index)) {
			int newVal = votingBoard.get(index) + 1;
			votingBoard.put(index, newVal);
		}
	}

	public void resetVotedPlayer(){
		this.votedPlayer.clear();
	}
}
