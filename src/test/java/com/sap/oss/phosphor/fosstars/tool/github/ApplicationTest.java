package com.sap.oss.phosphor.fosstars.tool.github;

import static com.sap.oss.phosphor.fosstars.tool.github.GitHubProjectFinder.EMPTY_EXCLUDE_LIST;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.sap.oss.phosphor.fosstars.tool.github.Application.ReportConfig;
import com.sap.oss.phosphor.fosstars.tool.github.GitHubProjectFinder.OrganizationConfig;
import com.sap.oss.phosphor.fosstars.tool.github.GitHubProjectFinder.ProjectConfig;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class ApplicationTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNoParameters() throws IOException {
    new Application().run();
  }

  @Test
  public void testHelp() throws IOException {
    new Application("-help").run();
    new Application("-h").run();
  }

  @Test
  public void testLoadConfig() throws IOException {
    final String filename = "ValidSecurityRatingCalculatorConfig.yml";
    try (InputStream is = getClass().getResourceAsStream(filename)) {
      Application.Config mainConfig = Application.config(is);

      assertEquals(".fosstars/project_rating_cache.json", mainConfig.cacheFilename);

      assertNotNull(mainConfig.reportConfigs);
      assertEquals(2, mainConfig.reportConfigs.size());

      for (ReportConfig reportConfig : mainConfig.reportConfigs) {
        assertNotNull(reportConfig.type);
        switch (reportConfig.type) {
          case MARKDOWN:
            assertEquals(".fosstars/report", reportConfig.where);
            assertEquals(".fosstars/report/github_projects.json", reportConfig.source);
            break;
          case JSON:
            assertEquals(".fosstars/report/github_projects.json", reportConfig.where);
            break;
          default:
            fail("Unexpected report type: " + reportConfig.type);
        }
      }

      assertNotNull(mainConfig.finderConfig);
      assertNotNull(mainConfig.finderConfig.organizationConfigs);
      assertEquals(3, mainConfig.finderConfig.organizationConfigs.size());
      assertThat(
          mainConfig.finderConfig.organizationConfigs,
          hasItem(
              new OrganizationConfig("apache", Arrays.asList("incubator", "incubating"), 0)));
      assertThat(
          mainConfig.finderConfig.organizationConfigs,
          hasItem(
              new OrganizationConfig("eclipse", Collections.singletonList("incubator"), 0)));
      assertThat(
          mainConfig.finderConfig.organizationConfigs,
          hasItem(
              new OrganizationConfig("spring-projects", EMPTY_EXCLUDE_LIST, 0)));
      assertNotNull(mainConfig.finderConfig.projectConfigs);
      assertEquals(2, mainConfig.finderConfig.projectConfigs.size());
      assertThat(
          mainConfig.finderConfig.projectConfigs,
          hasItem(
              new ProjectConfig("FasterXML", "jackson-databind")));
      assertThat(
          mainConfig.finderConfig.projectConfigs,
          hasItem(
              new ProjectConfig("FasterXML", "jackson-dataformat-xml")));
    }
  }

}