package com.sap.oss.phosphor.fosstars.model.score.oss;

import static com.sap.oss.phosphor.fosstars.TestUtils.assertScore;
import static com.sap.oss.phosphor.fosstars.model.feature.oss.OssFeatures.VULNERABILITIES;
import static com.sap.oss.phosphor.fosstars.model.other.Utils.setOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.sap.oss.phosphor.fosstars.model.Score;
import com.sap.oss.phosphor.fosstars.model.other.Utils;
import com.sap.oss.phosphor.fosstars.model.value.ScoreValue;
import com.sap.oss.phosphor.fosstars.model.value.Vulnerabilities;
import org.junit.Test;

public class UnpatchedVulnerabilitiesScoreTest {

  @Test
  public void calculateForAllUnknown() {
    Score score = new UnpatchedVulnerabilitiesScore();
    assertScore(Score.MIN, score, Utils.allUnknown(score.allFeatures()));
  }

  @Test
  public void calculate() {
    Score score = new UnpatchedVulnerabilitiesScore();
    assertScore(Score.INTERVAL, score, setOf(VULNERABILITIES.value(new Vulnerabilities())));
  }

  @Test(expected = IllegalArgumentException.class)
  public void noInfoAboutVulnerabilities() {
    assertScore(Score.INTERVAL, new UnpatchedVulnerabilitiesScore(), setOf());
  }

  @Test
  public void explanation() {
    Score score = new UnpatchedVulnerabilitiesScore();
    assertNotNull(score.description());
    assertTrue(score.description().isEmpty());
    ScoreValue value = score.calculate(VULNERABILITIES.value(new Vulnerabilities()));
    assertNotNull(value);
    assertFalse(value.explanation().isEmpty());
  }
}