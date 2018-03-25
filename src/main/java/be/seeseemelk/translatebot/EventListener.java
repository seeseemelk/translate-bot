package be.seeseemelk.translatebot;

import org.apache.logging.log4j.Logger;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

public class EventListener
{
	public static final String channelPrefix = "trans";
	private TranslationEngine translation;
	private Logger logger;
	
	public EventListener(TranslationEngine translation, Logger logger)
	{
		this.translation = translation;
		this.logger = logger;
	}
	
	public void post(TextChannel channel, String message)
	{
		channel.sendMessage(message).submit();
	}

	@SubscribeEvent
	public void onMessageReceived(MessageReceivedEvent event)
	{
		if (event.getChannel().getName().startsWith(channelPrefix))
		{
			Message message = event.getMessage();
			String content = message.getContentStripped();
			
			int space = content.indexOf(' ');
			if (space == 2)
			{
				String language = content.substring(0, space);
				String text = content.substring(space + 1, content.length());
				logger.info("Source language is '" + language + "' and message is '" + text + "'");
				translation.translate(language, text, translated -> {
					post(event.getTextChannel(), translated);
				});
			}
		}
	}
	
	@SubscribeEvent
	public void onReadyEvent(ReadyEvent event)
	{
		logger.info("Connected");
		logger.info("Authentication URL is: " + event.getJDA().asBot().getInviteUrl(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE));
	}
}
