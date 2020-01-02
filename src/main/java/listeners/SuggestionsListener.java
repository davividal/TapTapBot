package listeners;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import config.Config;
import containers.CommandMessage;
import containers.Suggestion;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

// $Id $
// (C) cantamen/Paul Kramer 2019

/**
 * Listener for the suggestions module
 */
public class SuggestionsListener extends AbstractMessageListener {
  public SuggestionsListener(JDA jda) {
    super(jda, "suggest");
  }

  private List<Suggestion> lastSuggestions = new ArrayList<>();

  @Override
  protected void messageReceived(MessageReceivedEvent event, CommandMessage messageContent) {
    if (lastSuggestions.stream()
        .filter(suggestion -> suggestion.timestamp.isAfter(Instant.now()
            .minus(Duration.ofSeconds(Integer.parseInt(Config.get("suggestions.maxSuggestionsTimeoutSeconds"))))))
        .filter(suggestion -> suggestion.userId.equals(event.getAuthor().getId()))
        .count() >= Integer.parseInt(Config.get("suggestions.maxSuggestionsPerUser"))) {
      event.getChannel()
          .sendMessage(
              "You have sent more than " + Config.get("suggestions.maxSuggestionsPerUser") + " suggestions in the last "
                  + Config.get("suggestions.maxSuggestionsTimeoutSeconds") + " seconds. Please wait a bit.")
          .queue();
    } else {
      TextChannel suggestionsChannel = jda.getTextChannelById(Config.get("suggestions.channelId"));
      suggestionsChannel
          .sendMessage(
              "Suggested by: " + event.getAuthor().getAsMention() + "\n>>> " + messageContent + event.getMessage()
                  .getAttachments().stream().map(Attachment::getUrl).map(x -> "\n" + x).collect(Collectors.joining()))
          .queue(success -> {
            success.addReaction("U+1F44D").queue(
                unused -> success.addReaction("U+1F44E").queue(unused2 -> success.addReaction("U+1F5D1").queue()));
            lastSuggestions.add(new Suggestion(event.getAuthor().getId()));
          });
    }
  }

  @Override
  public void messageReactionAdd(MessageReactionAddEvent event) {
    if (event.getChannel().getId().equals(Config.get("suggestions.channelId"))) {
      event.getTextChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
        if (event.getReactionEmote().isEmoji()
            && event.getReactionEmote().getEmoji().equals(new String(Character.toChars(0x1f5d1)))) {
          if (message.getContentRaw().startsWith("Suggested by: " + event.getMember().getUser().getAsMention())) {
            event.getChannel().deleteMessageById(event.getMessageId()).queue();
          } else {
            event.getReaction().removeReaction(event.getUser()).queue();
          }
        }
        if (event.getReactionEmote().isEmoji()
            && event.getReactionEmote().getEmoji().equals(new String(Character.toChars(0x1F44D)))) {
          message.removeReaction("U+1F44E", event.getUser()).queue();
        }
        if (event.getReactionEmote().isEmoji()
            && event.getReactionEmote().getEmoji().equals(new String(Character.toChars(0x1F44E)))) {
          message.removeReaction("U+1F44D", event.getUser()).queue();
        }
      });
    }
  }

}

// end of file
