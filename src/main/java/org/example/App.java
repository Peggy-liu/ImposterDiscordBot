package org.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.example.GameEngine.GameEngine;
import org.example.GameEngine.GameEngineManager;
import org.example.GameEngine.GameStatus;
import org.example.listener.MessageListener;
import org.example.listener.ReactionListener;

import javax.security.auth.login.LoginException;

public class App {
    private static final String TOKEN = "NzY0ODUyNDcwNjU1NzQ2MDU4.X4MSXw.wpu95IXqhIT9iTl3bWsSKMnf5g0";
    public static final GameEngineManager GAME_ENGINE_MANAGER = new GameEngineManager();
    public static void main(String[] args) {


        try {
            JDABuilder builder = JDABuilder.createDefault(TOKEN);
            builder.addEventListeners(new MessageListener(), new ReactionListener());
            builder.setActivity(Activity.playing("Imposter game"));
            JDA jda = builder.build();
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.awaitReady();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
