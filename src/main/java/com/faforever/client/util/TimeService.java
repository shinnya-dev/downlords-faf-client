package com.faforever.client.util;

import com.faforever.client.i18n.I18n;
import com.faforever.client.preferences.ChatPrefs;
import com.faforever.client.preferences.DateInfo;
import com.faforever.client.preferences.LocalizationPrefs;
import com.faforever.client.preferences.TimeInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.TimeZone;


@Lazy
@Service
@RequiredArgsConstructor
public class TimeService {

  private final I18n i18n;
  private final ChatPrefs chatPrefs;
  private final LocalizationPrefs localizationPrefs;

  private static final DateTimeFormatter FORMATTER_DMY_SHORT = DateTimeFormatter.ofPattern("d/M/yy");
  private static final DateTimeFormatter FORMATTER_DMY_MEDIUM = DateTimeFormatter.ofPattern("d MMM, yyyy");
  private static final DateTimeFormatter FORMATTER_DMY_LONG = DateTimeFormatter.ofPattern("d MMMM, yyyy");
  private static final DateTimeFormatter FORMATTER_DMY_FULL = DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy");

  private static final DateTimeFormatter FORMATTER_MDY_SHORT = DateTimeFormatter.ofPattern("M/d/yy");
  private static final DateTimeFormatter FORMATTER_MDY_MEDIUM = DateTimeFormatter.ofPattern("MMM d, yyyy");
  private static final DateTimeFormatter FORMATTER_MDY_LONG = DateTimeFormatter.ofPattern("MMMM d, yyyy");
  private static final DateTimeFormatter FORMATTER_MDY_FULL = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

  public String asDateTime(TemporalAccessor temporalAccessor) {
    if (temporalAccessor == null) {
      return i18n.get("noDateAvailable");
    }
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                            .withLocale(getCurrentDateLocale())
                            .withZone(TimeZone.getDefault().toZoneId())
                            .format(temporalAccessor);
  }

  public String asDate(TemporalAccessor temporalAccessor) {
    return asDate(temporalAccessor, FormatStyle.SHORT);
  }

  public String asDate(TemporalAccessor temporalAccessor, FormatStyle formatStyle) {
    if (temporalAccessor == null) {
      return i18n.get("noDateAvailable");
    }

    DateInfo dateInfo = localizationPrefs.getDateFormat();
    DateTimeFormatter formatter = switch (dateInfo) {
      case AUTO -> DateTimeFormatter.ofLocalizedDate(formatStyle);
      case DAY_MONTH_YEAR -> getDMYFormatter(formatStyle);
      case MONTH_DAY_YEAR -> getMDYFormatter(formatStyle);
    };

    return formatter.withLocale(i18n.getUserSpecificLocale())
                    .withZone(TimeZone.getDefault().toZoneId())
                    .format(temporalAccessor);
  }


  public String asShortTime(Temporal temporal) {
    if (temporal == null) {
      return "";
    }
    return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                            .withLocale(getCurrentTimeLocale())
                            .format(ZonedDateTime.ofInstant(Instant.from(temporal), TimeZone.getDefault().toZoneId()));
  }

  private Locale getCurrentTimeLocale() {
    if (chatPrefs.getTimeFormat().equals(TimeInfo.AUTO)) {
      return i18n.getUserSpecificLocale();
    }
    return chatPrefs.getTimeFormat().getUsedLocale();

  }

  private Locale getCurrentDateLocale() {
    DateInfo dateInfo = localizationPrefs.getDateFormat();
    if (dateInfo.equals(DateInfo.AUTO)) {
      return i18n.getUserSpecificLocale();
    }
    return dateInfo.getUsedLocale();

  }

  private DateTimeFormatter getDMYFormatter(FormatStyle style) {
    return switch (style) {
      case SHORT -> FORMATTER_DMY_SHORT;
      case MEDIUM -> FORMATTER_DMY_MEDIUM;
      case LONG -> FORMATTER_DMY_LONG;
      case FULL -> FORMATTER_DMY_FULL;
    };
  }

  private DateTimeFormatter getMDYFormatter(FormatStyle style) {
    return switch (style) {
      case SHORT -> FORMATTER_MDY_SHORT;
      case MEDIUM -> FORMATTER_MDY_MEDIUM;
      case LONG -> FORMATTER_MDY_LONG;
      case FULL -> FORMATTER_MDY_FULL;
    };
  }

  /**
   * Returns the localized minutes and seconds (e.g. '20min 31s'), or hours and minutes (e.g. '1h 5min') of the
   * specified duration.
   */
  public String shortDuration(Duration duration) {
    if (duration == null) {
      return "";
    }

    if (Duration.ofMinutes(1).compareTo(duration) > 0) {
      return i18n.get("duration.seconds", duration.getSeconds());
    }
    if (Duration.ofHours(1).compareTo(duration) > 0) {
      return i18n.get("duration.minutesSeconds", duration.toMinutes(), duration.getSeconds() % 60);
    }

    return i18n.get("duration.hourMinutes", duration.toMinutes() / 60, duration.toMinutes() % 60);
  }

  /**
   * Returns the localized time of duration, ignoring precision, ex minutes without seconds or hours without
   * minutes and seconds
   */
  public String shortDurationWithoutPrecision(Duration duration) {
    if (duration == null) {
      return "";
    }

    if (Duration.ofMinutes(1).compareTo(duration) > 0) {
      return i18n.get("duration.minute");
    }
    if (Duration.ofHours(1).compareTo(duration) > 0) {
      return i18n.get("duration.minutes", duration.toMinutes());
    }
    if (Duration.ofDays(1).compareTo(duration) > 0) {
      return i18n.get("duration.hours", duration.toHours());
    }
    return i18n.get("duration.days", duration.toDays());
  }

  /**
   * Returns e.g. "3:21:12" (h:mm:ss).
   */
  public String asHms(Duration duration) {
    long seconds = duration.getSeconds();
    return String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
  }
}
