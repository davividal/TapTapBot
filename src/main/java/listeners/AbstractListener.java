// $Id $
// (C) cantamen/Paul Kramer 2020
package listeners;

import java.util.Optional;

import config.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * TODO documentation
 */
public class AbstractListener extends ListenerAdapter {

  protected final JDA jda;

  public AbstractListener(JDA jda) {
    super();
    this.jda = jda;
  }

  @Override
  public final void onMessageReceived(MessageReceivedEvent event) {
    super.onMessageReceived(event);
    if (event.getAuthor().isBot()) {
      return;
    }

    if (!event.getGuild().getId().equals(Config.get("bot.serverId"))) {
      return;
    }

    messageReceived(event);

  }

  @Override
  public final void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
    super.onPrivateMessageReactionAdd(event);
    if (event.getUser().isBot()) {
      return;
    }
    privateMessageReactionAdd(event);
  }

  @Override
  public final void onMessageReactionAdd(MessageReactionAddEvent event) {
    super.onMessageReactionAdd(event);
    if (event.getUser().isBot()) {
      return;
    }
    if (event.getChannelType() == ChannelType.PRIVATE) {
      return;
    }
    if (!event.getGuild().getId().equals(Config.get("bot.serverId"))) {
      return;
    }
    messageReactionAdd(event);
  };

  @Override
  public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
    super.onGuildMessageReceived(event);
    if (!event.getGuild().getId().equals(Config.get("bot.serverId"))) {
      return;
    }
    if (Optional.ofNullable(event).map(GuildMessageReceivedEvent::getMember).map(Member::getUser).map(User::isBot)
        .orElse(false)) {
      return;
    }
    guildMessageReceived(event);
  }

  protected void messageReactionAdd(MessageReactionAddEvent event) {
    // do nothing by default
  }

  protected void privateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
    // do nothing by default
  }

  protected void messageReceived(MessageReceivedEvent event) {
    // do nothing by default
  }

  protected void guildMessageReceived(GuildMessageReceivedEvent event) {
    // do nothing by default
  }
}

// end of file
