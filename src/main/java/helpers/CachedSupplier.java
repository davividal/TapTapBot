package helpers;

import java.time.Duration;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

public class CachedSupplier<T> implements Supplier<Optional<T>>{

  private final Supplier<T> valueSupplier;
  private Optional<T> value = Optional.empty();
  private final Optional<String> firstInitStartMessage;
  private final Optional<String> firstInitEndMessage;
  
  public CachedSupplier(Supplier<T> valueSupplier, Duration timeout, String firstInitStartMessage, String firstInitEndMessage) {
    super();
    this.valueSupplier = valueSupplier;
    Timer t = new Timer();
    t.scheduleAtFixedRate(new Updater(), 10000,
        timeout.toMillis());
    this.firstInitStartMessage=Optional.ofNullable(firstInitStartMessage);
    this.firstInitEndMessage=Optional.ofNullable(firstInitEndMessage);

  }
  
  public CachedSupplier(Supplier<T> valueSupplier, Duration timeout) {
    this(valueSupplier,timeout,null,null);
  }

  @Override
  public Optional<T> get() {
    return value;
  }

  private class Updater extends TimerTask {

    @Override
    public void run() {
      boolean sendmsg = !value.isPresent();
      if(sendmsg&&firstInitStartMessage.isPresent()) {
        System.err.println(firstInitStartMessage.get());
      }
      value = Optional.of(valueSupplier.get());
      if(sendmsg&&firstInitEndMessage.isPresent()) {
        System.err.println(firstInitEndMessage.get());
      }
    }
  }

}
