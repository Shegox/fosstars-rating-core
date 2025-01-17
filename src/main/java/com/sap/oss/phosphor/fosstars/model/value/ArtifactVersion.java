package com.sap.oss.phosphor.fosstars.model.value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class represents a specific version of an artifact that is produced by an open source
 * project. For example, it may be a jar file.
 */
public class ArtifactVersion {

  public static final ArtifactVersion EMPTY = new ArtifactVersion("", LocalDate.now());

  /**
   * Comparator for artifact versions release date.
   */
  static final Comparator<ArtifactVersion> RELEASE_DATE_COMPARISON =
      (a, b) -> b.getReleaseDate().compareTo(a.getReleaseDate());

  private final String version;

  @JsonDeserialize(using = LocalDateDeserializer.class)
  @JsonSerialize(using = LocalDateSerializer.class)
  private final LocalDate releaseDate;

  @JsonIgnore
  private final Optional<SemanticVersion> semanticVersion;

  /**
   * Initialize the ArtifactVersion based on version tag and release date.
   *
   * @param version version tag
   * @param releaseDate release date
   */
  public ArtifactVersion(@JsonProperty("version") String version,
      @JsonProperty("releaseDate") LocalDate releaseDate) {

    Objects.requireNonNull(version, "Version must be set");
    Objects.requireNonNull(releaseDate, "Release date must be set");

    this.version = version;
    this.releaseDate = releaseDate;
    this.semanticVersion = SemanticVersion.parse(version);
  }

  /**
   * Sort artifact versions by release date.
   *
   * @param versions the artifact versions
   * @return sorted collection of ArtifactVersion
   */
  public static Collection<ArtifactVersion> sortByReleaseDate(Set<ArtifactVersion> versions) {
    SortedSet<ArtifactVersion> sortedArtifacts = new TreeSet<>(RELEASE_DATE_COMPARISON);
    sortedArtifacts.addAll(versions);
    return sortedArtifacts;
  }

  public Optional<SemanticVersion> getSemanticVersion() {
    return semanticVersion;
  }

  @JsonIgnore
  public boolean isValidSemanticVersion() {
    return semanticVersion.isPresent();
  }

  public String getVersion() {
    return version;
  }

  public LocalDate getReleaseDate() {
    return releaseDate;
  }

  @Override
  public String toString() {
    return "{"
        + "version='" + version + '\''
        + ", releaseDate=" + releaseDate
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArtifactVersion compareVersion = (ArtifactVersion) o;
    return version.equals(compareVersion.version) && releaseDate.equals(compareVersion.releaseDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, releaseDate);
  }

  /**
   * LocalDate to Date deserializer used by Jackson Databind for JSON parsing.
   */
  private static class LocalDateDeserializer extends StdDeserializer<LocalDate> {

    private static final long serialVersionUID = 1L;

    protected LocalDateDeserializer() {
      super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      String text = jp.readValueAs(String.class);
      return LocalDate.parse(text);
    }
  }

  /**
   * LocalDate to Date serializer used by Jackson Databind for JSON writing.
   */
  private static class LocalDateSerializer extends StdSerializer<LocalDate> {

    private static final long serialVersionUID = 1L;

    public LocalDateSerializer() {
      super(LocalDate.class);
    }

    @Override
    public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider sp)
        throws IOException {
      gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
  }
}
