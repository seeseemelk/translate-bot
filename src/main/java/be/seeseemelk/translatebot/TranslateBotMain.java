package be.seeseemelk.translatebot;

import javax.security.auth.login.LoginException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;

public class TranslateBotMain
{
	private Logger logger = LogManager.getLogger("Bot");
	private TranslationEngine translationEngine;
	private String token;
	private JDA jda;
	private EventListener listener;
	
	public TranslateBotMain()
	{
		logger.info("Starting bot...");
		token = System.getProperty("DiscordToken");
		if (token == null)
		{
			logger.error("Token not set (Set with -DDiscordToken)");
			System.exit(1);
		}
		
		translationEngine = new TranslationEngine();
		listener = new EventListener(translationEngine, logger);
		connect();
	}
	
	public void connect()
	{
		try
		{
			logger.info("Connecting to Discord");
			jda = new JDABuilder(AccountType.BOT).setToken(token).buildAsync();
			jda.setEventManager(new AnnotatedEventManager());
			jda.addEventListener(listener);
		}
		catch (LoginException e)
		{
			logger.error("Failed to login");
			logger.throwing(e);
		}
	}
	
	public static void main(String[] args)
	{
		new TranslateBotMain();
	}
	
}
