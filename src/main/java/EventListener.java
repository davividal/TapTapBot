import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

// $Id $
// (C) cantamen/Paul Kramer 2019

/** 
 * TODO documentation
 *
 * @author (C) cantamen/Paul Kramer 2019
 * @version $Id $
 */
public class EventListener extends ListenerAdapter{
  private final JDA jda;
  public EventListener(JDA jda) {
    super();
    this.jda=jda;
  }

  private static String PREFIX="tap!";
  private static int MAX_SUGGESTIONS=3;
  private static int MAX_SUGGESTIONS_TIMEOUT=1;
  
  private List<Suggestion> lastSuggestions = new ArrayList<>();
 
  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    super.onMessageReceived(event);
    String messageContent=event.getMessage().getContentRaw();

    if(event.getAuthor().isBot()) {
      return;
    }
    
    if(messageContent.startsWith(PREFIX+"suggest")) {
      synchronized (lastSuggestions) {
        lastSuggestions.stream().filter(suggestion->suggestion.timestamp.isBefore(Instant.now().minus(Duration.ofMinutes(MAX_SUGGESTIONS_TIMEOUT))));
      }
      if(lastSuggestions.stream().filter(suggestion->suggestion.userId.equals(event.getAuthor().getId())).count()>=MAX_SUGGESTIONS) {
        event.getChannel().sendMessage("You have sent more than "+MAX_SUGGESTIONS+" suggestions in the last "+MAX_SUGGESTIONS_TIMEOUT+" minutes. Please wait a bit.").queue();
      } else {
        TextChannel suggestionsChannel=jda.getTextChannelById("657168992984760340");
        suggestionsChannel.sendMessage("Suggested by: "+event.getAuthor().getAsMention()+"\n>>> "+messageContent.replaceFirst(PREFIX+"suggest ","")).queue(success->{
          success.addReaction("U+1F44D").queue(unused->success.addReaction("U+1F44E").queue(unused2 ->success.addReaction("U+1F5D1").queue()));
          lastSuggestions.add(new Suggestion(event.getAuthor().getId()));
        });
      }
    }
  }

  @Override
  public void onMessageReactionAdd(MessageReactionAddEvent event) {
    super.onMessageReactionAdd(event);
    if (event.getUser().isBot()) {
      return;
    }
    if (event.getChannel().getId().equals("657168992984760340")) {
      event.getTextChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
        if (event.getReactionEmote().isEmoji()
          && event.getReactionEmote().getEmoji().equals(new String(Character.toChars(0x1f5d1)))) {
          if (message.getContentRaw().startsWith("Suggested by: " + event.getMember().getAsMention())) {
            event.getChannel().deleteMessageById(event.getMessageId()).queue();
          } else {
            event.getReaction().removeReaction(event.getUser()).queue();
          }
        }
        if (event.getReactionEmote().isEmoji()
          && event.getReactionEmote().getEmoji().equals(new String(Character.toChars(0x1F44D)))) {
          message.removeReaction("U+1F44E",event.getUser()).queue();
        }
        if (event.getReactionEmote().isEmoji()
          && event.getReactionEmote().getEmoji().equals(new String(Character.toChars(0x1F44E)))) {
          message.removeReaction("U+1F44D",event.getUser()).queue();   
        }
      });
    }
  }

  
}


// end of file
